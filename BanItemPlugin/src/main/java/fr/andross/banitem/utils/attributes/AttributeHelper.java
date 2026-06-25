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
package fr.andross.banitem.utils.attributes;

import fr.andross.banitem.utils.MinecraftVersion;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An attribute helper class to retrieve correct Minecraft
 * Attribute objects across versions.
 * <p>
 * In MC >= 26.1, {@link Attribute} changed from an enum to a registry-backed
 * interface, so {@code Attribute.valueOf()} no longer exists. This helper
 * abstracts that difference using the Registry API (via reflection, since the
 * compile-time Bukkit API predates it).
 *
 * @author Andross
 * @version 3.4
 */
public abstract class AttributeHelper {

    private AttributeHelper() {}

    /**
     * Try to get an {@link Attribute} object by name.
     * <p>
     * Accepts both old-style enum names (e.g. {@code GENERIC_MAX_HEALTH}) and
     * new-style NamespacedKey names (e.g. {@code generic.max_health} or
     * {@code minecraft:generic.max_health}).
     *
     * @param name the attribute name from config
     * @return the matching Attribute, or null if not found
     */
    @Nullable
    public static Attribute getAttribute(@NotNull final String name) {
        if (MinecraftVersion.v26OrMore) {
            // Attribute is a registry-backed interface on MC 26.1+; use Registry via reflection
            // Try direct lookup (user may have typed "generic.max_health" or "minecraft:generic.max_health")
            Attribute attr = tryRegistryLookup(name.toLowerCase());
            if (attr != null) {
                return attr;
            }

            // Convert old-style enum name (GENERIC_MAX_HEALTH → generic.max_health)
            return tryRegistryLookup(convertEnumNameToKey(name));
        } else {
            // Attribute is still an enum on MC < 26.1
            try {
                return Attribute.valueOf(name.toUpperCase());
            } catch (final IllegalArgumentException e) {
                return null;
            }
        }
    }

    /**
     * Convert an old Bukkit enum-style attribute name to a NamespacedKey string.
     * Splits at the first underscore: {@code GENERIC_MAX_HEALTH} → {@code generic.max_health}.
     */
    @NotNull
    private static String convertEnumNameToKey(@NotNull final String name) {
        final int firstUnderscore = name.indexOf('_');
        if (firstUnderscore < 0) return name.toLowerCase();
        return name.substring(0, firstUnderscore).toLowerCase()
                + '.'
                + name.substring(firstUnderscore + 1).toLowerCase();
    }

    /**
     * Attempt a Registry.ATTRIBUTE lookup via reflection.
     * Reflection is required because Registry.ATTRIBUTE was added after the
     * compile-time Bukkit API version this project targets.
     */
    @Nullable
    private static Attribute tryRegistryLookup(@NotNull final String key) {
        try {
            final int colon = key.indexOf(':');
            final NamespacedKey namespacedKey;
            if (colon >= 0) {
                namespacedKey = new NamespacedKey(key.substring(0, colon), key.substring(colon + 1));
            } else {
                namespacedKey = NamespacedKey.minecraft(key);
            }

            final Class<?> registryClass = Class.forName("org.bukkit.Registry");
            final Field attributeField = registryClass.getField("ATTRIBUTE");
            final Object attributeRegistry = attributeField.get(null);
            final Method getMethod = attributeRegistry.getClass().getMethod("get", NamespacedKey.class);
            return (Attribute) getMethod.invoke(attributeRegistry, namespacedKey);
        } catch (final Exception e) {
            Logger.getLogger("BanItem").log(Level.FINE, "AttributeHelper registry lookup failed for key: " + key, e);
            return null;
        }
    }
}
