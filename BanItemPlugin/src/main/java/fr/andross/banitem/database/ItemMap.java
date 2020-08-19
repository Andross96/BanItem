/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2020 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.andross.banitem.database;

import fr.andross.banitem.options.BanDataType;
import fr.andross.banitem.options.BanOption;
import fr.andross.banitem.options.BanOptionData;
import fr.andross.banitem.utils.item.BannedItem;
import fr.andross.banitem.utils.item.BannedItemMeta;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Map that store all the banned items, with their options and options datas.
 * @version 2.4
 * @author Andross
 */
public class ItemMap extends HashMap<BannedItem, Map<BanOption, BanOptionData>> {

    /**
     * Get a map of options and their respective data for this material, if present.
     * This method will not consider the ban option meta data.
     * @param m material to get
     * @return a map of options and data if found, otherwise null.
     */
    @Nullable
    public Map<BanOption, BanOptionData> get(@NotNull final Material m) {
        return get(new BannedItem(m));
    }

    /**
     * Get a map of options and their respective data for this item stack, if present.
     * This method will not consider the ban option meta data.
     * @param item the item stack to get
     * @return a map of options and data if found, otherwise null.
     */
    @Nullable
    public Map<BanOption, BanOptionData> get(@NotNull final ItemStack item) {
        return get(new BannedItem(item));
    }

    /**
     * Get a map of options and their respective data for a banned item object, if present.
     * This method will not consider the ban option meta data.
     * @param o the banned item
     * @return a map of options and data if found, otherwise null.
     */
    @Override
    @Nullable
    public Map<BanOption, BanOptionData> get(@NotNull final Object o) {
        // Checking (can include meta)
        Map<BanOption, BanOptionData> map = super.get(o);

        // Checking by item without meta
        if (map == null && o instanceof BannedItem) {
            final BannedItem itemType = new BannedItem(((BannedItem)o).getType());
            map = super.get(itemType);
        }
        return map;
    }

    /**
     * Get the ban option data for a banned item object for this option, if present.
     * This method will also check and consider the item meta data.
     * @param item the item to get
     * @param option the option related to this
     * @return the ban options datas for this item with this option, otherwise null
     */
    @Nullable
    public BanOptionData getExact(@NotNull final BannedItem item, @NotNull final BanOption option) {
        final Map<BanOption, BanOptionData> map = get(item);
        if (map == null || !map.containsKey(option)) return null;

        // Respect metadatas?
        final BanOptionData data = map.get(option);
        if (data.containsKey(BanDataType.METADATA)) {
            final BannedItemMeta meta = data.getData(BanDataType.METADATA);
            if (meta != null && !meta.matches(item.toItemStack())) return null;
        }

        return data;
    }

}
