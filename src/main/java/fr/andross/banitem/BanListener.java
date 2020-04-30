package fr.andross.banitem;

import fr.andross.banitem.Options.BanData;
import fr.andross.banitem.Options.BanDataType;
import fr.andross.banitem.Options.BanOption;
import fr.andross.banitem.Utils.Ban.BanVersion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Set;

/**
 * <p>This class is used to register only the needed listeners.
 * {@link BanListener#loadListeners()} should be called everytimes that a option is added/removed
 * into a map <i>(blacklist or whitelist)</i></p>
 * @version 2.0.1
 * @author Andross
 */
public final class BanListener {
    private static final Listener listener = new Listener() {};

    /**
     * (re)Load the listeners for the current instance of the plugin
     */
    public static void loadListeners() {
        // Preparing variables
        final BanItem pl = BanItem.getInstance();
        final BanItemAPI api = pl.getApi();
        final BanDatabase db = pl.getBanDatabase();
        final BanUtils utils = pl.getUtils();
        final Set<BanOption> blacklist = db.getBlacklistOptions();
        final boolean whitelist = db.isWhitelistEnabled();

        // Preparing listener
        HandlerList.unregisterAll(pl);
        final PluginManager pm = pl.getServer().getPluginManager();
        final EventPriority ep = EventPriority.NORMAL;

        // Registering listeners, only if option is used
        if (blacklist.contains(BanOption.ARMORSTANDPLACE) || whitelist) {
            if (!BanVersion.v8OrMore) {
                if (!whitelist) pl.getLogger().warning("Can not use the 'armorstandplace' option in Minecraft < 1.8");
            } else
                pm.registerEvent(PlayerArmorStandManipulateEvent.class, listener, ep, (li, event) -> {
                    final PlayerArmorStandManipulateEvent e = (PlayerArmorStandManipulateEvent) event;
                    if (utils.isNullOrAir(e.getPlayerItem())) return; // nothing to place
                    if (api.isBanned(e.getPlayer(), e.getPlayerItem(), BanOption.ARMORSTANDPLACE)) e.setCancelled(true);
                }, pl, true);
        }

        if (blacklist.contains(BanOption.ARMORSTANDTAKE) || whitelist) {
            if (!BanVersion.v8OrMore) {
                if (!whitelist) pl.getLogger().warning("Can not use the 'armorstandtake' option in Minecraft < 1.8");
            } else
                pm.registerEvent(PlayerArmorStandManipulateEvent.class, new Listener() {}, ep, (li, event) -> {
                    final PlayerArmorStandManipulateEvent e = (PlayerArmorStandManipulateEvent) event;
                    if (e.getArmorStandItem().getType() == Material.AIR) return;
                    if (api.isBanned(e.getPlayer(), e.getArmorStandItem(), BanOption.ARMORSTANDTAKE))
                        e.setCancelled(true);
                }, pl, true);
        }

        if (blacklist.contains(BanOption.ATTACK) || whitelist) {
            pm.registerEvent(EntityDamageByEntityEvent.class, listener, ep, (li, event) -> {
                if (!(event instanceof EntityDamageByEntityEvent)) return; // this event is called even for EntityDamageByBlockEvent. Weird?
                final EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
                if (!(e.getDamager() instanceof Player)) return;
                final Player damager = (Player) e.getDamager();
                final ItemStack itemInHand = utils.getItemInHand(damager);
                if (api.isBanned(damager, itemInHand, BanOption.ATTACK, new BanData(BanDataType.ENTITY, e.getEntityType()))) e.setCancelled(true);
            }, pl);
        }

        if (blacklist.contains(BanOption.BREAK) || whitelist) {
            pm.registerEvent(PlayerInteractEvent.class, listener, EventPriority.LOW, (li, event) -> {
                final PlayerInteractEvent e = (PlayerInteractEvent) event;
                if (BanVersion.v9OrMore && e.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
                if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null) {
                    final ItemStack itemInHand = utils.getItemInHand(e.getPlayer());
                    if (api.isBanned(e.getPlayer(), e.getClickedBlock().getType(), BanOption.BREAK, new BanData(BanDataType.MATERIAL, itemInHand.getType()))) {
                        e.setCancelled(true);
                        if (BanVersion.v8) e.getPlayer().updateInventory();
                    }
                }
            }, pl, true);
        }

        if (blacklist.contains(BanOption.BREW) || whitelist) {
            pm.registerEvent(BrewEvent.class, listener, ep, (li, event) -> {
                final BrewEvent e = (BrewEvent) event;

                // Checking all items
                Bukkit.getScheduler().runTask(pl, () -> {
                    final BrewerInventory inv = e.getContents();
                    for (int i = 0; i < 3; i++) {
                        final ItemStack item = inv.getItem(i);
                        if (utils.isNullOrAir(item)) continue;
                        if (api.isBanned(e.getBlock().getWorld(), item, BanOption.BREW)) {
                            if (!inv.getViewers().isEmpty())
                                if (!api.isBanned((Player) inv.getViewers().get(0), item, BanOption.BREW)) continue;
                            inv.setItem(i, null);
                        }
                    }
                });
            }, pl);
        }

        if (blacklist.contains(BanOption.CLICK) || whitelist) {
            pm.registerEvent(PlayerInteractEvent.class, listener, ep, (li, event) -> {
                final PlayerInteractEvent e = (PlayerInteractEvent) event;
                if (BanVersion.v9OrMore && e.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
                if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) {
                    final ItemStack itemInHand = utils.getItemInHand(e.getPlayer());
                    if (e.getClickedBlock() != null) {
                        if (api.isBanned(e.getPlayer(), itemInHand, BanOption.CLICK, new BanData(BanDataType.MATERIAL, e.getClickedBlock().getType()))) {
                            e.setCancelled(true);
                            if (BanVersion.v8) e.getPlayer().updateInventory();
                        }
                    } else {
                        if (api.isBanned(e.getPlayer(), itemInHand, BanOption.CLICK)) {
                            e.setCancelled(true);
                            if (BanVersion.v8) e.getPlayer().updateInventory();
                        }
                    }
                }
            }, pl, true);
        }

        if (blacklist.contains(BanOption.CONSUME) || whitelist) {
            pm.registerEvent(PlayerItemConsumeEvent.class, listener, ep, (li, event) -> {
                final PlayerItemConsumeEvent e = (PlayerItemConsumeEvent) event;
                if (utils.isNullOrAir(e.getItem())) return;
                if (api.isBanned(e.getPlayer(), e.getItem(), BanOption.CONSUME)) e.setCancelled(true);
            }, pl);
        }

        if (blacklist.contains(BanOption.CRAFT) || whitelist) {
            pm.registerEvent(PrepareItemCraftEvent.class, listener, ep, (ll, event) -> {
                final PrepareItemCraftEvent e = (PrepareItemCraftEvent) event;
                if (e.getRecipe() == null) return;
                final ItemStack item = e.getRecipe().getResult();
                if (!e.getViewers().isEmpty())
                    if (api.isBanned((Player) e.getViewers().get(0), item, BanOption.CRAFT)) e.getInventory().setResult(null);
            }, pl);
        }

        if (blacklist.contains(BanOption.DELETE)) {
            pm.registerEvent(InventoryOpenEvent.class, listener, ep, (ll, event) -> {
                final InventoryOpenEvent e = (InventoryOpenEvent) event;
                utils.deleteItemFromInventory(((Player) e.getPlayer()), e.getView().getTopInventory(), e.getView().getBottomInventory());
            }, pl);
            pm.registerEvent(InventoryCloseEvent.class, listener, ep, (ll, event) -> {
                final InventoryCloseEvent e = (InventoryCloseEvent) event;
                utils.deleteItemFromInventory(((Player) e.getPlayer()), e.getView().getTopInventory(), e.getView().getBottomInventory());
            }, pl);
        }

        if (blacklist.contains(BanOption.DISPENSE) || whitelist) {
            pm.registerEvent(BlockDispenseEvent.class, listener, ep, (li, event) -> {
                final BlockDispenseEvent e = (BlockDispenseEvent) event;
                if (api.isBanned(e.getBlock().getWorld(), e.getItem(), BanOption.DISPENSE)) e.setCancelled(true);
            }, pl, true);
        }

        if (blacklist.contains(BanOption.DROP) || whitelist) {
            pm.registerEvent(PlayerDropItemEvent.class, listener, ep, (li, event) -> {
                final PlayerDropItemEvent e = (PlayerDropItemEvent) event;
                if (api.isBanned(e.getPlayer(), e.getItemDrop().getItemStack(), BanOption.DROP)) e.setCancelled(true);
            }, pl);
        }

        if (blacklist.contains(BanOption.DROPS) || whitelist) {
            pm.registerEvent(BlockBreakEvent.class, listener, ep, (li, event) -> {
                if (!(event instanceof BlockBreakEvent)) return; // also called for FurnaceExtractEvent...
                final BlockBreakEvent e = (BlockBreakEvent) event;
                final ItemStack itemInHand = utils.getItemInHand(e.getPlayer());
                if (e.getBlock().getDrops(itemInHand).stream().anyMatch(item -> api.isBanned(e.getPlayer(), item, BanOption.DROPS, new BanData(BanDataType.MATERIAL, itemInHand.getType()))))
                    e.setDropItems(false);
            }, pl);
        }

        if (blacklist.contains(BanOption.ENTITYDROP) || whitelist) {
            pm.registerEvent(EntityDeathEvent.class, listener, ep, (li, event) -> {
                final EntityDeathEvent e = (EntityDeathEvent) event;
                final Iterator<ItemStack> it = e.getDrops().iterator();
                while (it.hasNext()) {
                    if (api.isBanned(e.getEntity().getWorld(), it.next(), BanOption.ENTITYDROP, new BanData(BanDataType.ENTITY, e.getEntity().getType())))
                        it.remove();;
                }
            }, pl, true);
        }

        if (blacklist.contains(BanOption.ENTITYINTERACT) || whitelist) {
            pm.registerEvent(PlayerInteractEntityEvent.class, listener, ep, (li, event) -> {
                final PlayerInteractEntityEvent e = (PlayerInteractEntityEvent) event;
                if (BanVersion.v9OrMore && e.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
                if (api.isBanned(e.getPlayer(), utils.getItemInHand(e.getPlayer()), BanOption.ENTITYINTERACT, new BanData(BanDataType.ENTITY, e.getRightClicked().getType()))) e.setCancelled(true);
            }, pl, true);
        }

        if (blacklist.contains(BanOption.GLIDE) || whitelist) {
            if (!BanVersion.v9OrMore) {
                if (!whitelist) pl.getLogger().warning("Can not use the 'glide' option in Minecraft < 1.9");
            } else {
                pm.registerEvent(org.bukkit.event.entity.EntityToggleGlideEvent.class, listener, ep, (li, event) -> {
                    final org.bukkit.event.entity.EntityToggleGlideEvent e = (org.bukkit.event.entity.EntityToggleGlideEvent) event;
                    if (!(e.getEntity() instanceof Player)) return;
                    final Player p = (Player) e.getEntity();
                    final EntityEquipment ee = p.getEquipment();
                    if (ee == null) return;
                    final ItemStack item = ee.getChestplate();
                    if (item == null) return;
                    if (api.isBanned(p, item, BanOption.GLIDE)) {
                        p.setGliding(false);
                        p.setSneaking(true);

                        // Removing the elytra from player Inventory, to prevent any glitch
                        Bukkit.getScheduler().runTask(pl, () -> {
                            if (!p.isOnline()) return;
                            p.setGliding(false);
                            p.setSneaking(true);

                            // Already removed?
                            final ItemStack chestplate = ee.getChestplate();
                            if (utils.isNullOrAir(chestplate)) return;

                            // Put chestplate in inventory
                            final int freeSlot = p.getInventory().firstEmpty();
                            if (freeSlot == -1) { // no empty space, dropping it
                                p.getWorld().dropItemNaturally(p.getLocation(), chestplate);
                                ee.setChestplate(null);
                                return;
                            }

                            // Putting it back in inventory
                            p.getInventory().setItem(freeSlot, chestplate);
                            ee.setChestplate(null);
                        });
                    }
                }, pl);
            }
        }

        if (blacklist.contains(BanOption.HANGINGPLACE) || whitelist) {
            pm.registerEvent(HangingPlaceEvent.class, listener, ep, (li, event) -> {
                final HangingPlaceEvent e = (HangingPlaceEvent) event;
                if (e.getPlayer() == null) return;
                final ItemStack item = utils.getItemInHand(e.getPlayer());
                if (api.isBanned(e.getPlayer(), item, BanOption.HANGINGPLACE, new BanData(BanDataType.ENTITY, e.getEntity().getType()))) e.setCancelled(true);
            }, pl, true);
        }

        if (blacklist.contains(BanOption.INTERACT) || whitelist) {
            pm.registerEvent(PlayerInteractEvent.class, listener, ep, (li, event) -> {
                final PlayerInteractEvent e = (PlayerInteractEvent) event;
                if (BanVersion.v9OrMore && e.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
                if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    final ItemStack item = utils.getItemInHand(e.getPlayer());
                    if (api.isBanned(e.getPlayer(), e.getClickedBlock().getType(), BanOption.INTERACT, new BanData(BanDataType.MATERIAL, item.getType()))) {
                        if (BanVersion.v8) e.getPlayer().updateInventory();
                        e.setCancelled(true);
                    }
                }
            }, pl, true);
        }

        if (blacklist.contains(BanOption.PICKUP) || whitelist) {
            // Pickup cooldown map clearing
            pm.registerEvent(PlayerQuitEvent.class, listener, ep, (li, event) -> utils.getPickupCooldowns().remove(((PlayerQuitEvent) event).getPlayer().getUniqueId()), pl);

            // >=1.12: EntityPickupItemEvent
            // <1.12: PlayerPickupItemEvent
            final EventExecutor ee;
            final Class<? extends Event> clazz;
            if (BanVersion.v12OrMore) {
                clazz = org.bukkit.event.entity.EntityPickupItemEvent.class;
                ee = (li, event) -> {
                    final org.bukkit.event.entity.EntityPickupItemEvent e = (org.bukkit.event.entity.EntityPickupItemEvent) event;
                    if (!(e.getEntity() instanceof Player)) return;
                    if (api.isBanned((Player) e.getEntity(), e.getItem().getItemStack(), BanOption.PICKUP)) e.setCancelled(true);
                };
            } else {
                clazz = org.bukkit.event.player.PlayerPickupItemEvent.class;
                ee = (li, event) -> {
                    final org.bukkit.event.player.PlayerPickupItemEvent e = (org.bukkit.event.player.PlayerPickupItemEvent) event;
                    if (api.isBanned(e.getPlayer(), e.getItem().getItemStack(), BanOption.PICKUP)) e.setCancelled(true);
                };
            }
            pm.registerEvent(clazz, listener, ep, ee, pl, true);
        }

        if (blacklist.contains(BanOption.PLACE) || whitelist) {
            pm.registerEvent(PlayerInteractEvent.class, listener, ep, (li, event) -> {
                final PlayerInteractEvent e = (PlayerInteractEvent) event;
                if (BanVersion.v9OrMore && e.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
                if (utils.isNullOrAir(e.getItem())) return;
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
                    if (e.getClickedBlock() != null) {
                        if (api.isBanned(e.getPlayer(), e.getItem(), BanOption.PLACE, new BanData(BanDataType.MATERIAL, e.getClickedBlock().getType()))) {
                            e.setCancelled(true);
                            if (BanVersion.v8) e.getPlayer().updateInventory();
                        }
                    } else {
                        if (api.isBanned(e.getPlayer(), e.getItem(), BanOption.PLACE)) {
                            e.setCancelled(true);
                            if (BanVersion.v8) e.getPlayer().updateInventory();
                        }
                    }
                }
            }, pl);
        }

        if (blacklist.contains(BanOption.SMELT) || whitelist) {
            pm.registerEvent(FurnaceSmeltEvent.class, listener, ep, (ll, event) -> {
                if (!(event instanceof FurnaceSmeltEvent)) return;
                final FurnaceSmeltEvent e = (FurnaceSmeltEvent) event;
                final ItemStack item = e.getSource();
                final Furnace f = (Furnace) e.getBlock().getState();
                if (api.isBanned(f.getWorld(), item, BanOption.SMELT)) {
                    if (!f.getInventory().getViewers().isEmpty())
                        if (!api.isBanned((Player) f.getInventory().getViewers().get(0), item, BanOption.SMELT)) return;
                    e.setCancelled(true);
                }
            }, pl, true);
        }

        if (blacklist.contains(BanOption.SWAP) || whitelist) {
            if (!BanVersion.v9OrMore) {
                if (!whitelist) pl.getLogger().warning("Can not use the 'swap' option in Minecraft < 1.9");
            } else {
                pm.registerEvent(org.bukkit.event.player.PlayerSwapHandItemsEvent.class, listener, ep, (li, event) -> {
                    final org.bukkit.event.player.PlayerSwapHandItemsEvent e = (org.bukkit.event.player.PlayerSwapHandItemsEvent) event;
                    if (e.getMainHandItem() != null && api.isBanned(e.getPlayer(), e.getMainHandItem(), BanOption.SWAP)) {
                        e.setCancelled(true);
                        return;
                    }
                    if (e.getOffHandItem() != null && api.isBanned(e.getPlayer(), e.getOffHandItem(), BanOption.SWAP)) e.setCancelled(true);
                }, pl, true);
            }
        }

        if (blacklist.contains(BanOption.TRANSFER) || whitelist) {
            // Clicking
            pm.registerEvent(InventoryClickEvent.class, listener, ep, (li, event) -> {
                final InventoryClickEvent e = (InventoryClickEvent) event;
                if (e.getClickedInventory() == null) return;

                final Player p = (Player) e.getWhoClicked();
                final Inventory top = e.getView().getTopInventory();
                final Inventory bottom = e.getView().getBottomInventory();

                if (top.getType() != InventoryType.CRAFTING && e.getClick() == ClickType.DOUBLE_CLICK) { // Trying to get all items for a banned one?
                    final ItemStack item = e.getCursor();
                    if (!utils.isNullOrAir(item))
                        if (api.isBanned(p, item, BanOption.TRANSFER)) {
                            e.setCancelled(true);
                            return;
                        }
                }

                if (e.getClickedInventory().equals(bottom)) { // Player Inventory clicked
                    if (e.isShiftClick() && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        final ItemStack item = e.getCurrentItem();
                        if (utils.isNullOrAir(item)) return;
                        // Banned?
                        if (api.isBanned(p, item, BanOption.TRANSFER, new BanData(BanDataType.INVENTORY_FROM, bottom.getType()), new BanData(BanDataType.INVENTORY_TO, top.getType())))
                            e.setCancelled(true);
                    }
                } else { // Top container clicked
                    // Shift
                    if (e.isShiftClick() && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        final ItemStack item = e.getCurrentItem();
                        if (utils.isNullOrAir(item)) return;
                        if (api.isBanned(p, item, BanOption.TRANSFER, new BanData(BanDataType.INVENTORY_FROM, top.getType()), new BanData(BanDataType.INVENTORY_TO, bottom.getType())))
                            e.setCancelled(true);
                    } else {
                        // Hot bar click?
                        if (e.getHotbarButton() > -1) {
                            final ItemStack hotBarItem = bottom.getItem(e.getHotbarButton());
                            if (!utils.isNullOrAir(hotBarItem) && api.isBanned(p, hotBarItem, BanOption.TRANSFER, new BanData(BanDataType.INVENTORY_FROM, bottom.getType()))) {
                                e.setCancelled(true);
                                return;
                            }

                            // Trying to swap with an item from the top inventory?
                            final ItemStack item = top.getItem(e.getRawSlot());
                            if (!utils.isNullOrAir(item) && api.isBanned(p, item, BanOption.TRANSFER, new BanData(BanDataType.INVENTORY_TO, bottom.getType()))) {
                                e.setCancelled(true);
                                return;
                            }
                        }

                        // Normal click
                        final ItemStack clickedItem = e.getCurrentItem();
                        final ItemStack cursorItem = e.getCursor();

                        // Trying to place the cursor item?
                        if (!utils.isNullOrAir(cursorItem)) {
                            if (api.isBanned(p, cursorItem, BanOption.TRANSFER, new BanData(BanDataType.INVENTORY_FROM, bottom.getType()), new BanData(BanDataType.INVENTORY_TO, top.getType()))) {
                                e.setCancelled(true);
                                return;
                            }
                        }

                        // Trying to get the item from top?
                        if (!utils.isNullOrAir(clickedItem)) {
                            if (api.isBanned(p, clickedItem, BanOption.TRANSFER, new BanData(BanDataType.INVENTORY_FROM, top.getType()), new BanData(BanDataType.INVENTORY_TO, bottom.getType())))
                                e.setCancelled(true);
                        }
                    }
                }

            }, pl, true);

            // Dragging
            pm.registerEvent(InventoryDragEvent.class, listener, ep, (li, event) -> {
                final InventoryDragEvent e = (InventoryDragEvent) event;
                final Player p = (Player) e.getWhoClicked();
                // In is own inventory?
                if (e.getView().getTopInventory().getType() == InventoryType.CRAFTING) return;
                if (utils.isNullOrAir(e.getOldCursor())) return;
                if (api.isBanned(p, e.getOldCursor(), BanOption.TRANSFER))
                    e.setCancelled(true);
            }, pl, true);

            // Hoppers block?
            if (pl.getBanConfig().isHoppersBlock())
                pm.registerEvent(InventoryMoveItemEvent.class, listener, ep, (li, event) -> {
                    final InventoryMoveItemEvent e = (InventoryMoveItemEvent) event;
                    if (e.getSource().getHolder() instanceof BlockState) {
                        final BlockState bs = (BlockState) e.getSource().getHolder();
                        if (api.isBanned(bs.getWorld(), e.getItem(), BanOption.TRANSFER,
                                new BanData(BanDataType.INVENTORY_FROM, e.getSource().getType()),
                                new BanData(BanDataType.INVENTORY_TO, e.getDestination().getType())))
                            e.setCancelled(true);
                    }
                }, pl, true);
        }

        if (blacklist.contains(BanOption.WEAR) || whitelist) {
            pm.registerEvent(InventoryClickEvent.class, listener, ep, (li, event) -> {
                final InventoryClickEvent e = (InventoryClickEvent) event;
                if (e.getInventory().getType() != InventoryType.PLAYER && e.getInventory().getType() != InventoryType.CRAFTING) return;

                // Trying to put item on cursor to armor?
                if (e.getRawSlot() >= 5 && e.getRawSlot() <= 8) {
                    if (e.getCursor() != null) {
                        if (api.isBanned((Player) e.getWhoClicked(), e.getCursor(), BanOption.WEAR)) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }

                // Trying to shift click item to armor?
                if (!utils.isNullOrAir(e.getCurrentItem()) && e.isShiftClick() && !(e.getRawSlot() >= 5 && e.getRawSlot() <= 8)) {
                    if (api.isBanned((Player) e.getWhoClicked(), e.getCurrentItem(), BanOption.WEAR)) {
                        e.setCancelled(true);
                        return;
                    }
                }

                // Trying to use hotbar button?
                if (e.getRawSlot() >= 5 && e.getRawSlot() <= 8 && e.getHotbarButton() > -1) {
                    final ItemStack item = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                    if (utils.isNullOrAir(item)) return;
                    if (api.isBanned((Player) e.getWhoClicked(), item, BanOption.WEAR)) e.setCancelled(true);
                }
            }, pl, true);

            pm.registerEvent(PlayerChangedWorldEvent.class, listener, ep, (li, event) -> {
                final PlayerChangedWorldEvent e = (PlayerChangedWorldEvent) event;

                // Checking wearable items
                final Player p = e.getPlayer();
                final EntityEquipment ee = p.getEquipment();
                if (ee == null) return;

                final ItemStack[] items = new ItemStack[] { ee.getHelmet(), ee.getChestplate(), ee.getLeggings(), ee.getBoots() };
                int i = -1;

                for (final ItemStack item : items) {
                    i++;
                    if (utils.isNullOrAir(item)) continue;
                    if (!api.isBanned(p, item, BanOption.WEAR)) continue;

                    // Item can not be weared in this world
                    switch (i) {
                        case 0:
                            ee.setHelmet(null);
                            break;
                        case 1:
                            ee.setChestplate(null);
                            break;
                        case 2:
                            ee.setLeggings(null);
                            break;
                        case 3:
                            ee.setBoots(null);
                            break;
                        default:
                            break;
                    }
                    final int freeSlot = p.getInventory().firstEmpty();
                    // No empty space, dropping it, else adding it into inventory
                    if (freeSlot == -1) p.getWorld().dropItemNaturally(p.getLocation(), item);
                    else p.getInventory().setItem(freeSlot, item);
                }
            }, pl);
        }
    }

    @NotNull
    public static Listener getListener() {
        return listener;
    }
}
