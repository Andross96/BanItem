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
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An item wrapper, which store custom item meta
 * @version 3.1
 * @author Andross
 */
public final class CustomBannedItem extends BannedItem {
    private final String name;
    private final Set<Material> materials = EnumSet.noneOf(Material.class);
    private final Map<MetaType, MetaTypeComparator> meta = new EnumMap<>(MetaType.class);
    private boolean valid = true;

    public CustomBannedItem(@NotNull final String name, @NotNull final ConfigurationSection section, @NotNull final Debug debug) {
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
            if (key.equalsIgnoreCase("material")) continue;

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
                final MetaTypeComparator comparator = type.getClazz().getDeclaredConstructor(Object.class, Debug.class).newInstance(section.get(key), debug.clone().add(key));
                if (!comparator.isValid()) {
                    valid = false;
                    return;
                }
                meta.put(type, comparator);
            } catch (final Exception e) {
                debug.clone().add(ListType.METATYPE, "&cError loading &e&l" + key + "&c. More information on console.").sendDebug();
                e.printStackTrace();
            }
        }
    }

    /**
     * Comparing the ItemMeta of the item with the item meta stored
     * @param item the item stack to compare
     * @return true if the item meta matches, otherwise false
     */
    public boolean matches(@NotNull final ItemStack item) {
        // Matching material?
        if (!materials.contains(item.getType())) return false;

        // All meta are matching?
        final ItemMeta itemMeta = item.getItemMeta();
        for (final Map.Entry<MetaType, MetaTypeComparator> e : meta.entrySet())
            if (!e.getValue().matches(item, itemMeta))
                return false;

        return true;
    }

    /**
     * Get the custom banned item name from customitems.yml
     * @return the custom banned item name from customitems.yml
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Get the materials represented by this custom item
     * @return set of materials
     */
    @NotNull
    public Set<Material> getMaterials() {
        return materials;
    }

    /**
     * Get the map of meta to compare
     * @return the map of meta to compare
     */
    @NotNull
    public Map<MetaType, MetaTypeComparator> getMeta() {
        return meta;
    }

    /**
     * If the custom item is correctly loaded
     * @return if the custom item is correctly loaded
     */
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CustomBannedItem that = (CustomBannedItem) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}
