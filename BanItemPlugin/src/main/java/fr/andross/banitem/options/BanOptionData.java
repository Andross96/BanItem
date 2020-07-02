package fr.andross.banitem.options;

import fr.andross.banitem.utils.item.BannedItemMeta;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class offers a way to store the ban options data.
 * Example: the messages, if the option should be logged, any item...
 * The data type is written as description in {@link BanDataType} description.
 * I've also included some clear api methods to get them, at the bottom.
 * @version 2.1
 * @author Andross
 */
public final class BanOptionData extends HashMap<BanDataType, Object> {
    /**
     * Map containing player cooldowns
     * Used only if a cooldown data has been entered
     */
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    /**
     * Trying to get the data from this map
     * @param type the type of data
     * @param <T> the object of the data, retrievable and described in {@link BanDataType}
     * @return the data, null if not found or not a valid type
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getData(@NotNull final BanDataType type) {
        if (!containsKey(type)) return null;
        try {
            return (T) get(type);
        } catch (final ClassCastException e) {
            return null;
        }
    }

    /**
     * Trying to check if a data is present in this ban option datas.
     * This is accepted only for Set datas, described in {@link BanDataType}
     * @param data the ban data to check
     * @return true if the data matches, otherwise false
     */
    public boolean contains(@Nullable final BanData data) {
        if (data == null) return true;
        final Set<Object> s = getData(data.getType());
        return s == null || s.contains(data.getObject());
    }

    /**
     * Trying to check if the item meta matches
     * @param item the item stack to compare
     * @return true if the item meta matches, otherwise false
     */
    public boolean matches(@NotNull final ItemStack item) {
        if (!containsKey(BanDataType.METADATA)) return true;
        return ((BannedItemMeta) get(BanDataType.METADATA)).matches(item);
    }

    /**
     * Serializing the data (to save them into config files)
     * @return a non null serialized map
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new LinkedHashMap<>();

        for (final Map.Entry<BanDataType, Object> entry : entrySet()) {
            // Have to serialize a collection?
            if (entry.getValue() instanceof Collection) {
                // Empty collection?
                final Collection<Object> c = (Collection<Object>) entry.getValue();
                if (c.size() == 0) continue;
                // Could be a simple string, else serializing the list
                if (c.size() == 1) map.put(entry.getKey().name().toLowerCase(), c.iterator().next().toString());
                else map.put(entry.getKey().name().toLowerCase(), serialize(c));
            }
            else map.put(entry.getKey().name(), entry.getValue().toString());
        }

        return map;
    }

    /**
     * This will serialize a collection of object.
     * Mainly used to parse the maps into a config file
     * @param c collection of objects
     * @return a list of serialized object from the collection
     */
    @NotNull
    public List<String> serialize(@NotNull final Collection<Object> c) {
        final List<String> list = new ArrayList<>();
        for (final Object o : c) list.add(o.toString());
        return list;
    }

    // Name friendly methods to get the datas
    /**
     * Trying to get a cooldown added for this option
     * @return a cooldown long (millis)
     */
    public long getCooldown() {
        return (long) getOrDefault(BanDataType.COOLDOWN, 0L);
    }

    /**
     * Trying to get a {@link BannedItemMeta} object, to compare item metas
     * @return the banned item meta, null if there is none added
     */
    @Nullable
    public BannedItemMeta getMetadata() {
        return getData(BanDataType.METADATA);
    }

    /**
     * Trying to get a set of entitytype added for this option
     * @return the set of entitytype, null if there is none added
     */
    @Nullable
    public Set<EntityType> getEntities() {
        return getData(BanDataType.ENTITY);
    }

    /**
     * Trying to get a set of gamemodes added for this option
     * @return the set of gamemodes, null if there is none added
     */
    @Nullable
    public Set<GameMode> getGamemodes() {
        return getData(BanDataType.GAMEMODE);
    }

    /**
     * Trying to get a set of inventory-from added for this option
     * @return the set of InventoryType, null if there is none added
     */
    @Nullable
    public Set<InventoryType> getInventoryFrom() {
        return getData(BanDataType.INVENTORY_FROM);
    }

    /**
     * Trying to get a set of inventory-to added for this option
     * @return the set of InventoryType, null if there is none added
     */
    @Nullable
    public Set<InventoryType> getInventoryTo() {
        return getData(BanDataType.INVENTORY_TO);
    }

    /**
     * Trying to get a set of materials added for this option
     * @return the set of material, null if there is none added
     */
    @Nullable
    public Set<Material> getMaterial() {
        return getData(BanDataType.MATERIAL);
    }

    /**
     * Trying to check if the option should be logged
     * @return true if the option is logged, otherwise false
     */
    public boolean getLog() {
        return containsKey(BanDataType.LOG) && (boolean) get(BanDataType.LOG);
    }

    /**
     * Trying to get the messages added for this option
     * @return the list of messages, null if there is no messages added
     */
    @Nullable
    public List<String> getMessages() {
        return getData(BanDataType.MESSAGE);
    }

    /**
     * @return map containing players cooldowns time
     */
    @NotNull
    public Map<UUID, Long> getCooldowns() {
        return cooldowns;
    }
}
