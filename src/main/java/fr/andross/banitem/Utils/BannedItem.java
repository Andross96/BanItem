package fr.andross.banitem.Utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.Objects;

public final class BannedItem {
    private final Material m;
    private final ItemMeta meta;
    private final MaterialData data;

    public BannedItem(final Material m) {
        final ItemStack item = new ItemStack(m);
        this.m = item.getType();
        this.meta = item.hasItemMeta() ? item.getItemMeta() : null;
        this.data = item.getData();
    }

    public BannedItem(final ItemStack item) {
        this.m = item.getType();
        this.meta = item.hasItemMeta() ? item.getItemMeta() : null;
        this.data = item.getData();
    }

    public ItemStack toItemStack() {
        final ItemStack item = new ItemStack(m);
        if (meta != null) item.setItemMeta(meta);
        if (data != null) item.setData(data);
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BannedItem that = (BannedItem) o;
        return m == that.m && Objects.equals(meta, that.meta) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m, meta, data);
    }
}
