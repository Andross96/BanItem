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
package fr.andross.banitem.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Utility class
 * @version 3.3
 * @author Andross
 */
public final class Utils {

    /**
     * Quick utils to check if the item is null or if its type is Material.AIR
     * @param item the {@link ItemStack}
     * @return true if the ItemStack is null or AIR, otherwise false
     */
    public static boolean isNullOrAir(@Nullable final ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    /**
     * Used to check if an array is null or empty.
     * Mainly used to check varargs.
     * @param a array
     * @return true is the array is null or empty, otherwise false
     */
    public static boolean isNullOrEmpty(@Nullable final Object[] a) {
        return a == null || a.length == 0 || a[0] == null;
    }

    /**
     * Used to check if a String is <b>not</b> null nor empty
     * @param s the string to check
     * @return true if the string is not null nor empty, otherwise false
     */
    public static boolean isNotNullOrEmpty(@Nullable final String s) {
        return s != null && !s.isEmpty();
    }

    /**
     * Get the item from the player hand, even AIR, regardless the version
     * @param p the {@link Player}
     * @return the ItemStack in the player's hand, possibly AIR
     */
    @NotNull
    public static ItemStack getItemInHand(@NotNull final Player p) {
        final EntityEquipment ee = p.getEquipment();
        if (ee == null) return new ItemStack(Material.AIR);
        final ItemStack itemInHand = BanVersion.v9OrMore ? ee.getItemInMainHand() : ee.getItemInHand();
        return itemInHand == null ? new ItemStack(Material.AIR) : itemInHand;
    }

    /**
     * Get the display name of the item, empty string if empty
     * @param item the itemstack
     * @return the display name of the item, otherwise an empty string
     */
    @NotNull
    public static String getItemDisplayname(@NotNull final ItemStack item) {
        if (!item.hasItemMeta()) return "";
        final ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return "";
        return !itemMeta.hasDisplayName() ? "" : itemMeta.getDisplayName();
    }

    /**
     * Check if all elements are equals
     * @param c the collection
     * @return true if all elements are equals, otherwise false
     */
    public static boolean areAllEquals(@NotNull final Collection<?> c) {
        return c.stream().distinct().count() <= 1;
    }

    /**
     * Get the affected slots if an item will be added to the inventory
     * @param inv an inventory
     * @param newItem the item
     * @return a non-null list of affected slots
     */
    @NotNull
    public static List<Integer> getChangedSlots(@NotNull final PlayerInventory inv, @NotNull final ItemStack newItem) {
        final List<Integer> changed = new ArrayList<>();
        if (!inv.contains(newItem.getType())) {
            final int firstEmpty = firstEmpty(inv);
            if (firstEmpty != -1)
                changed.add(firstEmpty);
        } else {
            int amount = newItem.getAmount();
            final Map<Integer, ? extends ItemStack> items = inv.all(newItem.getType());
            for (int i = 0; i < inv.getSize(); i++) {
                if (!items.containsKey(i)) continue;
                final ItemStack item = items.get(i);
                int slotamount = item.getMaxStackSize() - item.getAmount();
                if (slotamount > 1) {
                    if (amount > slotamount) {
                        int toAdd = slotamount - amount;
                        amount = amount - toAdd;
                        changed.add(i);
                    } else {
                        changed.add(i);
                        amount = 0;
                        break;
                    }
                }
            }
            if (amount > 0) {
                final int firstEmpty = firstEmpty(inv);
                if (firstEmpty != -1)
                    changed.add(firstEmpty);
            }
        }
        return changed;
    }

    /**
     * A method that get the first slot empty from a player inventory.
     * This method will first focus on stored items, then will check on hotbar.
     * @param inv the player inventory
     * @return first empty slot
     * @see Inventory#firstEmpty()
     */
    public static int firstEmpty(final PlayerInventory inv) {
        // Ignoring hotbar first
        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 8) continue;
            if (inv.getItem(i) == null) return i;
        }
        return inv.firstEmpty();
    }

    /**
     * Get an unmodifiable map of all enchantments on an item, considering stored enchants on enchanted book
     * @param item the item
     * @return non-null unmodifiable map of enchantments and level on the item
     */
    @NotNull
    public static Map<Enchantment, Integer> getAllEnchants(@NotNull final ItemStack item) {
        final Map<Enchantment, Integer> map = new HashMap<>(item.getEnchantments());

        if (item.getType() == Material.ENCHANTED_BOOK) {
            final EnchantmentStorageMeta esm = ((EnchantmentStorageMeta) item.getItemMeta());
            if (esm != null)
                map.putAll(esm.getStoredEnchants());
        }

        return Collections.unmodifiableMap(map);
    }

    /**
     * Try to get potion effects on an item
     * @param item the item
     * @return non-null unmodifiable map of potion effect and level on the item
     */
    @NotNull
    public static Map<PotionEffectType, Integer> getAllPotionEffects(@NotNull final ItemStack item) {
        final Map<PotionEffectType, Integer> map = new HashMap<>();

        // Getting base effect
        final ItemMeta itemMeta = item.hasItemMeta() ? item.getItemMeta() : null;
        if (!BanVersion.v9OrMore && item.getType() == Material.POTION) {
            final Potion p = Potion.fromDamage(item.getDurability());
            if (p.getType().getEffectType() != null)
                map.put(p.getType().getEffectType(), p.getLevel());
        } else if (itemMeta instanceof PotionMeta) {
            final PotionMeta pm = (PotionMeta) itemMeta;
            final PotionEffectType effectType = pm.getBasePotionData().getType().getEffectType();
            final int level = pm.getBasePotionData().isUpgraded() ? 2 : 1;
            if (effectType != null)
                map.put(effectType, level);
        }

        // Getting custom effects
        if (itemMeta instanceof PotionMeta) {
            final PotionMeta pm = (PotionMeta) itemMeta;
            if (pm.hasCustomEffects()) {
                for (final PotionEffect customEffect : pm.getCustomEffects()) {
                    final PotionEffectType effectType = customEffect.getType();
                    final int level = customEffect.getAmplifier() == 0 ? 1 : 2;
                    map.put(effectType, level);
                }
            }
        }

        return Collections.unmodifiableMap(map);
    }

    /**
     * Get the clicked inventory from a view.
     * This is mainly used for 1.7 as InventoryEvent#getClickedInventory does not exist.
     * @param view the inventory view
     * @param slot the raw slot clicked
     * @return the inventory clicked
     */
    public static Inventory getClickedInventory(final InventoryView view, final int slot) {
        return view.getInventory(slot);
    }
}
