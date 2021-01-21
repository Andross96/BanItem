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
package fr.andross.banitem.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple double map, used to handle custom and meta items.
 * @version 3.0
 * @author Andross
 */
public class DoubleMap<K, V> extends HashMap<K, V> {
    private final Map<V, K> reversed = new HashMap<>();

    /**
     * Adding in both maps
     * @param key key
     * @param value value
     * @see Map#put(Object, Object)
     */
    @Override
    public V put(@NotNull final K key, @Nullable final V value) {
        reversed.put(value, key);
        return super.put(key, value);
    }

    /**
     * Will clear the map and its reversed one
     */
    @Override
    public void clear() {
        reversed.clear();
        super.clear();
    }

    /**
     * Remove the key from both maps
     * @param key key
     * @see Map#remove(Object)
     */
    @Override
    public V remove(@NotNull final Object key) {
        reversed.remove(get(key));
        return super.remove(key);
    }

    /**
     * Get the key of this value from the reversed map
     * @param value value
     * @return the key of this value if exists, otherwise null
     */
    @Nullable
    public K getKey(@NotNull final V value) {
        return reversed.get(value);
    }

    /**
     * Get the reversed map
     * @return the reversed map
     */
    @NotNull
    public Map<V, K> getReversed() {
        return reversed;
    }
}
