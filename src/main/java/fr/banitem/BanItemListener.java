package fr.banitem;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class BanItemListener implements Listener {
    private final BannedItems banned;

    BanItemListener(final BannedItems banned){ this.banned = banned; }

    private BannedItem getBannedItem(final Player p, final Material m){
        return p.hasPermission("banitem.bypass") ? null : banned.getBannedItem(p.getLocation().getWorld().getName(), m);
    }

    private String color(String text){
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlaceEvent(final BlockPlaceEvent e){
        BannedItem bi = getBannedItem(e.getPlayer(), e.getBlock().getType());
        if(bi == null) return;
        if(bi.ignorePlaced()) return;

        if(bi.getNotAllowedMessage() != null) e.getPlayer().sendMessage(color(bi.getNotAllowedMessage()));
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreakEvent(final BlockBreakEvent e){
        BannedItem bi = getBannedItem(e.getPlayer(), e.getBlock().getType());
        if(bi == null) return;
        if(bi.ignoreBreaked()) return;

        if(bi.getNotAllowedMessage() != null) e.getPlayer().sendMessage(color(bi.getNotAllowedMessage()));
        e.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPickupItemEvent(final EntityPickupItemEvent e){
        if(!(e.getEntity() instanceof Player)) return;
        BannedItem bi = getBannedItem((Player)e.getEntity(), e.getItem().getItemStack().getType());
        if(bi == null) return;
        if(bi.ignorePickUp()) return;

        if(bi.getNotAllowedMessage() != null) e.getEntity().sendMessage(color(bi.getNotAllowedMessage()));
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(final EntityDropItemEvent e){
        if(!(e.getEntity() instanceof Player)) return;
        BannedItem bi = getBannedItem((Player)e.getEntity(), e.getItemDrop().getItemStack().getType());
        if(bi == null) return;
        if(bi.ignoreDrop()) return;

        if(bi.getNotAllowedMessage() != null) e.getEntity().sendMessage(color(bi.getNotAllowedMessage()));
        e.setCancelled(true);
    }


}
