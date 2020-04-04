package fr.andross.banitem.Utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class BannedItem {
    private final Material m;
    private final ItemMeta meta;
    private final MaterialData data;

    public BannedItem(@NotNull final ItemStack item) {
        this.m = item.getType();
        this.meta = item.hasItemMeta() ? item.getItemMeta() : null;
        this.data = item.getData();
    }

    public Material getType() {
        return m;
    }

    public ItemStack toItemStack() {
        final ItemStack item = new ItemStack(m);
        if (meta != null) item.setItemMeta(meta);
        if (data != null) item.setData(data);
        return item;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BannedItem that = (BannedItem) o;
        return m == that.m && Objects.equals(meta, that.meta) && (BanUtils.v13OrMore || Objects.equals(data, that.data));
    }

    @Override
    public int hashCode() {
        return BanUtils.v13OrMore ? Objects.hash(m, meta) : Objects.hash(m, meta, data);
    }
}
