package fr.andross.banitem;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;

import java.util.*;

class BanItem {
    private final Plugin pl;
    private final boolean v12OrMore, v9OrMore;
    private final BanMap map;
    private long pickupCooldown = 1000;

    BanItem (final Plugin pl) {
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
        // Checking pickup cooldown
        pickupCooldown = pl.getConfig().getLong("pickup-message-cooldown", 1000);

        // (re)Loading from config into maps
        final Set<BanOption> options = map.load();

        // (re)Loading listeners
        HandlerList.unregisterAll(pl);
        final Listener l = new Listener() { };
        final EventPriority ep = EventPriority.LOWEST;

        // Registering listeners
        // Adding listeners if option is setted
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
            pl.getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, l, ep,
                    v9OrMore ? (li, e) -> onPlayerInteractEvent((PlayerInteractEvent) e, true) : (li, e) -> onPlayerInteractEvent((PlayerInteractEvent) e, false)
                    , pl, true);
        }
        if (options.contains(BanOption.INVENTORY)) {
            pl.getServer().getPluginManager().registerEvent(InventoryClickEvent.class, l, ep, (li, e) -> {
                final InventoryClickEvent event = (InventoryClickEvent) e;
                final ItemStack item = event.getCurrentItem();
                if(item == null) return;
                if (isBanned((Player)event.getWhoClicked(), item.getType(), BanOption.INVENTORY))
                    event.setCancelled(true);
            }, pl, true);
        }
        if (options.contains(BanOption.DROP)) {
            pl.getServer().getPluginManager().registerEvent(PlayerDropItemEvent.class, l, ep, (li, e) -> {
                final PlayerDropItemEvent event = (PlayerDropItemEvent) e;
                if (isBanned(event.getPlayer(), event.getItemDrop().getItemStack().getType(), BanOption.DROP))
                    event.setCancelled(true);
            }, pl, true);
        }
        if (options.contains(BanOption.PICKUP)) {
            // Pickup cooldown map clearing
            pl.getServer().getPluginManager().registerEvent(PlayerQuitEvent.class, l, ep, (li, e) -> { map.getPickupCooldown().remove(((PlayerQuitEvent) e).getPlayer().getUniqueId());
            }, pl, true);

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



        // Handling eggs
        final List<Material> materials = new ArrayList<>();
        for (Map<Material, Map<BanOption, String>> map2 : map.getBlacklist().values()) materials.addAll(map2.keySet());
        for (Material m : materials) {
            if(!m.name().contains("_EGG")) continue;

            // Registering an egg listener
            pl.getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, l, ep, (li, e) -> {
                final PlayerInteractEvent event = (PlayerInteractEvent) e;
                if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
                if(event.getItem() == null) return;
                if(!event.getItem().getType().name().contains("_EGG")) return;
                if (isBanned(event.getPlayer(), event.getItem().getType(), BanOption.PLACE))
                    event.setCancelled(true);
            }, pl, true);
            return;
        }
    }
    
    private void onPlayerInteractEvent(final PlayerInteractEvent e, final boolean equipment){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        if(equipment && e.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        if(isBanned(e.getPlayer(), e.getClickedBlock().getType(), BanOption.INTERACT)) e.setCancelled(true);
    }

    private boolean isBanned(final Player p, final Material m, final BanOption o) {
        final String w = p.getWorld().getName().toLowerCase();

        // Checking permission bypass
        if (p.hasPermission("banitem.bypass." + w + "." + m.name().toLowerCase())) return false;

        /* Checking blacklisted*/
        // Checking world
        final Map<Material, Map<BanOption, String>> blacklisted = map.getBlacklist().get(w);
        if (blacklisted != null) {
            // Checking material
            final Map<BanOption, String> options = blacklisted.get(m);
            if (options != null) {
                // Checking option
                if (options.containsKey(o)) {
                    // Sending banned message, if exists
                    sendMessage(p, o, options.get(o));
                    return true;
                }
            }
        }

        // Ignoring inventory for whitelist
        if(o == BanOption.INVENTORY) return false;

        /* Checking whitelisted*/
        // Checking world
        final Set<Material> whitelisted = map.getWhitelist().get(w);
        if (whitelisted == null) return false;

        // Checking material
        if (whitelisted.contains(m)) return false;

        // Material not on whitelist, so banned:
        final String message = map.getWhitelistMessage().get(w);
        if (message != null) sendMessage(p, o, message);
        return true;
    }

    private void sendMessage(final Player p, final BanOption o, final String m) {
        // No message set
        if(m == null || m.isEmpty()) return;

        // Checking pick up cooldown, to prevent spam
        if (o == BanOption.PICKUP) {
            final Long time = map.getPickupCooldown().get(p.getUniqueId());
            if (time == null) map.getPickupCooldown().put(p.getUniqueId(), System.currentTimeMillis());
            else {
                if (time + pickupCooldown > System.currentTimeMillis()) return;
                else map.getPickupCooldown().put(p.getUniqueId(), System.currentTimeMillis());
            }
        }
        p.sendMessage(color(m));
    }

    String color(final String text) { return ChatColor.translateAlternateColorCodes('&', text); }
    boolean isV9OrMore() { return v9OrMore; }

}
