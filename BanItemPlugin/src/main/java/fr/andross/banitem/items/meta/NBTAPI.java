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
import de.tr7zw.nbtapi.NBTType;
import fr.andross.banitem.utils.debug.Debug;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A simple meta comparator to compare NBT
 * @version 3.2
 * @author Andross
 */
public final class NBTAPI extends MetaTypeComparator {
    private final Map<List<String>, List<Object>> map = new HashMap<>();

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
            final List<Object> expectedValues = new ArrayList<>();

            // Multiple matches
            if (object instanceof ConfigurationSection) {
                final ConfigurationSection keySection = (ConfigurationSection) object;
                // Getting all objects
                for (final String objectName : keySection.getKeys(false)) {
                    final Object object2 = keySection.get(objectName);
                    if (object2 == null) {
                        debug.clone().add("&cUnknown object '" + objectName + "' for NBT node '" + keyNodes + "'.");
                        setValid(false);
                        return;
                    }
                    expectedValues.add(object2);
                }
            }

            // Simple NBT
            else
                expectedValues.add(object);


            if (!expectedValues.isEmpty())
                map.put(keys, expectedValues);
        }
    }

    @Override
    public boolean matches(@NotNull final ItemStack itemStack, @Nullable final ItemMeta itemMeta) {
        final NBTItem nbtItem = new NBTItem(itemStack);

        for (final Map.Entry<List<String>, List<Object>> e : map.entrySet()) {
            NBTCompound compound = getNBTCompoundFromPath(e.getKey(), nbtItem);
            if (compound == null) return false;

            // Matching object?
            String lastKey = e.getKey().get(e.getKey().size() - 1);
            if (!anyValueMatches(lastKey, e.getValue(), compound))
                return false;
        }

        return !map.entrySet().isEmpty();
    }

    private NBTCompound getNBTCompoundFromPath(List<String> keyList, final NBTCompound nbtItem) {
        NBTCompound compound = nbtItem;

        Iterator<String> keyIterator = keyList.iterator();
        while (keyIterator.hasNext()) {
            final String key = keyIterator.next();

            // Last one?
            if (!keyIterator.hasNext())
                break;

            // Get compound from array
            // Allows the use of paths like Items[0].id
            else if (isArray(key)) {
                String arrKey = removeArrStatement(key);
                if (!compound.hasKey(arrKey)) return null;
                compound = compound.getCompoundList(arrKey).get(getArrayIndex(key));
            }

            // Get compound from key
            else {
                if (!compound.hasKey(key)) return null;
                compound = compound.getCompound(key);
            }
        }

        return compound;
    }

    private static final Set<NBTType> SIMPLE_TYPES =  new HashSet<>(Arrays.asList(
            NBTType.NBTTagByte, NBTType.NBTTagDouble, NBTType.NBTTagFloat, NBTType.NBTTagInt,
            NBTType.NBTTagLong, NBTType.NBTTagShort, NBTType.NBTTagString
    ));
    private boolean anyValueMatches(String lastKey, List<Object> values, final NBTCompound nbtCompound) {
        // Can only expect to compare a value if it's a primitive/String
        if (!SIMPLE_TYPES.contains(nbtCompound.getType(lastKey))) return false;

        for (Object o : values) {
            if (equals(getSimpleDataFromCompound(nbtCompound, lastKey), o))
                return true;
        }

        return false;
    }

    private boolean isArray(final String key) {
        return key.endsWith("]");
    }

    private String removeArrStatement(final String key) {
        return key.replaceAll("\\[\\d*]$", "");
    }

    private int getArrayIndex(final String key) {
        return Integer.parseInt(key.replaceAll(".+\\[|]$", ""));
    }

    private Object getSimpleDataFromCompound(final NBTCompound compound, String key) {
        NBTType type = compound.getType(key);

        if (type == NBTType.NBTTagInt) return compound.getInteger(key);
        else if (type == NBTType.NBTTagShort) return compound.getShort(key);
        else if (type == NBTType.NBTTagLong) return compound.getLong(key);
        else if (type == NBTType.NBTTagFloat) return compound.getFloat(key);
        else if (type == NBTType.NBTTagDouble) return compound.getDouble(key);
        else if (type == NBTType.NBTTagByte) return compound.getByte(key);
        else return  compound.getString(key);
    }

    private boolean equals(Object simpleData, Object expected) {
        if (simpleData instanceof Number) {
            if (!(expected instanceof Number)) return false;
            Double sD = ((Number) simpleData).doubleValue();
            Double eD = ((Number) expected).doubleValue();

            return eD.compareTo(sD) == 0;
        }
        else if (simpleData instanceof String) {
            if (!(expected instanceof String)) return false;

            return ((String) simpleData).equals((expected));
        }


        return false;
    }
}
