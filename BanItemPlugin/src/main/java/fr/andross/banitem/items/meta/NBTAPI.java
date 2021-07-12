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
package fr.andross.banitem.items.meta;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import fr.andross.banitem.utils.debug.Debug;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * A simple meta comparator to compare NBT
 * @version 3.2
 * @author Andross
 */
public final class NBTAPI extends MetaTypeComparator {
    private final Map<List<String>, List<BiPredicate<NBTCompound, String>>> map = new HashMap<>();

    public NBTAPI(final Object o, final Debug debug) {
        super(o);

        // Not available?
        try {
            Class.forName("de.tr7zw.nbtapi.NBTItem");
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            debug.clone().add("&cTrying to use NBTAPI but the plugin is not enabled.").sendDebug();
            setValid(false);
            return;
        }

        if (!(o instanceof ConfigurationSection)) {
            debug.clone().add("&cInvalid NBTAPI configuration synthax.").sendDebug();
            setValid(false);
            return;
        }

        // Loading
        final ConfigurationSection section = (ConfigurationSection) o;
        for (final String keyNodes : section.getKeys(false)) {
            final Object object = section.get(keyNodes);
            if (object == null) continue;

            // Preparing variables
            final List<String> keys = Arrays.asList(keyNodes.split("#"));
            final List<BiPredicate<NBTCompound, String>> predicates = new ArrayList<>();

            // Multiple matches
            if (object instanceof ConfigurationSection) {
                final ConfigurationSection keySection = (ConfigurationSection) object;
                // Getting all objects
                for (final String objectName : keySection.getKeys(false)) {
                    final Object object2 = keySection.get(objectName);
                    final BiPredicate<NBTCompound, String> predicate = getPredicate(object2);
                    if (predicate == null) {
                        debug.clone().add("&cUnknown object '" + objectName + "' for NBT node '" + keyNodes + "'.");
                        setValid(false);
                        return;
                    }
                    predicates.add(predicate);
                }
            } else { // Simple NBT
                final BiPredicate<NBTCompound, String> predicate = getPredicate(object);
                if (predicate == null) {
                    debug.clone().add("&cUnknown object '" + object + "' for NBT node '" + keyNodes + "'.");
                    setValid(false);
                    return;
                }
                predicates.add(predicate);
            }

            if (!predicates.isEmpty())
                map.put(keys, predicates);
        }
    }

    @Nullable
    private BiPredicate<NBTCompound, String> getPredicate(@Nullable final Object o) {
        if (o instanceof String) return (c, k) -> o.equals(c.getString(k));
        else if (o instanceof Boolean) return (c, k) -> o.equals(c.getBoolean(k));
        else if (o instanceof Byte) return (c, k) -> o.equals(c.getByte(k));
        else if (o instanceof Double) return (c, k) -> o.equals(c.getDouble(k));
        else if (o instanceof Float) return (c, k) -> o.equals(c.getFloat(k));
        else if (o instanceof Integer) return (c, k) -> o.equals(c.getInteger(k));
        else if (o instanceof Long) return (c, k) -> o.equals(c.getLong(k));
        else if (o instanceof Short) return (c, k) -> o.equals(c.getShort(k));
        else if (o instanceof UUID) return (c, k) -> o.equals(c.getUUID(k));
        else if (o instanceof List) return (c, k) -> o.equals(c.getStringList(k));
        else if (o instanceof ItemStack) return (c, k) -> {
            final ItemStack item = c.getItemStack(k);
            return item != null && item.isSimilar((ItemStack) o);
        };
        else return null;
    }

    @Override
    public boolean matches(@NotNull final ItemStack itemStack, @Nullable final ItemMeta itemMeta) {
        final NBTItem nbtItem = new NBTItem(itemStack);

        for (final Map.Entry<List<String>, List<BiPredicate<NBTCompound, String>>> e : map.entrySet()) {
            final List<String> nodes = e.getKey();
            String lastKey = null;
            NBTCompound compound = nbtItem;

            // Getting Compound
            for (int i = 0; i < nodes.size(); i++) {
                final String key = nodes.get(i);
                // Last one?
                if (i == nodes.size() - 1) {
                    lastKey = key;
                    break;
                } else {
                    if (!compound.hasKey(key)) return false;
                    compound = compound.getCompound(key);
                }
            }

            if (lastKey == null || compound == null) return false;

            // Matching object?
            for (final BiPredicate<NBTCompound, String> predicate : e.getValue())
                if (predicate.test(compound, lastKey))
                    return true;
        }
        return false;
    }
}
