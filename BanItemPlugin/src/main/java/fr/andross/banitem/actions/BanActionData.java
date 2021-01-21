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
package fr.andross.banitem.actions;

import fr.andross.banitem.utils.BanVersion;
import fr.andross.banitem.utils.EnchantmentWrapper;
import fr.andross.banitem.utils.statics.Chat;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class offers a way to store the ban actions data.
 * Example: the messages, if the action should be logged, any item...
 * The data type is written as description in {@link BanDataType} description.
 * I've also included some clear api methods to get them, at the bottom.
 * @version 3.0
 * @author Andross
 */
public final class BanActionData {
    private final Map<BanDataType, Object> map = new EnumMap<>(BanDataType.class);
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
        if (!map.containsKey(type)) return null;
        try {
            return (T) map.get(type);
        } catch (final ClassCastException e) {
            return null;
        }
    }

    /**
     * Trying to check if a data is present in this ban action datas.
     * This is accepted only for Set datas, described in {@link BanDataType}
     * @param data the ban data to check
     * @return true if the data matches, otherwise false
     */
    @SuppressWarnings("unchecked")
    public boolean contains(@Nullable final BanData data) {
        if (data == null) return true;
        final Set<Object> s = getData(data.getType());

        // Enchantment?
        if (s != null && data.getType() == BanDataType.ENCHANTMENT) {
            final Set<EnchantmentWrapper> enchants = (Set<EnchantmentWrapper>) data.getObject();
            return !Collections.disjoint(s, enchants);
        }

        return s == null || s.contains(data.getObject());
    }

    /**
     * Serializing the data (to save them into config files)
     * @return a non null serialized map
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new LinkedHashMap<>();

        for (final Map.Entry<BanDataType, Object> entry : this.map.entrySet()) {
            // Have to serialize a collection?
            if (entry.getValue() instanceof Collection) {
                // Empty collection?
                Collection<Object> c = (Collection<Object>) entry.getValue();
                if (c.size() == 0) continue;

                // Is enchantments?
                if (c.iterator().next() instanceof EnchantmentWrapper) {
                    final Collection<EnchantmentWrapper> enchantments = (Collection<EnchantmentWrapper>) entry.getValue();
                    c = new HashSet<>();
                    for (final EnchantmentWrapper e : enchantments) {
                        final String name = BanVersion.v13OrMore ? e.getEnchantment().getKey().getKey() : e.getEnchantment().getName();
                        c.add(name);
                    }
                }

                // Could be a simple string, else serializing the list
                if (c.size() == 1) map.put(entry.getKey().name().toLowerCase(), Chat.revertColor(c.iterator().next().toString()));
                else map.put(entry.getKey().name().toLowerCase(), serialize(c));
            } else map.put(entry.getKey().name(), Chat.revertColor(entry.getValue().toString()));
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
        return c.stream().map(Object::toString).map(Chat::revertColor).collect(Collectors.toList());
    }

    // Name friendly methods to get the datas
    /**
     * Trying to get a cooldown added for this action
     * @return a cooldown long (millis)
     */
    public long getCooldown() {
        return (long) map.getOrDefault(BanDataType.COOLDOWN, 0L);
    }

    /**
     * Trying to get a set of entitytype added for this action
     * @return the set of entitytype, null if there is none added
     */
    @Nullable
    public Set<EntityType> getEntities() {
        return getData(BanDataType.ENTITY);
    }

    /**
     * Trying to get a set of enchantmentwrapper added for this action
     * @return the set of enchantmentwrapper, null if there is none added
     */
    @Nullable
    public Set<EnchantmentWrapper> getEnchantments() {
        return getData(BanDataType.ENCHANTMENT);
    }

    /**
     * Trying to get a set of gamemodes added for this action
     * @return the set of gamemodes, null if there is none added
     */
    @Nullable
    public Set<GameMode> getGamemodes() {
        return getData(BanDataType.GAMEMODE);
    }

    /**
     * Trying to get a set of inventory-from added for this action
     * @return the set of InventoryType, null if there is none added
     */
    @Nullable
    public Set<InventoryType> getInventoryFrom() {
        return getData(BanDataType.INVENTORY_FROM);
    }

    /**
     * Trying to get a set of inventory-to added for this action
     * @return the set of InventoryType, null if there is none added
     */
    @Nullable
    public Set<InventoryType> getInventoryTo() {
        return getData(BanDataType.INVENTORY_TO);
    }

    /**
     * Trying to get a set of materials added for this action
     * @return the set of material, null if there is none added
     */
    @Nullable
    public Set<Material> getMaterial() {
        return getData(BanDataType.MATERIAL);
    }

    /**
     * Trying to check if the action should be logged
     * @return true if the action is logged, otherwise false
     */
    public boolean getLog() {
        return map.containsKey(BanDataType.LOG) && (boolean) map.get(BanDataType.LOG);
    }

    /**
     * Trying to get the messages added for this action
     * @return the list of messages, null if there is no messages added
     */
    @Nullable
    public List<String> getMessages() {
        return getData(BanDataType.MESSAGE);
    }

    /**
     * Trying to get the commands run for this action
     * @return the list of commands run, null if there is no commands added
     */
    @Nullable
    public List<String> getRun() {
        return getData(BanDataType.RUN);
    }

    /**
     * @return map containing players cooldowns time
     */
    @NotNull
    public Map<UUID, Long> getCooldowns() {
        return cooldowns;
    }

    /**
     * Get the map
     * @return the map
     */
    @NotNull
    public Map<BanDataType, Object> getMap() {
        return map;
    }

}
