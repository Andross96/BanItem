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

import fr.andross.banitem.items.meta.MetaType;
import fr.andross.banitem.items.meta.MetaTypeComparator;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.list.ListType;
import fr.andross.banitem.utils.list.Listable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

/**
 * An item wrapper, which store custom item meta.
 *
 * @author Andross
 * @version 3.3.2
 */
public final class CustomBannedItem extends BannedItem implements ICustomName {
    private final String name;
    private final Set<Material> materials = EnumSet.noneOf(Material.class);
    private final Map<MetaType, MetaTypeComparator> meta = new EnumMap<>(MetaType.class);
    private boolean valid = true;
    private boolean reverted = false;

    /**
     * Represents a custom item, handled by BanItem, which will match specific meta types
     * on the item.
     *
     * @param name    name of the custom item
     * @param section the section in the configuration file
     * @param debug   the debug
     */
    public CustomBannedItem(@NotNull final String name,
                            @NotNull final ConfigurationSection section,
                            @NotNull final Debug debug) {
        super(Material.AIR);
        this.name = name;
        final Object materialsObject = section.get("material");

        if (materialsObject == null) {
            debug.add("material").clone().add(ListType.ITEM, "&cNo material set.").sendDebug();
            valid = false;
            return;
        }

        // Loading materials
        final List<Material> materials = Listable.getMaterials(materialsObject, debug.clone().add("material"));
        if (materials.isEmpty()) {
            valid = false;
            return;
        }
        this.materials.addAll(materials);

        // Meta
        for (final String key : section.getKeys(false)) {
            if (key.equalsIgnoreCase("material")) {
                continue;
            }
            if (key.equalsIgnoreCase("reverted")) {
                reverted = section.getBoolean(key);
                continue;
            }

            // Getting type
            final MetaType type;
            try {
                type = MetaType.valueOf(key.toUpperCase().replace("-", "_"));
            } catch (final Exception e) {
                debug.clone().add(ListType.METATYPE, "&cUnknown meta type &e&l" + key + "&c.").sendDebug();
                continue;
            }

            // Creating comparator
            try {
                final MetaTypeComparator comparator = type.getClazz()
                        .getDeclaredConstructor(Object.class, Debug.class)
                        .newInstance(section.get(key), debug.clone().add(key));
                if (!comparator.isValid()) {
                    valid = false;
                    return;
                }
                meta.put(type, comparator);
            } catch (final Exception e) {
                debug.clone().add(ListType.METATYPE, "&cError loading &e&l" + key + "&c. More information on console.").sendDebug();
                Bukkit.getLogger().log(Level.WARNING, "Error loading '" + key + "'.", e);
            }
        }
    }

    /**
     * Comparing the ItemMeta of the item with the item meta stored.
     *
     * @param bannedItem the item to compare
     * @return true if the item meta matches, otherwise false
     */
    public boolean matches(@NotNull final BannedItem bannedItem) {
        // Matching material?
        if (!materials.contains(bannedItem.getType())) {
            return false;
        }

        // All meta are matching?
        if (!reverted) {
            for (final Map.Entry<MetaType, MetaTypeComparator> e : meta.entrySet()) {
                if (!e.getValue().matches(bannedItem)) {
                    return false;
                }
            }
            return true;
        }

        // Reverted custom item! (matching everything that does not match)
        for (final Map.Entry<MetaType, MetaTypeComparator> e : meta.entrySet()) {
            if (!e.getValue().matches(bannedItem)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the custom banned item name from customitems.yml.
     *
     * @return the custom banned item name from customitems.yml
     */
    @NotNull
    @Override
    public String getName() {
        return name;
    }

    /**
     * Get the materials represented by this custom item.
     *
     * @return set of materials
     */
    @NotNull
    public Set<Material> getMaterials() {
        return materials;
    }

    /**
     * Get the map of meta to compare.
     *
     * @return the map of meta to compare
     */
    @NotNull
    public Map<MetaType, MetaTypeComparator> getMeta() {
        return meta;
    }

    /**
     * If the custom item is correctly loaded.
     *
     * @return if the custom item is correctly loaded
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * If the custom item match is reverted.
     *
     * @return if the custom item match is reverted
     */
    public boolean isReverted() {
        return reverted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomBannedItem that = (CustomBannedItem) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
