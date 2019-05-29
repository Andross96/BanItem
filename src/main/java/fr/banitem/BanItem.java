package fr.banitem;

import org.bukkit.ChatColor;
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
    private final Map<String, Map<Material, BannedItem>> banned = new HashMap<>();

    @Override
    public void onEnable() {
        load();
        getLogger().info("BanItem: Enabled");
    }

    private void load(){
        // Config:
        saveDefaultConfig();
        reloadConfig();

        // Checking versions
        boolean v12OrMore = getServer().getBukkitVersion().matches("1\\.12|1\\.13|1\\.14");
        boolean v11OrMore = getServer().getBukkitVersion().matches("1\\.11|1\\.12|1\\.13|1\\.14");

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
        banned.clear();
        for(String material : getConfig().getKeys(false)){
            if(material.equals("no-permission")) continue;

            final Material m = Material.getMaterial(material.toUpperCase());
            if(m == null){
                getLogger().warning("Unknown material '" + material + "'");
                continue;
            }

            final BannedItem bi = new BannedItem(getConfig().getBoolean(material + ".block-place"),
                    getConfig().getBoolean(material + ".block-break"),
                    getConfig().getBoolean(material + ".pickup"),
                    getConfig().getBoolean(material + ".drop"),
                    getConfig().getBoolean(material + ".interact"),
                    getConfig().getString(material + ".message"));


            List<String> worlds = new ArrayList<>();
            if(getConfig().isString(material + ".worlds")) worlds.add(getConfig().getString(material + ".worlds"));
            else worlds = getConfig().getStringList(material + ".worlds");

            if(worlds.isEmpty()) getServer().getWorlds().forEach(w -> addBannedItem(w.getName(), m, bi));
            else{
                for(String world : worlds){
                    World w = getServer().getWorld(world);
                    if(w == null) {
                        getLogger().warning("Unknown world '" + world + "' for item '" + material + "'.");
                        continue;
                    }
                    addBannedItem(w.getName(), m, bi);
                }
            }

            // Loading listeners
            if(!blockPlaceEvent && !bi.canPlace()){
                getServer().getPluginManager().registerEvent(BlockPlaceEvent.class, listener, eventPriority, (l, e) -> onBlockPlaceEvent((BlockPlaceEvent) e), this, true);
                blockPlaceEvent = true;
            }
            if(!blockBreakEvent && !bi.canBreak()){
                getServer().getPluginManager().registerEvent(BlockBreakEvent.class, listener, eventPriority, (l, e) -> onBlockBreakEvent((BlockBreakEvent) e), this, true);
                blockBreakEvent = true;
            }

            if(!interactEvent && !bi.canPickUp()){
                // >=1.11: It has EquipmentSlot
                // <1.11: it doesn't
                if(v11OrMore) getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, listener, eventPriority, (l, e) -> onPlayerInteractEvent((org.bukkit.event.player.PlayerInteractEvent) e, true), this, true);
                else getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, listener, eventPriority, (l, e) -> onPlayerInteractEvent((org.bukkit.event.player.PlayerInteractEvent) e, false), this, true);
                interactEvent = true;
            }

            if(!pickupItemEvent && !bi.canPickUp()){
                // >=1.12: EntityPickupItemEvent
                // <1.12: PlayerPickupItemEvent
                if(v12OrMore) getServer().getPluginManager().registerEvent(org.bukkit.event.entity.EntityPickupItemEvent.class, listener, eventPriority, (l, e) -> onEntityPickupItemEvent((org.bukkit.event.entity.EntityPickupItemEvent) e), this, true);
                else getServer().getPluginManager().registerEvent(org.bukkit.event.player.PlayerPickupItemEvent.class, listener, eventPriority, (l, e) -> onPlayerPickupItemEvent((org.bukkit.event.player.PlayerPickupItemEvent) e), this, true);
                pickupItemEvent = true;
            }

            if(!dropItemEvent && !bi.canDrop()){
                // >=1.12: EntityDropItemEvent
                // <1.12: PlayerDropItemEvent
                if(v12OrMore) getServer().getPluginManager().registerEvent(org.bukkit.event.entity.EntityDropItemEvent.class, listener, eventPriority, (l, e) -> onEntityDropItemEvent((org.bukkit.event.entity.EntityDropItemEvent) e), this, true);
                else getServer().getPluginManager().registerEvent(org.bukkit.event.player.PlayerDropItemEvent.class, listener, eventPriority, (l, e) -> onPlayerDropItemEvent((org.bukkit.event.player.PlayerDropItemEvent) e), this, true);
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

    private void addBannedItem(final String world, final Material m, final BannedItem bi){
        Map<Material, BannedItem> map = banned.get(world);
        if(map == null) map = new HashMap<>();
        map.put(m, bi);
        banned.put(world, map);
    }

    private BannedItem getBannedItem(final Player p, final Material m){
        final String wName = p.getWorld().getName();
        if(p.hasPermission("banitem.bypass." + wName)) return null;
        return (!banned.containsKey(wName) || !banned.get(wName).containsKey(m)) ? null : banned.get(wName).get(m);
    }

    private String color(String text){
        return ChatColor.translateAlternateColorCodes('&', text);
    }


    // Listeners
    private void onBlockPlaceEvent(final BlockPlaceEvent e){
        BannedItem bi = getBannedItem(e.getPlayer(), e.getBlock().getType());
        if(bi == null || bi.canPlace()) return;
        if(bi.getMessage() != null) e.getPlayer().sendMessage(color(bi.getMessage()));
        e.setCancelled(true);
    }

    private void onBlockBreakEvent(final BlockBreakEvent e){
        BannedItem bi = getBannedItem(e.getPlayer(), e.getBlock().getType());
        if(bi == null || bi.canBreak()) return;
        if(bi.getMessage() != null) e.getPlayer().sendMessage(color(bi.getMessage()));
        e.setCancelled(true);
    }

    private void onPlayerInteractEvent(final PlayerInteractEvent e, boolean equipment){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        if(equipment && e.getHand() != EquipmentSlot.HAND) return;
        BannedItem bi = getBannedItem(e.getPlayer(), e.getClickedBlock().getType());
        if(bi == null || bi.canInteract()) return;
        if(bi.getMessage() != null) e.getPlayer().sendMessage(color(bi.getMessage()));
        e.setCancelled(true);
    }

    private void onEntityPickupItemEvent(final org.bukkit.event.entity.EntityPickupItemEvent e){
        if(!(e.getEntity() instanceof Player)) return;
        BannedItem bi = getBannedItem((Player)e.getEntity(), e.getItem().getItemStack().getType());
        if(bi == null || bi.canPickUp()) return;
        e.setCancelled(true);
    }

    private void onPlayerPickupItemEvent(final org.bukkit.event.player.PlayerPickupItemEvent e){
        BannedItem bi = getBannedItem(e.getPlayer(), e.getItem().getItemStack().getType());
        if(bi == null || bi.canPickUp()) return;
        e.setCancelled(true);
    }

    private void onEntityDropItemEvent(final org.bukkit.event.entity.EntityDropItemEvent e){
        if(!(e.getEntity() instanceof Player)) return;
        BannedItem bi = getBannedItem((Player)e.getEntity(), e.getItemDrop().getItemStack().getType());
        if(bi == null || bi.canDrop()) return;
        if(bi.getMessage() != null) e.getEntity().sendMessage(color(bi.getMessage()));
        e.setCancelled(true);
    }

    private void onPlayerDropItemEvent(final org.bukkit.event.player.PlayerDropItemEvent e){
        BannedItem bi = getBannedItem(e.getPlayer(), e.getItemDrop().getItemStack().getType());
        if(bi == null || bi.canDrop()) return;
        if(bi.getMessage() != null) e.getPlayer().sendMessage(color(bi.getMessage()));
        e.setCancelled(true);
    }

}
