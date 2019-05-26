package fr.banitem;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class BanItem extends JavaPlugin implements Listener {
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

        // Loading banned items:
        banned.clear();
        for(String material : getConfig().getKeys(false)){
            if(material.equals("no-permission")) continue;

            final Material m = Material.getMaterial(material.toUpperCase());
            if(m == null){
                getLogger().warning("Unknown material '" + material + "'");
                continue;
            }

            boolean blockPlace = getConfig().getBoolean(material + ".block-place");
            boolean blockBreak = getConfig().getBoolean(material + ".block-break");
            boolean pickUp = getConfig().getBoolean(material + ".pickup");
            boolean drop = getConfig().getBoolean(material + ".drop");
            boolean interact = getConfig().getBoolean(material + ".interact");
            String message = getConfig().getString(material + ".message");
            final BannedItem bi = new BannedItem(blockPlace, blockBreak, pickUp, drop, interact, message);


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
        }

        // Registering listener
        HandlerList.unregisterAll((JavaPlugin)this);
        getServer().getPluginManager().registerEvents(this, this);
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



    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlaceEvent(final BlockPlaceEvent e){
        BannedItem bi = getBannedItem(e.getPlayer(), e.getBlock().getType());
        if(bi == null || bi.ignorePlaced()) return;
        if(bi.getMessage() != null) e.getPlayer().sendMessage(color(bi.getMessage()));
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreakEvent(final BlockBreakEvent e){
        BannedItem bi = getBannedItem(e.getPlayer(), e.getBlock().getType());
        if(bi == null || bi.ignoreBreaked()) return;
        if(bi.getMessage() != null) e.getPlayer().sendMessage(color(bi.getMessage()));
        e.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItemEvent(final EntityPickupItemEvent e){
        if(!(e.getEntity() instanceof Player)) return;
        BannedItem bi = getBannedItem((Player)e.getEntity(), e.getItem().getItemStack().getType());
        if(bi == null || bi.ignorePickUp()) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(final EntityDropItemEvent e){
        if(!(e.getEntity() instanceof Player)) return;
        BannedItem bi = getBannedItem((Player)e.getEntity(), e.getItemDrop().getItemStack().getType());
        if(bi == null || bi.ignoreDrop()) return;
        if(bi.getMessage() != null) e.getEntity().sendMessage(color(bi.getMessage()));
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEvent(final PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null || e.getHand() != EquipmentSlot.HAND) return;
        BannedItem bi = getBannedItem(e.getPlayer(), e.getClickedBlock().getType());
        if(bi == null || bi.ignoreInteract()) return;
        if(bi.getMessage() != null) e.getPlayer().sendMessage(color(bi.getMessage()));
        e.setCancelled(true);
    }
}
