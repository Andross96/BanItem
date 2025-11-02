/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a banned item with optional NBT data.
 */
public class BannedItem {
    private final Item type;
    private final ItemStack itemStack;

    public BannedItem(@NotNull final Item type) {
        this.type = type;
        this.itemStack = null;
    }

    public BannedItem(@NotNull final ItemStack itemStack) {
        this.type = itemStack.getItem();
        this.itemStack = itemStack.copy();
    }

    @NotNull
    public Item getType() {
        return type;
    }

    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean hasNBT() {
        return itemStack != null && itemStack.hasTag();
    }
    
    /**
     * Convert this BannedItem to an ItemStack.
     */
    @NotNull
    public ItemStack toItemStack() {
        if (itemStack != null) {
            return itemStack.copy();
        }
        return new ItemStack(type);
    }

    /**
     * Check if this banned item matches the given ItemStack.
     */
    public boolean matches(@NotNull final ItemStack other) {
        if (other.getItem() != type) {
            return false;
        }

        // If we don't have NBT data, just match by type
        if (itemStack == null || !hasNBT()) {
            return true;
        }

        // Match with NBT
        return ItemStack.isSameItemSameComponents(itemStack, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BannedItem that = (BannedItem) o;
        if (type != that.type) return false;
        
        // If both don't have NBT, they're equal
        if (itemStack == null && that.itemStack == null) return true;
        if (itemStack == null || that.itemStack == null) return false;
        
        // Compare with NBT
        return ItemStack.isSameItemSameComponents(itemStack, that.itemStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        if (itemStack != null) {
            return "BannedItem{" + itemStack.toString() + "}";
        }
        return "BannedItem{" + type.toString() + "}";
    }
}
