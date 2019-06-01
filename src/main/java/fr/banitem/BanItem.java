package fr.banitem;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class BanItem extends JavaPlugin {
    private final Map<String, Map<Material, Item>> items = new HashMap<>();
    private boolean v12OrMore, v9OrMore;

    enum Option {
        PLACE,
        BREAK,
        PICKUP,
        DROP,
        INTERACT,
        CREATIVE
    }

    final class Item {
        private final Set<Option> options;
        private final String message;

        Item(final Set<Option> options, final String message){
            this.options = options;
            this.message = message;
        }

        Set<Option> getOptions(){ return options; }
        String getMessage(){ return message; }
    }

    @Override
    public void onEnable() {
        // Checking versions
        v12OrMore = getServer().getBukkitVersion().matches("(1\\.12)(.*)|(1\\.13)(.*)|(1\\.14)(.*)");
        v9OrMore = getServer().getBukkitVersion().matches("(1\\.9)(.*)|(1\\.10)(.*)|(1\\.11)(.*)") || v12OrMore;

        load();
        getLogger().info("BanItem: Enabled");
    }

    private void load(){
        // Config:
        saveDefaultConfig();
        reloadConfig();

        // Preparing listeners
        HandlerList.unregisterAll(this);
        Listener listener = new Listener(){};
        EventPriority eventPriority = EventPriority.HIGHEST;
        boolean blockPlaceEvent = false;
        boolean blockBreakEvent = false;
        boolean interactEvent = false;
        boolean pickupItemEvent = false;
        boolean dropItemEvent = false;

        // Loading banned items:
        items.clear();
        for(String material : getConfig().getKeys(false)){
            if(material.equals("no-permission")) continue;

            // Checking material
            final Material m = Material.getMaterial(material.toUpperCase());
            if(m == null){
                getLogger().warning("Unknown material '" + material + "'");
                continue;
            }

            // Checking options
            String configOptions = getConfig().getString(material + ".options");
            Set<Option> optionsList = new HashSet<>();
            if(configOptions == null || configOptions.isEmpty()){
                getLogger().warning("No banning options setted in '" + material + "'");
                continue;
            }
            if(configOptions.equals("*")) Collections.addAll(optionsList, Option.values());
            else{
                String[] options = configOptions.trim().replaceAll("\\s+", "").split(",");
                for(String option : options){
                    try {
                        optionsList.add(Option.valueOf(option.toUpperCase()));
                    }catch(Exception e){
                        getLogger().warning("Unknown option '" + option + "' for material '" + material + "'");
                    }
                }
            }
            if(optionsList.isEmpty()){
                getLogger().warning("No valid banning options setted in '" + material + "'");
                continue;
            }
            final Item item = new Item(optionsList, getConfig().getString(material + ".message"));

            // Checking worlds
            List<String> worlds = new ArrayList<>();
            if(getConfig().isString(material + ".worlds")) worlds.add(getConfig().getString(material + ".worlds"));
            else worlds = getConfig().getStringList(material + ".worlds");

            if(worlds.isEmpty()) getServer().getWorlds().forEach(w -> addBannedItem(w.getName(), m, item));
            else{
                for(String world : worlds){
                    World w = getServer().getWorld(world);
                    if(w == null) {
                        getLogger().warning("Unknown world '" + world + "' for item '" + material + "'.");
                        continue;
                    }
                    addBannedItem(w.getName(), m, item);
                }
            }

            // Loading listeners
            if(!blockPlaceEvent && optionsList.contains(Option.PLACE)){
                getServer().getPluginManager().registerEvent(BlockPlaceEvent.class, listener, eventPriority, (l, e) -> {
                        BlockPlaceEvent event = (BlockPlaceEvent) e;
                        if(isBannedItem(event.getPlayer(), event.getBlock().getType(), Option.PLACE)) event.setCancelled(true);
                    }, this, true);
                blockPlaceEvent = true;
            }
            if(!blockBreakEvent && optionsList.contains(Option.BREAK)){
                getServer().getPluginManager().registerEvent(BlockBreakEvent.class, listener, eventPriority, (l, e) -> {
                    BlockBreakEvent event = (BlockBreakEvent) e;
                    if(isBannedItem(event.getPlayer(), event.getBlock().getType(), Option.BREAK)) event.setCancelled(true);
                }, this, true);
                blockBreakEvent = true;
            }

            if(!interactEvent && optionsList.contains(Option.INTERACT)){
                // >=1.9: It has EquipmentSlot
                // <1.9: it doesn't
                if(v9OrMore) getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, listener, eventPriority, (l, e) -> onPlayerInteractEvent((PlayerInteractEvent) e, true), this, true);
                else getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, listener, eventPriority, (l, e) -> onPlayerInteractEvent((PlayerInteractEvent) e, false), this, true);
                interactEvent = true;
            }

            if(!pickupItemEvent && optionsList.contains(Option.PICKUP)){
                // >=1.12: EntityPickupItemEvent
                // <1.12: PlayerPickupItemEvent
                if(v12OrMore) getServer().getPluginManager().registerEvent(org.bukkit.event.entity.EntityPickupItemEvent.class, listener, eventPriority, (l, e) -> {
                    org.bukkit.event.entity.EntityPickupItemEvent event = (org.bukkit.event.entity.EntityPickupItemEvent)e;
                    if(!(event.getEntity() instanceof Player)) return;
                    if(isBannedItem((Player)event.getEntity(), event.getItem().getItemStack().getType(), Option.PICKUP)) event.setCancelled(true);
                }, this, true);
                else getServer().getPluginManager().registerEvent(org.bukkit.event.player.PlayerPickupItemEvent.class, listener, eventPriority, (l, e) -> {
                    org.bukkit.event.player.PlayerPickupItemEvent event = (org.bukkit.event.player.PlayerPickupItemEvent)e;
                    if(isBannedItem(event.getPlayer(), event.getItem().getItemStack().getType(), Option.PICKUP)) event.setCancelled(true);
                }, this, true);
                pickupItemEvent = true;
            }

            if(!dropItemEvent && optionsList.contains(Option.DROP)){
                // >=1.12: EntityDropItemEvent
                // <1.12: PlayerDropItemEvent
                if(v12OrMore) getServer().getPluginManager().registerEvent(org.bukkit.event.entity.EntityDropItemEvent.class, listener, eventPriority, (l, e) -> {
                    org.bukkit.event.entity.EntityDropItemEvent event = (org.bukkit.event.entity.EntityDropItemEvent)e;
                    if(!(event.getEntity() instanceof Player)) return;
                    if(isBannedItem((Player)event.getEntity(), event.getItemDrop().getItemStack().getType(), Option.DROP)) event.setCancelled(true);
                }, this, true);
                else getServer().getPluginManager().registerEvent(org.bukkit.event.player.PlayerDropItemEvent.class, listener, eventPriority, (l, e) -> {
                    org.bukkit.event.player.PlayerDropItemEvent event = (org.bukkit.event.player.PlayerDropItemEvent)e;
                    if(isBannedItem(event.getPlayer(), event.getItemDrop().getItemStack().getType(), Option.DROP)) event.setCancelled(true);
                }, this, true);
                dropItemEvent = true;
            }
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("banitem.command")) {
            sender.sendMessage(color(getConfig().getString("no-permission")));
            return true;
        }

        // Reload command
        if(args.length > 0 && args[0].equalsIgnoreCase("reload")){
            if(!sender.hasPermission("banitem.command.reload")) {
                sender.sendMessage(color(getConfig().getString("no-permission")));
                return true;
            }

            load();
            sender.sendMessage(color("&c[&e&lBanItem&c] &2Config reloaded."));
            return true;
        }

        // Help message
        sender.sendMessage(color("&c[&e&lBanItem&c] &2Version: &ev" + getDescription().getVersion()));
        sender.sendMessage(color("&c[&e&lBanItem&c] &7Use /banitem &3reload&7 to reload the config."));
        return true;
    }

    private void addBannedItem(final String world, final Material m, final Item item){
        Map<Material, Item> map = items.get(world);
        if(map == null) map = new HashMap<>();
        map.put(m, item);
        items.put(world, map);
    }

    private boolean isBannedItem(final Player p, final Material m, final Option option){
        final String wName = p.getWorld().getName();
        if(!items.containsKey(wName) || !items.get(wName).containsKey(m)) return false;
        if(p.hasPermission("banitem.bypass." + wName + ".*") || p.hasPermission("banitem.bypass." + wName + "." + m.name().toLowerCase() + "")) return false;
        Item i = items.get(wName).get(m);
        if(!i.getOptions().contains(option)) return false;
        if(p.getGameMode() != GameMode.CREATIVE && i.getOptions().contains(Option.CREATIVE)) return false;
        if(option != Option.PICKUP && i.getMessage() != null) p.sendMessage(color(i.getMessage()));
        return true;
    }

    private String color(String text){
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void onPlayerInteractEvent(final PlayerInteractEvent e, boolean equipment){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        if(equipment && e.getHand() != EquipmentSlot.HAND) return;
        if(isBannedItem(e.getPlayer(), e.getClickedBlock().getType(), Option.INTERACT)) e.setCancelled(true);
    }

}
