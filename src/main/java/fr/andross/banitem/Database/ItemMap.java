package fr.andross.banitem.Database;

import fr.andross.banitem.Options.BanDataType;
import fr.andross.banitem.Options.BanOption;
import fr.andross.banitem.Options.BanOptionData;
import fr.andross.banitem.Utils.Item.BannedItem;
import fr.andross.banitem.Utils.Item.BannedItemMeta;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Map that store all the banned items, with their options and options datas.
 * @version 2.1
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
            final BannedItem itemType = new BannedItem((BannedItem)o, false);
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
