package fr.andross.banitem.Utils;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Maps.CustomItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class BanUtils {

    @Nullable
    public List<World> getWorldsFromString(@NotNull final String s){
        if (s.equals("*")) return Bukkit.getWorlds();

        final List<World> worlds = new ArrayList<>();
        for (String w : s.trim().replaceAll("\\s+", "").split(",")) {
            final World world = Bukkit.getWorld(w);
            if (world == null) return null;
            worlds.add(world);
        }
        return worlds;
    }

    @Nullable
    public BannedItem getBannedItemFromString(@NotNull final String material, @NotNull final CustomItems custom_items){
        final Material m = Material.matchMaterial(material);
        return m == null ? (custom_items.getItemsConfig() == null ? null : custom_items.get(material)) : new BannedItem(m);
    }

    @Nullable
    public List<BanOption> getBanOptionsFromString(@NotNull final String options) {
        List<BanOption> optionsList = new ArrayList<>();

        switch (options.toLowerCase()) {
            case "*": return Arrays.asList(BanOption.values());
            case "*!":
                for (BanOption o : BanOption.values()) if (o != BanOption.CREATIVE) optionsList.add(o);
                return optionsList;
            case "*b": return Arrays.asList(BanOption.PLACE, BanOption.BREAK);
            default:
                for (String option : options.toUpperCase().trim().replaceAll("\\s+", "").split(",")) {
                    try {
                        optionsList.add(BanOption.valueOf(option));
                    } catch (Exception e) {
                        return null;
                    }
                }
                break;
        }

        return optionsList;
    }

    public void reloadListeners(final BanItem pl) {
        // Preparing variables
        HandlerList.unregisterAll(pl);
        final Listener l = new Listener() { };
        final EventPriority ep = EventPriority.HIGHEST;
        final BanDatabase db = pl.getDatabase();
        final Set<BanOption> blacklist = db.getBlacklistOptions();
        final boolean whitelist = db.isWhitelistEnabled();

        // Registering listeners, only if option is used
        if (blacklist.contains(BanOption.PLACE) || whitelist) {
            pl.getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, l, ep, (li, e) -> {
                final PlayerInteractEvent event = (PlayerInteractEvent) e;
                if (pl.isv9OrMore() && event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.isBlockInHand()) {
                    if (db.isBanned(event.getPlayer(), event.getItem(), BanOption.PLACE)) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (event.getItem() != null && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR))
                    if (db.isBanned(event.getPlayer(), event.getItem(), BanOption.PLACE)) event.setCancelled(true);
            }, pl);
        }

        if (blacklist.contains(BanOption.BREAK) || whitelist) {
            pl.getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, l, ep, (li, e) -> {
                final PlayerInteractEvent event = (PlayerInteractEvent) e;
                if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null)
                    if (db.isBanned(event.getPlayer(), new ItemStack(event.getClickedBlock().getType()), BanOption.BREAK)) event.setCancelled(true);
            }, pl, true);
        }

        if (blacklist.contains(BanOption.INTERACT) || whitelist) {
            pl.getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, l, ep, (li, e) -> {
                final PlayerInteractEvent event = (PlayerInteractEvent) e;
                if (pl.isv9OrMore() && event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
                if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK)
                    if (db.isBanned(event.getPlayer(), new ItemStack(event.getClickedBlock().getType()), BanOption.INTERACT)) event.setCancelled(true);
            }, pl, true);
        }

        if (blacklist.contains(BanOption.INVENTORY) || whitelist) {
            pl.getServer().getPluginManager().registerEvent(InventoryClickEvent.class, l, ep, (li, e) -> {
                final InventoryClickEvent event = (InventoryClickEvent) e;
                final ItemStack item = event.getCurrentItem();
                if (item == null) return;
                if (db.isBanned((Player)event.getWhoClicked(), item, BanOption.INVENTORY)) event.setCancelled(true);
            }, pl, true);
        }

        if (blacklist.contains(BanOption.DROP) || whitelist) {
            pl.getServer().getPluginManager().registerEvent(PlayerDropItemEvent.class, l, ep, (li, e) -> {
                final PlayerDropItemEvent event = (PlayerDropItemEvent) e;
                if (db.isBanned(event.getPlayer(), event.getItemDrop().getItemStack(), BanOption.DROP)) event.setCancelled(true);
            }, pl, true);
        }

        if (blacklist.contains(BanOption.PICKUP) || whitelist) {
            // Pickup cooldown map clearing
            pl.getServer().getPluginManager().registerEvent(PlayerQuitEvent.class, l, ep, (li, e) -> db.getPickupCooldowns().remove(((PlayerQuitEvent) e).getPlayer().getUniqueId()), pl);

            // >=1.12: EntityPickupItemEvent
            // <1.12: PlayerPickupItemEvent
            final EventExecutor ee;
            final Class<? extends Event> c;
            if (pl.isv12OrMore()) {
                c = org.bukkit.event.entity.EntityPickupItemEvent.class;
                ee = (li, e) -> {
                    final org.bukkit.event.entity.EntityPickupItemEvent event = (org.bukkit.event.entity.EntityPickupItemEvent) e;
                    if (!(event.getEntity() instanceof Player)) return;
                    if (db.isBanned((Player) event.getEntity(), event.getItem().getItemStack(), BanOption.PICKUP)) event.setCancelled(true);
                };
            } else {
                c = org.bukkit.event.player.PlayerPickupItemEvent.class;
                ee = (li, e) -> {
                    final org.bukkit.event.player.PlayerPickupItemEvent event = (org.bukkit.event.player.PlayerPickupItemEvent) e;
                    if (db.isBanned(event.getPlayer(), event.getItem().getItemStack(), BanOption.PICKUP)) event.setCancelled(true);
                };
            }
            pl.getServer().getPluginManager().registerEvent(c, l, ep, ee, pl, true);
        }
    }
}
