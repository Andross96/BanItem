package fr.andross;

import fr.andross.Utils.BanMap;
import fr.andross.Utils.BanOption;
import fr.andross.Utils.BanOptions;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;

import java.util.*;

class BanItem {
    private final Plugin pl;
    private final boolean v12OrMore, v9OrMore;
    private final BanMap map;

    BanItem(final Plugin pl){
        // Setting plugin
        this.pl = pl;

        // Checking version
        v12OrMore = pl.getServer().getBukkitVersion().matches("(1\\.12)(.*)|(1\\.13)(.*)|(1\\.14)(.*)");
        v9OrMore = pl.getServer().getBukkitVersion().matches("(1\\.9)(.*)|(1\\.10)(.*)|(1\\.11)(.*)") || v12OrMore;

        // Loading config & maps
        map = new BanMap(pl);
        load();

        pl.getLogger().info("BanItem: Sucessfully loaded.");
    }

    void load() {
        // (re)Loading config
        pl.saveDefaultConfig();
        pl.reloadConfig();

        // (re)Loading from config into maps
        final Set<BanOption> options = map.load();

        // (re)Loading listeners
        HandlerList.unregisterAll(pl);
        final Listener l = new Listener() { };
        final EventPriority ep = EventPriority.LOWEST;

        // Registering listeners
        if (options.contains(BanOption.PLACE) || !map.getWhitelist().isEmpty()) {
            pl.getServer().getPluginManager().registerEvent(BlockPlaceEvent.class, l, ep, (li, e) -> {
                final BlockPlaceEvent event = (BlockPlaceEvent) e;
                if (isBanned(event.getPlayer(), event.getBlock().getType(), BanOption.PLACE)) event.setCancelled(true);
            }, pl, true);
        }
        if (options.contains(BanOption.BREAK) || !map.getWhitelist().isEmpty()) {
            pl.getServer().getPluginManager().registerEvent(BlockBreakEvent.class, l, ep, (li, e) -> {
                final BlockBreakEvent event = (BlockBreakEvent) e;
                if (isBanned(event.getPlayer(), event.getBlock().getType(), BanOption.BREAK)) event.setCancelled(true);
            }, pl, true);
        }
        if (options.contains(BanOption.INTERACT)) {
            // >=1.9: It has EquipmentSlot
            // <1.9: it doesn't
            final EventExecutor ee = v9OrMore ? (li, e) -> onPlayerInteractEvent((PlayerInteractEvent) e, true) : (li, e) -> onPlayerInteractEvent((PlayerInteractEvent) e, false);
            pl.getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, l, ep, ee, pl, true);
        }
        if (options.contains(BanOption.PICKUP)) {
            // >=1.12: EntityPickupItemEvent
            // <1.12: PlayerPickupItemEvent
            final EventExecutor ee;
            final Class<? extends Event> c;
            if (v12OrMore) {
                c = org.bukkit.event.entity.EntityPickupItemEvent.class;
                ee = (li, e) -> {
                    final org.bukkit.event.entity.EntityPickupItemEvent event = (org.bukkit.event.entity.EntityPickupItemEvent) e;
                    if (!(event.getEntity() instanceof Player)) return;
                    if (isBanned((Player) event.getEntity(), event.getItem().getItemStack().getType(), BanOption.PICKUP)) event.setCancelled(true);
                };
            } else {
                c = org.bukkit.event.player.PlayerPickupItemEvent.class;
                ee = (li, e) -> {
                    final org.bukkit.event.player.PlayerPickupItemEvent event = (org.bukkit.event.player.PlayerPickupItemEvent) e;
                    if (isBanned(event.getPlayer(), event.getItem().getItemStack().getType(), BanOption.PICKUP)) event.setCancelled(true);
                };
            }
            pl.getServer().getPluginManager().registerEvent(c, l, ep, ee, pl, true);
        }
        if (options.contains(BanOption.DROP)) {
            pl.getServer().getPluginManager().registerEvent(PlayerDropItemEvent.class, l, ep, (li, e) -> {
                final PlayerDropItemEvent event = (PlayerDropItemEvent) e;
                if (isBanned(event.getPlayer(), event.getItemDrop().getItemStack().getType(), BanOption.DROP))
                    event.setCancelled(true);
            }, pl, true);
        }
    }
    
    private void onPlayerInteractEvent(final PlayerInteractEvent e, final boolean equipment){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        if(equipment && e.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        if(isBanned(e.getPlayer(), e.getClickedBlock().getType(), BanOption.INTERACT)) e.setCancelled(true);
    }

    private boolean isBanned(final Player p, final Material m, final BanOption o){
        final String w = p.getWorld().getName();

        // Checking permission bypass
        if(p.hasPermission("banitem.bypass." + w + "." + m.name().toLowerCase() + "")) return false;

        /* Checking blacklisted*/
        // Checking world
        final Map<Material, BanOptions> blacklisted = map.getBlacklist().get(w);
        if (blacklisted != null) {
            // Checking material
            final BanOptions bo = blacklisted.get(m);
            if (bo == null) return false;

            // Checking option
            if (bo.hasOption(o)) {
                if (!bo.hasOption(BanOption.CREATIVE) || p.getGameMode() == GameMode.CREATIVE) {
                    // Sending message if it is not a pickup (as it is very spammy)
                    if (o != BanOption.PICKUP && bo.getMessage() != null) p.sendMessage(pl.color(bo.getMessage()));
                    return true;
                }
            }
        }

        /* Checking whitelisted*/
        // Checking world
        final Set<Material> whitelisted = map.getWhitelist().get(w);
        if (whitelisted == null) return false;

        // Checking material
        if (whitelisted.contains(m)) return false;

        // Material not on whitelist, so banned:
        final String message = map.getWhitelistMessage().get(w);
        if (o != BanOption.PICKUP && message != null) p.sendMessage(pl.color(message));
        return true;
    }
}
