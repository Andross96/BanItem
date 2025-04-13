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
package fr.andross.banitem.items;

import fr.andross.banitem.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * An item wrapper, which can handle matching Material/ItemStacks
 * with their ItemMeta and not considering the amount.
 *
 * @author Andross
 * @version 3.1.1
 */
public class BannedItem {
    private final Material m;
    private final ItemMeta itemMeta;
    private final short data;
    private ItemStack itemStack;

    /**
     * Create a banned item without metadata.
     *
     * @param m material
     */
    public BannedItem(@NotNull final Material m) {
        this.m = m;
        itemMeta = null;
        data = 0;
    }

    /**
     * Create a banned item including the ItemMeta of the item.
     *
     * @param item item stack
     */
    public BannedItem(@NotNull final ItemStack item) {
        m = item.getType();
        itemMeta = item.hasItemMeta() ? item.getItemMeta() : null;
        data = MinecraftVersion.v13OrMore ? 0 : item.getDurability();
        itemStack = item;
    }

    /**
     * @return the material of the item
     */
    @NotNull
    public Material getType() {
        return m;
    }

    /**
     * @return the item meta
     */
    @Nullable
    public ItemMeta getItemMeta() {
        return itemMeta;
    }

    /**
     * @return the item data, ignored after 1.13+
     */
    public short getData() {
        return data;
    }

    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * @return an ItemStack representing this item
     */
    public ItemStack toItemStack() {
        if (itemStack != null) return itemStack;
        itemStack = new ItemStack(m);
        if (itemMeta != null) itemStack.setItemMeta(itemMeta.clone());
        if (!MinecraftVersion.v13OrMore) itemStack.setDurability(data);
        return itemStack;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BannedItem)) return false;
        final BannedItem that = (BannedItem) o;
        return itemMeta == null ?
                (MinecraftVersion.v13OrMore ? m == that.m : (m == that.m && data == that.data)) :
                (m == that.m && Objects.equals(itemMeta, that.itemMeta) && (MinecraftVersion.v13OrMore || Objects.equals(data, that.data)));
    }

    @Override
    public int hashCode() {
        return itemMeta == null ?
                (MinecraftVersion.v13OrMore ? m.hashCode() : Objects.hash(m, data)) :
                (MinecraftVersion.v13OrMore ? Objects.hash(m, itemMeta) : Objects.hash(m, itemMeta, data));
    }
}
