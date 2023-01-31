/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2021 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your action) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.andross.banitem;

import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanData;
import fr.andross.banitem.actions.BanDataType;
import fr.andross.banitem.events.PlayerRegionChangeEvent;
import fr.andross.banitem.utils.BanVersion;
import fr.andross.banitem.utils.Chat;
import fr.andross.banitem.utils.ItemStackBuilder;
import fr.andross.banitem.utils.Utils;
import fr.andross.banitem.utils.enchantments.EnchantmentWrapper;
import fr.andross.banitem.utils.hooks.IWorldGuardHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>This class is used to register only the needed listeners.
 * The listeners should be refreshed everytime you manually add/remove an action
 * from a map <i>(blacklist or whitelist)</i></p>
 * We are ignoring the deprecation warning, as these methods are used across multiple Bukkit version.
 *
 * @author Andross
 * @version 3.3.2
 */
@SuppressWarnings("deprecation")
public final class BanListener {
    private final BanItem pl;
    private final Listener listener = new Listener() {};
    private int activated = 0;

    BanListener(@NotNull final BanItem pl) {
        this.pl = pl;
    }

    /**
     * (re)Loading the listeners needed for blacklist and whitelist
     *
     * @param sender the sender who executed, for debug
     */
    public void load(@NotNull final CommandSender sender) {
        // Preparing variables
        final BanItemAPI api = pl.getApi();
        final BanDatabase db = pl.getBanDatabase();
        final Set<BanAction> priority = pl.getBanConfig().getPriority();
        final Set<BanAction> blacklist = db.getBlacklistActions();
        final boolean whitelist = !db.getWhitelist().isEmpty();
        final boolean all = blacklist.size() == BanAction.values().length; // check if a '*' is used, if so, do not notify for version uncompatibility
        HandlerList.unregisterAll(pl);
        activated = 0;

        // Registering listeners, only if action is used
        if (blacklist.contains(BanAction.ARMORSTANDPLACE) || whitelist) {
            if (!BanVersion.v8OrMore) {
                if (!all && !whitelist) // notifying if used an action unavailable on the current minecraft version
                    sender.sendMessage(Chat.color("&cCan not use the '&earmorstandplace&c' action in Minecraft < 1.8."));
            } else
                registerEvent(PlayerArmorStandManipulateEvent.class, (li, event) -> {
                    final PlayerArmorStandManipulateEvent e = (PlayerArmorStandManipulateEvent) event;
                    if (Utils.isNullOrAir(e.getPlayerItem())) return; // nothing to place
                    if (api.isBanned(e.getPlayer(), e.getRightClicked().getLocation(), e.getPlayerItem(), true, BanAction.ARMORSTANDPLACE))
                        e.setCancelled(true);
                }, priority.contains(BanAction.ARMORSTANDPLACE));
        }

        if (blacklist.contains(BanAction.ARMORSTANDTAKE) || whitelist) {
            if (!BanVersion.v8OrMore) {
                if (!all && !whitelist) // notifying if used an action unavailable on the current minecraft version
                    sender.sendMessage(Chat.color("&cCan not use the '&earmorstandtake&c' action in Minecraft < 1.8."));
            } else
                registerEvent(PlayerArmorStandManipulateEvent.class, (li, event) -> {
                    final PlayerArmorStandManipulateEvent e = (PlayerArmorStandManipulateEvent) event;
                    if (e.getArmorStandItem().getType() == Material.AIR) return;
                    if (api.isBanned(e.getPlayer(), e.getRightClicked().getLocation(), e.getArmorStandItem(), true, BanAction.ARMORSTANDTAKE))
                        e.setCancelled(true);
                }, priority.contains(BanAction.ARMORSTANDTAKE));
        }

        if (blacklist.contains(BanAction.ATTACK) || whitelist) {
            registerEvent(EntityDamageByEntityEvent.class, (li, event) -> {
                if (!(event instanceof EntityDamageByEntityEvent))
                    return; // this event is called even for EntityDamageByBlockEvent. Weird?
                final EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
                if (!(e.getDamager() instanceof Player)) return;
                final Player damager = (Player) e.getDamager();
                final ItemStack itemInHand = Utils.getItemInHand(damager);
                if (api.isBanned(damager, e.getEntity().getLocation(), itemInHand, true, BanAction.ATTACK, new BanData(BanDataType.ENTITY, e.getEntityType())))
                    e.setCancelled(true);
            }, priority.contains(BanAction.ATTACK));
        }

        if (blacklist.contains(BanAction.BOOKEDIT) || whitelist) {
            registerEvent(PlayerEditBookEvent.class, (li, event) -> {
                final PlayerEditBookEvent e = (PlayerEditBookEvent) event;
                if (api.isBanned(e.getPlayer(), Utils.getItemInHand(e.getPlayer()), true, BanAction.BOOKEDIT)) {
                    e.setCancelled(true);
                    e.setNewBookMeta(e.getPreviousBookMeta());
                }
            }, priority.contains(BanAction.BOOKEDIT));
        }

        if (blacklist.contains(BanAction.BREAK) || whitelist) {
            registerEvent(PlayerInteractEvent.class, (li, event) -> {
                final PlayerInteractEvent e = (PlayerInteractEvent) event;
                if (e.useInteractedBlock() == Event.Result.DENY || e.useItemInHand() == Event.Result.DENY) return;
                if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null) {
                    final ItemStack itemInHand = Utils.getItemInHand(e.getPlayer());
                    if (api.isBanned(e.getPlayer(), e.getClickedBlock().getLocation(), e.getClickedBlock().getType(), true, BanAction.BREAK, new BanData(BanDataType.MATERIAL, itemInHand.getType()))) {
                        e.setCancelled(true);
                        if (!BanVersion.v12OrMore) e.getPlayer().updateInventory();
                    }
                }
            }, priority.contains(BanAction.BREAK));

            registerEvent(BlockBreakEvent.class, (li, event) -> {
                if (!(event instanceof BlockBreakEvent)) return; // // also called for FurnaceExtractEvent...
                final BlockBreakEvent e = (BlockBreakEvent) event;
                final ItemStack itemInHand = Utils.getItemInHand(e.getPlayer());
                if (api.isBanned(e.getPlayer(), e.getBlock().getLocation(), e.getBlock().getType(), true, BanAction.BREAK, new BanData(BanDataType.MATERIAL, itemInHand.getType()))) {
                    e.setCancelled(true);
                    if (!BanVersion.v12OrMore) e.getPlayer().updateInventory();
                }
            }, priority.contains(BanAction.BREAK));
        }

        if (blacklist.contains(BanAction.BREW) || whitelist) {
            registerEvent(BrewEvent.class, (li, event) -> {
                final BrewEvent e = (BrewEvent) event;
                final ItemStack ingridient = e.getContents().getIngredient() == null ? null : e.getContents().getIngredient().clone();
                final List<ItemStack> items = new ArrayList<>();
                for (final ItemStack item : e.getContents().getContents()) {
                    if (Utils.isNullOrAir(item)) items.add(null);
                    else items.add(item.clone());
                }
                Bukkit.getScheduler().runTask(pl, () -> {
                    final BrewerInventory inv = e.getContents();
                    for (int i = 0; i < 3; i++) {
                        final ItemStack item = inv.getItem(i);
                        if (Utils.isNullOrAir(item)) continue;
                        if (api.isBanned(e.getBlock().getWorld(), item, BanAction.BREW)) {
                            if (!inv.getViewers().isEmpty())
                                if (!api.isBanned((Player) inv.getViewers().get(0), e.getBlock().getLocation(), item, true, BanAction.BREW))
                                    continue;
                            inv.setItem(i, items.get(i));
                            inv.setIngredient(ingridient);
                        }
                    }
                });
            }, priority.contains(BanAction.BREW));
        }

        if (blacklist.contains(BanAction.CLICK) || whitelist) {
            registerEvent(PlayerInteractEvent.class, (li, event) -> {
                final PlayerInteractEvent e = (PlayerInteractEvent) event;
                if (e.useItemInHand() == Event.Result.DENY || ((e.useInteractedBlock() == Event.Result.DENY) && e.getAction() != Action.LEFT_CLICK_AIR))
                    return;
                if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) {
                    final ItemStack itemInHand = Utils.getItemInHand(e.getPlayer());
                    if (e.getClickedBlock() != null) {
                        if (api.isBanned(e.getPlayer(), itemInHand, true, BanAction.CLICK, new BanData(BanDataType.MATERIAL, e.getClickedBlock().getType()))) {
                            e.setCancelled(true);
                            if (!BanVersion.v12OrMore) e.getPlayer().updateInventory();
                        }
                    } else {
                        if (api.isBanned(e.getPlayer(), itemInHand, true, BanAction.CLICK)) {
                            e.setCancelled(true);
                            if (!BanVersion.v12OrMore) e.getPlayer().updateInventory();
                        }
                    }
                }
            }, priority.contains(BanAction.CLICK));
        }

        if (blacklist.contains(BanAction.CONSUME) || whitelist) {
            registerEvent(PlayerItemConsumeEvent.class, (li, event) -> {
                final PlayerItemConsumeEvent e = (PlayerItemConsumeEvent) event;
                if (Utils.isNullOrAir(e.getItem())) return;
                if (api.isBanned(e.getPlayer(), e.getItem(), true, BanAction.CONSUME)) {
                    e.setCancelled(true);
                    if (!BanVersion.v12OrMore) e.getPlayer().updateInventory();
                }
            }, priority.contains(BanAction.CONSUME));
        }

        if (blacklist.contains(BanAction.CRAFT) || whitelist) {
            registerEvent(CraftItemEvent.class, (ll, event) -> {
                if (!(event instanceof CraftItemEvent)) return;
                final CraftItemEvent e = (CraftItemEvent) event;
                final ItemStack item = e.getInventory().getResult();
                if (item == null) return;
                if (!e.getViewers().isEmpty()) {
                    final Player p = (Player) e.getViewers().get(0);
                    if (api.isBanned(p, item, true, BanAction.CRAFT))
                        e.getInventory().setResult(null);
                }
            }, priority.contains(BanAction.CRAFT));
        }

        if (blacklist.contains(BanAction.DELETE)) {
            registerEvent(InventoryOpenEvent.class, (ll, event) -> {
                final InventoryOpenEvent e = (InventoryOpenEvent) event;
                pl.getUtils().deleteItemFromInventoryView((Player) e.getPlayer());
            }, priority.contains(BanAction.DELETE));
            registerEvent(InventoryCloseEvent.class, (ll, event) -> {
                final InventoryCloseEvent e = (InventoryCloseEvent) event;
                pl.getUtils().deleteItemFromInventoryView((Player) e.getPlayer());
            }, priority.contains(BanAction.DELETE));
        }

        if (blacklist.contains(BanAction.DISPENSE) || whitelist) {
            registerEvent(BlockDispenseEvent.class, (li, event) -> {
                final BlockDispenseEvent e = (BlockDispenseEvent) event;
                if (api.isBanned(e.getBlock().getWorld(), e.getItem(), BanAction.DISPENSE)) e.setCancelled(true);
            }, priority.contains(BanAction.DISPENSE));
        }

        if (blacklist.contains(BanAction.DROP) || whitelist) {
            registerEvent(PlayerDropItemEvent.class, (li, event) -> {
                final PlayerDropItemEvent e = (PlayerDropItemEvent) event;
                if (api.isBanned(e.getPlayer(), e.getItemDrop().getItemStack(), true, BanAction.DROP))
                    e.setCancelled(true);
            }, priority.contains(BanAction.DROP));
        }

        if (blacklist.contains(BanAction.DROPS) || whitelist) {
            if (BanVersion.v13OrMore)
                registerEvent(BlockDropItemEvent.class, (li, event) -> {
                    if (!(event instanceof BlockDropItemEvent)) return; // also called for FurnaceExtractEvent...
                    final BlockDropItemEvent e = (BlockDropItemEvent) event;
                    final ItemStack itemInHand = Utils.getItemInHand(e.getPlayer());
                    e.getItems().removeIf(item -> api.isBanned(e.getPlayer(), e.getBlock().getLocation(), item.getItemStack(), true, BanAction.DROPS, new BanData(BanDataType.MATERIAL, itemInHand.getType())));
                }, priority.contains(BanAction.DROPS));
            else
                registerEvent(BlockBreakEvent.class, (li, event) -> {
                    if (!(event instanceof BlockBreakEvent)) return; // also called for FurnaceExtractEvent...
                    final BlockBreakEvent e = (BlockBreakEvent) event;
                    final ItemStack itemInHand = Utils.getItemInHand(e.getPlayer());
                    if (e.getBlock().getDrops(itemInHand).stream().anyMatch(item -> api.isBanned(e.getPlayer(), e.getBlock().getLocation(), item, true, BanAction.DROPS, new BanData(BanDataType.MATERIAL, itemInHand.getType()))))
                        e.setDropItems(false);
                }, priority.contains(BanAction.DROPS));
        }

        if (blacklist.contains(BanAction.ENCHANT) || whitelist) {
            registerEvent(EnchantItemEvent.class, (li, event) -> {
                if (!(event instanceof EnchantItemEvent)) return;
                final EnchantItemEvent e = (EnchantItemEvent) event;
                if (api.isBanned(e.getEnchanter(), e.getEnchantBlock().getLocation(), e.getItem(), true, BanAction.ENCHANT, new BanData(BanDataType.ENCHANTMENT, EnchantmentWrapper.from(e.getEnchantsToAdd()))))
                    e.setCancelled(true);
            }, priority.contains(BanAction.ENCHANT));

            if (BanVersion.v9OrMore) {
                // Getting denied item
                ItemStack denied;
                try {
                    denied = new ItemStackBuilder(pl.getBanConfig().getConfig().getConfigurationSection("actions.enchant")).build();
                } catch (final Exception e) {
                    pl.getUtils().sendMessage(sender, "&cUnable to load denied item 'actions.enchant' from config: " + e.getMessage());
                    denied = new ItemStack(Material.BARRIER);
                }
                final ItemStack finalDenied = denied;
                registerEvent(PrepareAnvilEvent.class, (li, event) -> {
                    if (!(event instanceof PrepareAnvilEvent)) return;
                    final PrepareAnvilEvent e = (PrepareAnvilEvent) event;
                    final ItemStack item = e.getInventory().getItem(0);
                    if (item == null) return;
                    final ItemStack addedItem = e.getInventory().getItem(1);
                    if (addedItem == null) return;
                    final Map<Enchantment, Integer> enchants = Utils.getAllEnchants(addedItem);
                    if (enchants.isEmpty()) return;

                    // Getting the player
                    if (e.getViewers().size() == 0) return;
                    final Player p = (Player) e.getViewers().get(0);
                    if (api.isBanned(p, item, true, BanAction.ENCHANT, new BanData(BanDataType.ENCHANTMENT, EnchantmentWrapper.from(enchants)))) {
                        e.setResult(finalDenied);
                        e.getInventory().setRepairCost(0);
                    }
                }, priority.contains(BanAction.ENCHANT));
            } else
                registerEvent(InventoryClickEvent.class, (li, event) -> {
                    final InventoryClickEvent e = (InventoryClickEvent) event;
                    if (e.getView().getTopInventory().getType() != InventoryType.ANVIL) return;

                    final ItemStack item = e.getInventory().getItem(0);
                    if (item == null) return;
                    final ItemStack addedItem = e.getInventory().getItem(1);
                    if (addedItem == null) return;
                    final Map<Enchantment, Integer> enchants = Utils.getAllEnchants(addedItem);
                    if (enchants.isEmpty()) return;
                    final Player p = (Player) e.getWhoClicked();
                    if (api.isBanned(p, item, true, BanAction.ENCHANT, new BanData(BanDataType.ENCHANTMENT, EnchantmentWrapper.from(enchants)))) {
                        e.getInventory().setItem(2, null);
                        p.updateInventory();
                        if (e.getRawSlot() == 2) e.setCancelled(true);
                        return;
                    }

                    // Checking also on next tick
                    Bukkit.getScheduler().runTask(pl, () -> {
                        final ItemStack item2 = e.getInventory().getItem(0);
                        if (item2 == null) return;
                        final ItemStack addedItem2 = e.getInventory().getItem(1);
                        if (addedItem2 == null) return;
                        final Map<Enchantment, Integer> enchants2 = Utils.getAllEnchants(addedItem);
                        if (enchants2.isEmpty()) return;
                        if (api.isBanned(p, item, true, BanAction.ENCHANT, new BanData(BanDataType.ENCHANTMENT, EnchantmentWrapper.from(enchants)))) {
                            e.getInventory().setItem(2, null);
                            p.updateInventory();
                            if (e.getRawSlot() == 2) e.setCancelled(true);
                        }
                    });
                }, priority.contains(BanAction.ENCHANT));
        }

        if (blacklist.contains(BanAction.ENTITYDROP) || whitelist) {
            registerEvent(EntityDeathEvent.class, (li, event) -> {
                final EntityDeathEvent e = (EntityDeathEvent) event;
                final Player killer = e.getEntity().getKiller();
                if (killer != null)
                    e.getDrops().removeIf(i -> {
                        if (i == null) return false;
                        return api.isBanned(killer, e.getEntity().getLocation(), i, true, BanAction.ENTITYDROP, new BanData(BanDataType.ENTITY, e.getEntity().getType()));
                    });
                else
                    e.getDrops().removeIf(i -> {
                        if (i == null) return false;
                        return api.isBanned(e.getEntity().getWorld(), i, BanAction.ENTITYDROP, new BanData(BanDataType.ENTITY, e.getEntity().getType()));
                    });
            }, priority.contains(BanAction.ENTITYDROP));
        }

        if (blacklist.contains(BanAction.ENTITYINTERACT) || whitelist) {
            if (BanVersion.v9OrMore)
                registerEvent(PlayerInteractEntityEvent.class, (li, event) -> {
                    final PlayerInteractEntityEvent e = (PlayerInteractEntityEvent) event;
                    final ItemStack used = Utils.getItemInHand(e.getPlayer());
                    if (api.isBanned(e.getPlayer(), e.getRightClicked().getLocation(), used, true, BanAction.ENTITYINTERACT, new BanData(BanDataType.ENTITY, e.getRightClicked().getType())))
                        e.setCancelled(true);
                }, priority.contains(BanAction.ENTITYINTERACT));
            else
                registerEvent(PlayerInteractEntityEvent.class, (li, event) -> {
                    final PlayerInteractEntityEvent e = (PlayerInteractEntityEvent) event;
                    if (api.isBanned(e.getPlayer(), e.getRightClicked().getLocation(), Utils.getItemInHand(e.getPlayer()), true, BanAction.ENTITYINTERACT, new BanData(BanDataType.ENTITY, e.getRightClicked().getType())))
                        e.setCancelled(true);
                }, priority.contains(BanAction.ENTITYINTERACT));
        }

        if (blacklist.contains(BanAction.FILL) || whitelist) {
            registerEvent(PlayerBucketFillEvent.class, (li, event) -> {
                final PlayerBucketFillEvent e = (PlayerBucketFillEvent) event;
                final ItemStack item = Utils.getItemInHand(e.getPlayer());
                if (api.isBanned(e.getPlayer(), e.getBlockClicked().getLocation(), item, true, BanAction.FILL, new BanData(BanDataType.MATERIAL, e.getBlockClicked().getType())))
                    e.setCancelled(true);
            }, priority.contains(BanAction.FILL));
        }

        if (blacklist.contains(BanAction.GLIDE) || whitelist) {
            if (!BanVersion.v9OrMore) {
                if (!all && !whitelist) // notifying if used an action unavailable on the current minecraft version
                    sender.sendMessage(Chat.color("&cCan not use the '&eglide&c' action in Minecraft < 1.9."));
            } else
                registerEvent(org.bukkit.event.entity.EntityToggleGlideEvent.class, (li, event) -> {
                    final org.bukkit.event.entity.EntityToggleGlideEvent e = (org.bukkit.event.entity.EntityToggleGlideEvent) event;
                    if (!(e.getEntity() instanceof Player)) return;
                    final Player p = (Player) e.getEntity();
                    final EntityEquipment ee = p.getEquipment();
                    if (ee == null) return;
                    final ItemStack item = ee.getChestplate();
                    if (item == null) return;
                    if (api.isBanned(p, item, true, BanAction.GLIDE)) {
                        p.setGliding(false);
                        p.setSneaking(true);

                        // Removing the elytra from player Inventory, to prevent any glitch
                        Bukkit.getScheduler().runTask(pl, () -> {
                            if (!p.isOnline()) return;
                            p.setGliding(false);
                            p.setSneaking(true);

                            // Already removed?
                            final ItemStack chestplate = ee.getChestplate();
                            if (Utils.isNullOrAir(chestplate)) return;

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
                }, priority.contains(BanAction.GLIDE));
        }

        if (blacklist.contains(BanAction.HANGINGPLACE) || whitelist) {
            registerEvent(HangingPlaceEvent.class, (li, event) -> {
                final HangingPlaceEvent e = (HangingPlaceEvent) event;
                if (e.getPlayer() == null) return;
                final ItemStack item = Utils.getItemInHand(e.getPlayer());
                if (api.isBanned(e.getPlayer(), e.getEntity().getLocation(), item, true, BanAction.HANGINGPLACE, new BanData(BanDataType.ENTITY, e.getEntity().getType())))
                    e.setCancelled(true);
            }, priority.contains(BanAction.HANGINGPLACE));
        }

        if (blacklist.contains(BanAction.HOLD) || whitelist) {
            registerEvent(PlayerItemHeldEvent.class, (li, event) -> {
                final PlayerItemHeldEvent e = (PlayerItemHeldEvent) event;
                final ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
                if (item != null && api.isBanned(e.getPlayer(), item, true, BanAction.HOLD))
                    e.setCancelled(true);
            }, priority.contains(BanAction.HOLD));

            registerEvent(InventoryDragEvent.class, (li, event) -> {
                final InventoryDragEvent e = (InventoryDragEvent) event;
                final Player p = (Player) e.getWhoClicked();
                final ItemStack item = e.getOldCursor();
                if (e.getInventorySlots().contains(p.getInventory().getHeldItemSlot()) && api.isBanned(p, item, true, BanAction.HOLD))
                    e.setCancelled(true);
            }, priority.contains(BanAction.HOLD));

            registerEvent(InventoryClickEvent.class, (li, event) -> {
                final InventoryClickEvent e = (InventoryClickEvent) event;
                final Player p = (Player) e.getWhoClicked();
                final int heldItemSlot = p.getInventory().getHeldItemSlot();

                // Hotkey?
                if (e.getHotbarButton() == heldItemSlot) {
                    final ItemStack item = p.getInventory().getItem(e.getSlot());
                    if (item != null && api.isBanned(p, item, true, BanAction.HOLD)) {
                        e.setCancelled(true);
                        return;
                    }
                } else if (e.getHotbarButton() > -1) {
                    if (e.getSlot() == heldItemSlot) {
                        final ItemStack item = p.getInventory().getItem(e.getHotbarButton());
                        if (item != null && api.isBanned(p, item, true, BanAction.HOLD)) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }

                // Shift click from another inventory?
                if (!e.getView().getTopInventory().equals(e.getView().getBottomInventory()) && e.getView().getTopInventory().equals(Utils.getClickedInventory(e.getView(), e.getRawSlot())) && e.isShiftClick()) {
                    final ItemStack item = e.getCurrentItem();
                    if (item == null) return;
                    final List<Integer> changedSlots = Utils.getChangedSlots(p.getInventory(), item);
                    if (changedSlots.contains(heldItemSlot) && api.isBanned(p, item, true, BanAction.HOLD)) {
                        e.setCancelled(true);
                        return;
                    }
                }

                // Click?
                if (e.getSlot() == heldItemSlot) {
                    final ItemStack cursor = e.getCursor();
                    if (cursor != null && api.isBanned(p, cursor, true, BanAction.HOLD))
                        e.setCancelled(true);
                }
            }, priority.contains(BanAction.HOLD));

            // Pickup
            // >=1.12: EntityPickupItemEvent
            // <1.12: PlayerPickupItemEvent
            if (BanVersion.v12OrMore) {
                registerEvent(org.bukkit.event.entity.EntityPickupItemEvent.class, (li, event) -> {
                    final org.bukkit.event.entity.EntityPickupItemEvent e = (org.bukkit.event.entity.EntityPickupItemEvent) event;
                    if (!(e.getEntity() instanceof Player)) return;
                    final Player p = (Player) e.getEntity();
                    final int toSlot = p.getInventory().firstEmpty();
                    if (toSlot == p.getInventory().getHeldItemSlot() && api.isBanned(p, e.getItem().getLocation(), e.getItem().getItemStack(), true, BanAction.HOLD))
                        e.setCancelled(true);
                }, priority.contains(BanAction.HOLD));
            } else {
                registerEvent(org.bukkit.event.player.PlayerPickupItemEvent.class, (li, event) -> {
                    final org.bukkit.event.player.PlayerPickupItemEvent e = (org.bukkit.event.player.PlayerPickupItemEvent) event;
                    final Player p = e.getPlayer();
                    final int toSlot = p.getInventory().firstEmpty();
                    if (toSlot == p.getInventory().getHeldItemSlot() && api.isBanned(p, e.getItem().getLocation(), e.getItem().getItemStack(), true, BanAction.HOLD))
                        e.setCancelled(true);
                }, priority.contains(BanAction.HOLD));
            }
        }

        if (blacklist.contains(BanAction.INTERACT) || whitelist) {
            registerEvent(PlayerInteractEvent.class, (li, event) -> {
                final PlayerInteractEvent e = (PlayerInteractEvent) event;
                if (e.useInteractedBlock() == Event.Result.DENY || e.useItemInHand() == Event.Result.DENY) return;
                if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    final ItemStack item = Utils.getItemInHand(e.getPlayer());
                    if (api.isBanned(e.getPlayer(), e.getClickedBlock().getLocation(), e.getClickedBlock().getType(), true, BanAction.INTERACT, new BanData(BanDataType.MATERIAL, item.getType()))) {
                        if (!BanVersion.v12OrMore) e.getPlayer().updateInventory();
                        e.setCancelled(true);
                    }
                }
            }, priority.contains(BanAction.INTERACT));
        }

        if (blacklist.contains(BanAction.INVENTORYCLICK) || whitelist) {
            registerEvent(InventoryClickEvent.class, (li, event) -> {
                final InventoryClickEvent e = (InventoryClickEvent) event;
                if (Utils.getClickedInventory(e.getView(), e.getRawSlot()) == null) return;

                final Inventory inv;
                final ItemStack item;

                if (e.getHotbarButton() >= 0) {
                    inv = e.getRawSlot() < e.getView().getTopInventory().getSize() ? e.getView().getTopInventory() : e.getView().getBottomInventory();
                    item = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                } else {
                    inv = Utils.getClickedInventory(e.getView(), e.getRawSlot());
                    item = e.getCurrentItem();
                }

                if (!Utils.isNullOrAir(item))
                    if (api.isBanned((Player) e.getWhoClicked(), item, true, BanAction.INVENTORYCLICK, new BanData(BanDataType.INVENTORY_FROM, inv.getType())))
                        e.setCancelled(true);
            }, priority.contains(BanAction.INVENTORYCLICK));
        }

        if (blacklist.contains(BanAction.INVENTORYCLICK) || whitelist) {
            registerEvent(InventoryClickEvent.class, (li, event) -> {
                final InventoryClickEvent e = (InventoryClickEvent) event;
                if (Utils.getClickedInventory(e.getView(), e.getRawSlot()) == null) return;

                final Inventory inv;
                final ItemStack item;

                if (e.getHotbarButton() >= 0) {
                    inv = e.getRawSlot() < e.getView().getTopInventory().getSize() ? e.getView().getTopInventory() : e.getView().getBottomInventory();
                    item = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                } else {
                    inv = Utils.getClickedInventory(e.getView(), e.getRawSlot());
                    item = e.getCurrentItem();
                }

                if (!Utils.isNullOrAir(item))
                    if (api.isBanned((Player) e.getWhoClicked(), item, true, BanAction.INVENTORYCLICK, new BanData(BanDataType.INVENTORY_FROM, inv.getType())))
                        e.setCancelled(true);
            }, priority.contains(BanAction.INVENTORYCLICK));
        }

        if (blacklist.contains(BanAction.MENDING) || whitelist) {
            if (!BanVersion.v13OrMore) {
                if (!all && !whitelist) // notifying if used an action unavailable on the current minecraft version
                    sender.sendMessage(Chat.color("&cCan not use the '&emending&c' action in Minecraft < 1.13."));
            } else
                registerEvent(PlayerItemMendEvent.class, (ll, event) -> {
                    final PlayerItemMendEvent e = (PlayerItemMendEvent) event;
                    if (api.isBanned(e.getPlayer(), e.getItem(), true, BanAction.MENDING))
                        e.setCancelled(true);
                }, priority.contains(BanAction.MENDING));
        }

        if (blacklist.contains(BanAction.PICKUP) || whitelist) {
            // Pickup cooldown map clearing
            registerEvent(PlayerQuitEvent.class, (li, event) -> pl.getUtils().getMessagesCooldown().remove(((PlayerQuitEvent) event).getPlayer().getUniqueId()), priority.contains(BanAction.PICKUP));

            if (BanVersion.v12OrMore)
                registerEvent(org.bukkit.event.entity.EntityPickupItemEvent.class, (li, event) -> {
                    final org.bukkit.event.entity.EntityPickupItemEvent e = (org.bukkit.event.entity.EntityPickupItemEvent) event;
                    if (!(e.getEntity() instanceof Player)) return;
                    if (api.isBanned((Player) e.getEntity(), e.getItem().getLocation(), e.getItem().getItemStack(), true, BanAction.PICKUP))
                        e.setCancelled(true);
                }, priority.contains(BanAction.PICKUP));
            else
                registerEvent(org.bukkit.event.player.PlayerPickupItemEvent.class, (li, event) -> {
                    final org.bukkit.event.player.PlayerPickupItemEvent e = (org.bukkit.event.player.PlayerPickupItemEvent) event;
                    if (api.isBanned(e.getPlayer(), e.getItem().getLocation(), e.getItem().getItemStack(), true, BanAction.PICKUP))
                        e.setCancelled(true);
                }, priority.contains(BanAction.PICKUP));
        }

        if (blacklist.contains(BanAction.PLACE) || whitelist) {
            registerEvent(BlockPlaceEvent.class, (li, event) -> {
                final BlockPlaceEvent e = (BlockPlaceEvent) event;
                if (Utils.isNullOrAir(e.getItemInHand())) return;
                if (api.isBanned(e.getPlayer(), e.getItemInHand(), true, BanAction.PLACE, new BanData(BanDataType.MATERIAL, e.getBlockAgainst().getType()))) {
                    e.setCancelled(true);
                    if (!BanVersion.v12OrMore) e.getPlayer().updateInventory();
                }
            }, priority.contains(BanAction.PLACE));
        }

        if (blacklist.contains(BanAction.USE) || whitelist) {
            registerEvent(PlayerInteractEvent.class, (li, event) -> {
                final PlayerInteractEvent e = (PlayerInteractEvent) event;
                if (Utils.isNullOrAir(e.getItem())) return;
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
                    if (e.getClickedBlock() != null) {
                        if (api.isBanned(e.getPlayer(), e.getClickedBlock().getRelative(e.getBlockFace()).getLocation(), e.getItem(), true, BanAction.USE, new BanData(BanDataType.MATERIAL, e.getClickedBlock().getType()))) {
                            e.setCancelled(true);
                            if (!BanVersion.v12OrMore) e.getPlayer().updateInventory();
                        }
                    } else {
                        if (api.isBanned(e.getPlayer(), e.getItem(), true, BanAction.USE)) {
                            e.setCancelled(true);
                            if (!BanVersion.v12OrMore) e.getPlayer().updateInventory();
                        }
                    }
                }
            }, priority.contains(BanAction.USE));
        }

        if (blacklist.contains(BanAction.RENAME) || whitelist) {
            registerEvent(InventoryClickEvent.class, (li, event) -> {
                final InventoryClickEvent e = (InventoryClickEvent) event;
                final Inventory inv = Utils.getClickedInventory(e.getView(), e.getRawSlot());
                if (inv == null) return;
                if (inv.getType() != InventoryType.ANVIL || e.getRawSlot() != 2) return;

                // Getting item
                final ItemStack item = inv.getItem(0);
                if (Utils.isNullOrAir(item)) return;

                // Getting result
                final ItemStack result = inv.getItem(2);
                if (Utils.isNullOrAir(result)) return;

                // Comparing display names
                final String itemName = Utils.getItemDisplayname(item);
                final String resultName = Utils.getItemDisplayname(result);

                if (!itemName.equals(resultName))
                    if (api.isBanned((Player) e.getWhoClicked(), item, true, BanAction.RENAME))
                        e.setCancelled(true);

            }, priority.contains(BanAction.RENAME));

            final List<String> renameCommands = pl.getBanConfig().getConfig().getStringList("actions.rename");
            if (renameCommands.size() > 0)
                registerEvent(PlayerCommandPreprocessEvent.class, (li, event) -> {
                    final PlayerCommandPreprocessEvent e = (PlayerCommandPreprocessEvent) event;
                    for (final String command : renameCommands)
                        if (e.getMessage().toLowerCase().startsWith(command.toLowerCase()))
                            if (api.isBanned(e.getPlayer(), Utils.getItemInHand(e.getPlayer()), true, BanAction.RENAME)) {
                                e.setCancelled(true);
                                return;
                            }
                }, priority.contains(BanAction.RENAME));
        }

        if (blacklist.contains(BanAction.SMELT) || whitelist) {
            registerEvent(FurnaceSmeltEvent.class, (li, event) -> {
                if (!(event instanceof FurnaceSmeltEvent)) return;
                final FurnaceSmeltEvent e = (FurnaceSmeltEvent) event;
                final ItemStack item = e.getSource();
                final BlockState blockState = e.getBlock().getState();
                if (blockState instanceof InventoryHolder) {
                    final InventoryHolder holder = (InventoryHolder) blockState;
                    if (api.isBanned(blockState.getWorld(), item, BanAction.SMELT)) {
                        if (!holder.getInventory().getViewers().isEmpty())
                            if (!api.isBanned((Player) holder.getInventory().getViewers().get(0), blockState.getLocation(), item, true, BanAction.SMELT))
                                return;
                        e.setCancelled(true);
                    }
                }
            }, priority.contains(BanAction.SMELT));
        }

        if (blacklist.contains(BanAction.SWAP) || whitelist) {
            if (!BanVersion.v9OrMore) {
                if (!all && !whitelist) // notifying if used an action unavailable on the current minecraft version
                    sender.sendMessage(Chat.color("&cCan not use the '&eswap&c' action in Minecraft < 1.9."));
            } else {
                registerEvent(org.bukkit.event.player.PlayerSwapHandItemsEvent.class, (li, event) -> {
                    final org.bukkit.event.player.PlayerSwapHandItemsEvent e = (org.bukkit.event.player.PlayerSwapHandItemsEvent) event;
                    if (e.getMainHandItem() != null && api.isBanned(e.getPlayer(), e.getMainHandItem(), true, BanAction.SWAP)) {
                        e.setCancelled(true);
                        return;
                    }
                    if (e.getOffHandItem() != null && api.isBanned(e.getPlayer(), e.getOffHandItem(), true, BanAction.SWAP))
                        e.setCancelled(true);
                }, priority.contains(BanAction.SWAP));

                registerEvent(InventoryClickEvent.class, (li, event) -> {
                    final InventoryClickEvent e = (InventoryClickEvent) event;
                    if (e.getInventory().getType() != InventoryType.PLAYER && e.getInventory().getType() != InventoryType.CRAFTING)
                        return;
                    if (e.getRawSlot() == 45) {
                        final ItemStack item;
                        if (e.getHotbarButton() >= 0)
                            item = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                        else item = e.getCursor();
                        if (!Utils.isNullOrAir(item))
                            if (api.isBanned((Player) e.getWhoClicked(), item, true, BanAction.SWAP)) {
                                e.setCancelled(true);
                                return;
                            }
                    }

                    if (e.isShiftClick()) {
                        final ItemStack item = e.getCurrentItem();
                        if (!Utils.isNullOrAir(item))
                            if (api.isBanned((Player) e.getWhoClicked(), item, true, BanAction.SWAP))
                                e.setCancelled(true);
                    }
                }, priority.contains(BanAction.SWAP));

                registerEvent(InventoryDragEvent.class, (li, event) -> {
                    final InventoryDragEvent e = (InventoryDragEvent) event;
                    if (e.getRawSlots().contains(45)) {
                        final ItemStack item = e.getNewItems().get(45);
                        if (!Utils.isNullOrAir(item))
                            if (api.isBanned((Player) e.getWhoClicked(), item, true, BanAction.SWAP))
                                e.setCancelled(true);
                    }
                }, priority.contains(BanAction.SWAP));
            }
        }

        if (blacklist.contains(BanAction.TRANSFER) || whitelist) {
            // Clicking
            registerEvent(InventoryClickEvent.class, (li, event) -> {
                final InventoryClickEvent e = (InventoryClickEvent) event;
                final Inventory invClicked = Utils.getClickedInventory(e.getView(), e.getRawSlot());
                if (invClicked == null) return;

                final Player p = (Player) e.getWhoClicked();
                final Inventory top = e.getView().getTopInventory();
                final Inventory bottom = e.getView().getBottomInventory();

                if (top.getType() != InventoryType.CRAFTING && e.getClick() == ClickType.DOUBLE_CLICK) { // Trying to get all items for a banned one?
                    final ItemStack item = e.getCursor();
                    if (!Utils.isNullOrAir(item))
                        if (api.isBanned(p, item, true, BanAction.TRANSFER, new BanData(BanDataType.INVENTORY_FROM, bottom.getType()), new BanData(BanDataType.INVENTORY_TO, top.getType()))) {
                            e.setCancelled(true);
                            return;
                        }
                }

                if (invClicked.equals(bottom)) { // Player Inventory clicked
                    if (e.isShiftClick() && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        final ItemStack item = e.getCurrentItem();
                        if (Utils.isNullOrAir(item)) return;
                        // Banned?
                        if (api.isBanned(p, item, true, BanAction.TRANSFER, new BanData(BanDataType.INVENTORY_FROM, bottom.getType()), new BanData(BanDataType.INVENTORY_TO, top.getType())))
                            e.setCancelled(true);
                    }
                } else { // Top container clicked
                    // Shift
                    if (e.isShiftClick() && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        final ItemStack item = e.getCurrentItem();
                        if (Utils.isNullOrAir(item)) return;
                        if (api.isBanned(p, item, true, BanAction.TRANSFER, new BanData(BanDataType.INVENTORY_FROM, top.getType()), new BanData(BanDataType.INVENTORY_TO, bottom.getType())))
                            e.setCancelled(true);
                    } else {
                        // Hot bar click?
                        if (e.getHotbarButton() > -1) {
                            final ItemStack hotBarItem = bottom.getItem(e.getHotbarButton());
                            if (!Utils.isNullOrAir(hotBarItem) && api.isBanned(p, hotBarItem, true, BanAction.TRANSFER, new BanData(BanDataType.INVENTORY_FROM, bottom.getType()))) {
                                e.setCancelled(true);
                                return;
                            }

                            // Trying to swap with an item from the top inventory?
                            final ItemStack item = top.getItem(e.getRawSlot());
                            if (!Utils.isNullOrAir(item) && api.isBanned(p, item, true, BanAction.TRANSFER, new BanData(BanDataType.INVENTORY_TO, bottom.getType()))) {
                                e.setCancelled(true);
                                return;
                            }
                        }

                        // Normal click
                        final ItemStack clickedItem = e.getCurrentItem();
                        final ItemStack cursorItem = e.getCursor();

                        // Trying to place the cursor item?
                        if (!Utils.isNullOrAir(cursorItem)) {
                            if (api.isBanned(p, cursorItem, true, BanAction.TRANSFER, new BanData(BanDataType.INVENTORY_FROM, bottom.getType()), new BanData(BanDataType.INVENTORY_TO, top.getType()))) {
                                e.setCancelled(true);
                                return;
                            }
                        }

                        // Trying to get the item from top?
                        if (!Utils.isNullOrAir(clickedItem)) {
                            if (api.isBanned(p, clickedItem, true, BanAction.TRANSFER, new BanData(BanDataType.INVENTORY_FROM, top.getType()), new BanData(BanDataType.INVENTORY_TO, bottom.getType())))
                                e.setCancelled(true);
                        }
                    }
                }

            }, priority.contains(BanAction.TRANSFER));

            // Dragging
            registerEvent(InventoryDragEvent.class, (li, event) -> {
                final InventoryDragEvent e = (InventoryDragEvent) event;
                final Player p = (Player) e.getWhoClicked();
                // In its own inventory?
                if (e.getView().getTopInventory().getType() == InventoryType.CRAFTING) return;
                if (Utils.isNullOrAir(e.getOldCursor())) return;

                // Transfering?
                final Set<Integer> rawSlots = e.getRawSlots();
                final int topInventory = e.getView().getTopInventory().getSize();
                boolean transfering = false;
                for (final int slot : rawSlots)
                    if (slot <= topInventory) {
                        transfering = true;
                        break;
                    }
                if (!transfering) return;

                if (api.isBanned(p, e.getOldCursor(), true, BanAction.TRANSFER,
                        new BanData(BanDataType.INVENTORY_FROM, e.getView().getBottomInventory().getType()),
                        new BanData(BanDataType.INVENTORY_TO, e.getView().getTopInventory().getType())))
                    e.setCancelled(true);
            }, priority.contains(BanAction.TRANSFER));

            // Hoppers block?
            if (pl.getBanConfig().getConfig().getBoolean("actions.transfer.hoppers-block")) {
                registerEvent(InventoryMoveItemEvent.class, (li, event) -> {
                    final InventoryMoveItemEvent e = (InventoryMoveItemEvent) event;
                    if (e.getSource().getHolder() instanceof BlockState) {
                        final BlockState bs = (BlockState) e.getSource().getHolder();
                        if (api.isBanned(bs.getWorld(), e.getItem(), BanAction.TRANSFER,
                                new BanData(BanDataType.INVENTORY_FROM, e.getSource().getType()),
                                new BanData(BanDataType.INVENTORY_TO, e.getDestination().getType())))
                            e.setCancelled(true);
                    }
                }, priority.contains(BanAction.TRANSFER));

                registerEvent(InventoryPickupItemEvent.class, (li, event) -> {
                    final InventoryPickupItemEvent e = (InventoryPickupItemEvent) event;
                    if (api.isBanned(e.getItem().getWorld(), e.getItem().getItemStack(), BanAction.TRANSFER, new BanData(BanDataType.INVENTORY_TO, InventoryType.HOPPER)))
                        e.setCancelled(true);
                }, priority.contains(BanAction.TRANSFER));
            }
        }

        if (blacklist.contains(BanAction.UNFILL) || whitelist) {
            registerEvent(PlayerBucketEmptyEvent.class, (li, event) -> {
                final PlayerBucketEmptyEvent e = (PlayerBucketEmptyEvent) event;
                final ItemStack item = Utils.getItemInHand(e.getPlayer());
                if (api.isBanned(e.getPlayer(), e.getBlockClicked().getLocation(), item, true, BanAction.UNFILL, new BanData(BanDataType.MATERIAL, e.getBlockClicked().getType()))) {
                    e.setCancelled(true);
                    e.getPlayer().updateInventory();
                }
            }, priority.contains(BanAction.FILL));
        }

        if (blacklist.contains(BanAction.WEAR) || whitelist) {
            registerEvent(InventoryClickEvent.class, (li, event) -> {
                final InventoryClickEvent e = (InventoryClickEvent) event;
                if (e.getInventory().getType() != InventoryType.PLAYER && e.getInventory().getType() != InventoryType.CRAFTING)
                    return;

                // Armor interaction?
                if (e.getRawSlot() >= 5 && e.getRawSlot() <= 8) {
                    Bukkit.getScheduler().runTask(pl, () -> pl.getUtils().checkPlayerArmors((Player) e.getWhoClicked()));
                    return;
                }

                // Trying to shift click item to armor?
                final ItemStack currentItem = e.getCurrentItem();
                if (e.isShiftClick() && !Utils.isNullOrAir(currentItem)) {
                    Bukkit.getScheduler().runTask(pl, () -> pl.getUtils().checkPlayerArmors((Player) e.getWhoClicked()));
                    return;
                }

                // Trying to use hotbar button?
                if (e.getRawSlot() >= 5 && e.getRawSlot() <= 8 && e.getHotbarButton() > -1) {
                    final ItemStack item = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                    if (!Utils.isNullOrAir(item))
                        Bukkit.getScheduler().runTask(pl, () -> pl.getUtils().checkPlayerArmors((Player) e.getWhoClicked()));
                }
            }, priority.contains(BanAction.WEAR));

            registerEvent(PlayerChangedWorldEvent.class, (li, event) -> {
                final PlayerChangedWorldEvent e = (PlayerChangedWorldEvent) event;
                Bukkit.getScheduler().runTask(pl, () -> pl.getUtils().checkPlayerArmors(e.getPlayer()));
            }, priority.contains(BanAction.WEAR));

            if (pl.getBanConfig().getConfig().getBoolean("actions.wear.region-check") && pl.getHooks().isWorldGuardEnabled()) {
                final IWorldGuardHook hook = pl.getHooks().getWorldGuardHook();
                if (hook == null)
                    sender.sendMessage(Chat.color("&cCan not use the region checker for wear action, as worldguard is not reachable."));
                else {
                    // Register the region change event
                    registerEvent(PlayerMoveEvent.class, (li, event) -> {
                        final PlayerMoveEvent e = (PlayerMoveEvent) event;
                        if (e.getTo() == null) return;
                        final Location from = e.getFrom();
                        final Location to = e.getTo();
                        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())
                            return;
                        if (!hook.getStandingRegions(from).equals(hook.getStandingRegions(to)))
                            Bukkit.getPluginManager().callEvent(new PlayerRegionChangeEvent(e.getPlayer()));
                    }, priority.contains(BanAction.WEAR));

                    registerEvent(PlayerRegionChangeEvent.class, (li, event) -> {
                        final PlayerRegionChangeEvent e = (PlayerRegionChangeEvent) event;
                        Bukkit.getScheduler().runTask(pl, () -> pl.getUtils().checkPlayerArmors(e.getPlayer()));
                    }, priority.contains(BanAction.WEAR));
                }
            }

            // Scanner?
            pl.getUtils().getWearScanner().setEnabled(pl.getBanConfig().getConfig().getBoolean("actions.wear.scanner"));
        }
    }

    /**
     * Registering a needed event
     *
     * @param c        the event class
     * @param ee       the event executor
     * @param priority if the event should have maximum priority
     */
    private void registerEvent(@NotNull final Class<? extends Event> c, @NotNull final EventExecutor ee, final boolean priority) {
        Bukkit.getPluginManager().registerEvent(c, listener, (priority ? EventPriority.LOWEST : EventPriority.NORMAL), ee, pl, !priority);
        activated++;
    }

    /**
     * Get the amount of events listened
     *
     * @return the amount of events listened
     */
    public int getActivated() {
        return activated;
    }
}
