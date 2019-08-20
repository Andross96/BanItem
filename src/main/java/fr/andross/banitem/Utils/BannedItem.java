package fr.andross.banitem.Utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public final class BannedItem {
    private final Material m;
    private final ItemMeta meta;

    public BannedItem(final Material m) {
        this.m = m;
        this.meta = null;
    }

    public BannedItem(final ItemStack item) {
        this.m = item.getType();
        this.meta = item.hasItemMeta() ? item.getItemMeta() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BannedItem that = (BannedItem) o;
        return m == that.m && Objects.equals(meta, that.meta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m, meta);
    }
}
