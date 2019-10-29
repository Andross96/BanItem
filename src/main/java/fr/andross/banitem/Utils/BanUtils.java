package fr.andross.banitem.Utils;

import fr.andross.banitem.BanDatabase;
import fr.andross.banitem.BanItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Furnace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class BanUtils {

    @Nullable
    public static List<World> getWorldsFromString(@NotNull final String s){
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
    public static List<BanOption> getBanOptionsFromString(@NotNull final String options) {
        final List<BanOption> optionsList = new ArrayList<>();
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

    public static void deleteItemFromInventory(final BanItem pl, final String world, final Inventory... invs) {
        final Map<Material, Map<BanOption, String>> map = pl.getDb().getBlacklist().get(world);
        final Map<BannedItem, Map<BanOption, String>> customMap = pl.getDb().getBlacklist().getCustomItems(world);
        if (map == null && customMap == null) return;

        for (Inventory inv : invs) {
            for (int i = 0; i < inv.getSize(); i++) {
                final ItemStack item = inv.getItem(i);
                if (item == null || item.getType() == Material.AIR) continue;
                // Handling normal items
                if (map != null) {
                    final Map<BanOption, String> options = map.get(item.getType());
                    if (options != null && options.containsKey(BanOption.DELETE)) inv.clear(i);
                }
                // Handling custom items
                if (customMap == null) continue;
                final Map<BanOption, String> options = customMap.get(new BannedItem(item));
                if (options != null && options.containsKey(BanOption.DELETE)) inv.clear(i);
            }
        }
    }

    ///////////////////////////////////////
    /// LISTENERS
    ///////////////////////////////////////
    public static void reloadListeners(final BanItem pl) {
        // Preparing variables
        HandlerList.unregisterAll(pl);
        final Listener l = new Listener() { };
        final EventPriority ep = EventPriority.HIGHEST;
        final BanDatabase db = pl.getDb();
        final Set<BanOption> blacklist = db.getBlacklistOptions();
        final boolean whitelist = db.isWhitelistEnabled();

        // Registering listeners, only if option is used
        if (blacklist.contains(BanOption.PLACE) || whitelist) {
            pl.getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, l, ep, (li, e) -> {
                final PlayerInteractEvent event = (PlayerInteractEvent) e;
                if (pl.isv9OrMore() && event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.isBlockInHand() && event.getItem() != null) {
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

        if (blacklist.contains(BanOption.CLICK) || whitelist) {
            pl.getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, l, ep, (li, e) -> {
                final PlayerInteractEvent event = (PlayerInteractEvent) e;
                if (pl.isv9OrMore() && event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
                if (event.getItem() == null) return;
                if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR)
                    if (db.isBanned(event.getPlayer(), event.getItem(), BanOption.CLICK)) event.setCancelled(true);
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

        if (blacklist.contains(BanOption.WEAR) || whitelist) {
            pl.getServer().getPluginManager().registerEvent(InventoryClickEvent.class, l, ep, (li, e) -> {
                final InventoryClickEvent event = (InventoryClickEvent) e;
                if (event.getRawSlot() >= 5 && event.getRawSlot() <= 8) {
                    if (event.getCursor() != null) {
                        if (db.isBanned((Player) event.getWhoClicked(), event.getCursor(), BanOption.WEAR)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                if (event.getCurrentItem() == null) return;
                if (event.isShiftClick()) {
                    if (db.isBanned((Player) event.getWhoClicked(), event.getCurrentItem(), BanOption.WEAR)) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (event.getRawSlot() >= 5 && event.getRawSlot() <= 8 && event.getHotbarButton() > -1 && event.getClickedInventory() != null) {
                    final ItemStack item = event.getClickedInventory().getItem(event.getHotbarButton());
                    if (item == null) return;
                    if (db.isBanned((Player) event.getWhoClicked(), item, BanOption.WEAR)) event.setCancelled(true);
                }
            }, pl, true);
        }

        if (blacklist.contains(BanOption.DROP) || whitelist) {
            pl.getServer().getPluginManager().registerEvent(PlayerDropItemEvent.class, l, ep, (li, e) -> {
                final PlayerDropItemEvent event = (PlayerDropItemEvent) e;
                if (db.isBanned(event.getPlayer(), event.getItemDrop().getItemStack(), BanOption.DROP)) event.setCancelled(true);
            }, pl, true);
        }

        if (blacklist.contains(BanOption.DISPENSE) || whitelist) {
            pl.getServer().getPluginManager().registerEvent(BlockDispenseEvent.class, l, ep, (li, e) -> {
                final BlockDispenseEvent event = (BlockDispenseEvent) e;
                if (db.isBanned(event.getBlock().getWorld().getName(), event.getItem(), BanOption.DISPENSE)) event.setCancelled(true);
            }, pl, true);
        }

        if (blacklist.contains(BanOption.CRAFT) || whitelist) {
            pl.getServer().getPluginManager().registerEvent(PrepareItemCraftEvent.class, l, ep, (ll, e) -> {
                final PrepareItemCraftEvent event = (PrepareItemCraftEvent) e;
                if (event.getRecipe() == null) return;
                final ItemStack item = event.getRecipe().getResult();
                if (!event.getViewers().isEmpty())
                    if (db.isBanned((Player) event.getViewers().get(0), item, BanOption.CRAFT)) event.getInventory().setResult(null);
            }, pl);
        }

        if (blacklist.contains(BanOption.SMELT) || whitelist) {
            pl.getServer().getPluginManager().registerEvent(FurnaceSmeltEvent.class, l, ep, (ll, e) -> {
                if (!(e instanceof FurnaceSmeltEvent)) return;
                final FurnaceSmeltEvent event = (FurnaceSmeltEvent) e;
                final ItemStack item = event.getSource();
                final Furnace f = (Furnace) event.getBlock().getState();
                if (db.isBanned(f.getWorld().getName(), item, BanOption.SMELT)) {
                    if (!f.getInventory().getViewers().isEmpty())
                        if (!db.isBanned((Player) f.getInventory().getViewers().get(0), item, BanOption.SMELT)) return;
                    event.setCancelled(true);
                }
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

        // Remove blacklisted items
        if (blacklist.contains(BanOption.DELETE)) {
            pl.getServer().getPluginManager().registerEvent(InventoryOpenEvent.class, l, ep, (ll, e) -> {
                final InventoryOpenEvent event = (InventoryOpenEvent) e;
                final String world = event.getPlayer().getWorld().getName();
                final Inventory[] invs = { event.getView().getTopInventory(), event.getView().getBottomInventory() };
                deleteItemFromInventory(pl, world, invs);
            }, pl);
            pl.getServer().getPluginManager().registerEvent(InventoryCloseEvent.class, l, ep, (ll, e) -> {
                final InventoryCloseEvent event = (InventoryCloseEvent) e;
                final String world = event.getPlayer().getWorld().getName();
                final Inventory[] invs = { event.getView().getTopInventory(), event.getView().getBottomInventory() };
                deleteItemFromInventory(pl, world, invs);
            }, pl);
        }
    }
}
