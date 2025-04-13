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

import fr.andross.banitem.utils.enchantments.EnchantmentHelper;
import fr.andross.banitem.utils.enchantments.EnchantmentWrapper;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A simple ItemStack builder utility class.
 *
 * @author Andross
 * @version 3.2
 */
public final class ItemStackBuilder {
    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    public ItemStackBuilder(@Nullable final ConfigurationSection section) throws Exception {
        if (section == null) {
            throw new Exception("&cunknown section.");
        }
        final String material = section.getString("material");
        if (material == null) {
            throw new Exception("&cempty material.");
        }
        final Material m = Material.matchMaterial(material.toUpperCase());
        if (m == null) {
            throw new Exception("&cmaterial not found.");
        }
        itemStack = new ItemStack(m);
        itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }

        // Checking amount
        if (section.contains("amount")) {
            final int amount = section.getInt("amount");
            if (amount <= 0) {
                throw new Exception("&cinvalid amount. Must be superior to 0.");
            }
            itemStack.setAmount(amount);
        }

        // Checking durability
        if (section.contains("durability")) {
            final short durability = (short) section.getInt("durability");
            if (MinecraftVersion.v13OrMore) {
                if (!(itemMeta instanceof org.bukkit.inventory.meta.Damageable)) {
                    throw new Exception("&ccan not set durability to this item.");
                }
                final org.bukkit.inventory.meta.Damageable d = (org.bukkit.inventory.meta.Damageable) itemMeta;
                d.setDamage(durability);
            } else {
                itemStack.setDurability(durability);
            }
        }

        // Checking displayname
        if (section.contains("displayname")) {
            itemMeta.setDisplayName(Chat.color(Objects.requireNonNull(section.getString("displayname", ""))));
        }

        // Checking lore
        if (section.contains("lore")) {
            itemMeta.setLore(section.getStringList("lore").stream().map(Chat::color).collect(Collectors.toList()));
        }

        // Checking enchantments
        if (section.contains("enchantment")) {
            for (final String enchant : section.getStringList("enchantment")) {
                final EnchantmentWrapper ew = EnchantmentHelper.getEnchantmentWrapper(enchant);
                if (ew == null) {
                    throw new Exception("&cinvalid enchantment '&e" + enchant + "&c'.");
                }
                itemMeta.addEnchant(ew.getEnchantment(), ew.getLevel(), true);
            }
        }

        // Checking flags
        if (section.contains("flags")) {
            section.getStringList("flags").forEach(f -> itemMeta.addItemFlags(ItemFlag.valueOf(f.toUpperCase())));
        }
    }

    /**
     * Build the item.
     *
     * @return the item built
     */
    @NotNull
    public ItemStack build() {
        final ItemStack clonedStack = itemStack.clone();
        if (itemMeta != null) {
            clonedStack.setItemMeta(itemMeta.clone());
        }
        return clonedStack;
    }
}
