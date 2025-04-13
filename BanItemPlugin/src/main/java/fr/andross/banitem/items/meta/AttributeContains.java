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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.utils.MinecraftVersion;
import fr.andross.banitem.utils.attributes.AttributeLegacy;
import fr.andross.banitem.utils.attributes.AttributeLevels;
import fr.andross.banitem.utils.attributes.ReflectionUtils;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.list.Listable;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * A simple meta comparator to compare attributes.
 *
 * @author EpiCanard
 * @version 3.4
 */
public final class AttributeContains extends MetaTypeComparator {
    private final Multimap<Object, AttributeLevels> attributes = HashMultimap.create();

    /**
     * Prepare the configured property to be compared with an item.
     *
     * @param o     the configured property value
     * @param debug the debug handler
     */
    public AttributeContains(final Object o, final Debug debug) {
        super(o);

        // Not available?
        if (!MinecraftVersion.v8OrMore) {
            debug.clone().add("&cTrying to use Attribute but you are below MC1.8.").sendDebug();
            setValid(false);
            return;
        }

        final List<String> configAttributes = Listable.getSplitStringList(o);

        for (final String attr : configAttributes) {
            final String[] s = attr.split(":");

            if (s.length == 0) {
                return;
            }

            // Extract attribute
            final Object attribute = getAttributeKey(s[0], debug);
            if (attribute == null) {
                return;
            }

            // 'Attribute': if the item contains this enchantment, does not consider the level;
            if (s.length == 1) {
                attributes.put(attribute, null);
                continue;
            }

            // 'Attribute:Level': if the item contains this enchantment with this level;
            if (s.length == 2) {
                final AttributeLevels.Comparator comparator = AttributeLevels.Comparator.fromString(s[1].substring(0, 1));
                final String value = (comparator == AttributeLevels.Comparator.EQUALS) ? s[1] : s[1].substring(1);

                final Double level = parseLevel(value, debug);
                if (level == null) {
                    return;
                }

                attributes.put(attribute, new AttributeLevels(level, comparator));
                continue;
            }

            // 'Attribute:MinLevel:MaxLevel': if the item contains this enchantment, within the min & max level interval [inclusive];
            final Double minLevel = parseLevel(s[1], debug);
            if (minLevel == null) {
                return;
            }
            final Double maxLevel = parseLevel(s[2], debug);
            if (maxLevel == null) {
                return;
            }

            attributes.put(attribute, new AttributeLevels(minLevel, maxLevel));
        }
    }

    @Override
    public boolean matches(@NotNull final BannedItem bannedItem) {
        if (bannedItem.getItemMeta() == null) {
            return false;
        }

        // Not an item ?
        if (bannedItem.getItemStack() == null && !bannedItem.getType().isItem()) {
            return false;
        }

        return getAttributesModifiers(bannedItem.toItemStack()).entries().stream()
                .filter(entry -> attributes.containsKey(entry.getKey()))
                .anyMatch(entry -> attributes.get(entry.getKey()).stream()
                        .anyMatch(levels -> levels == null || levels.matches(entry.getValue())));
    }

    /**
     * Get the attribute from config string.
     *
     * @param attributeName Name of attribute to find
     * @param debug         Debug
     * @return The Attribute found or null
     */
    @Nullable
    private Object getAttributeKey(@NotNull final String attributeName,
                                   @NotNull final Debug debug) {
        try {
            return (MinecraftVersion.v9OrMore) ? Attribute.valueOf(attributeName) : AttributeLegacy.valueOf(attributeName);
        } catch (final IllegalArgumentException e) {
            invalidateMetaType(debug, "Unknown attribute '" + attributeName + "'.");
            return null;
        }
    }

    /**
     * Extract attributes modifiers from item.
     *
     * @param itemStack ItemStack
     * @return A Multimap of attribute name and amount
     */
    @NotNull
    private Multimap<Object, Double> getAttributesModifiers(@NotNull final ItemStack itemStack) {
        final Multimap<Object, Double> map = HashMultimap.create();
        if (MinecraftVersion.v9OrMore) {
            // Extract attributes with bukkit api
            if (itemStack.getItemMeta() != null && itemStack.getItemMeta().getAttributeModifiers() != null) {
                itemStack.getItemMeta().getAttributeModifiers().entries().forEach(entry ->
                        map.put(entry.getKey(), entry.getValue().getAmount())
                );
            }
        } else {
            // Extract attributes with reflection from NMSItemStack (MC <1.9)
            try {
                final Object nmsItemStack = ReflectionUtils.asNMSCopy(itemStack);
                final Multimap<String, Object> multimap = ReflectionUtils.callMethodWithReturnType(nmsItemStack, Multimap.class);
                for (Map.Entry<String, Object> entry : multimap.entries()) {
                    final AttributeLegacy attribute = AttributeLegacy.valueFromName(entry.getKey());
                    if (attribute != null) {
                        map.put(attribute, ReflectionUtils.callMethodWithName(entry.getValue(), "d"));
                    }
                }
            } catch (final ClassNotFoundException | InvocationTargetException | IllegalAccessException |
                     NoSuchMethodException e) {
                Bukkit.getLogger().log(Level.WARNING, "AttributeContains reflection error.", e);
            }
        }
        return map;
    }

    /**
     * Parse the level from String to Double.
     *
     * @param level String level to parse
     * @param debug Debug
     * @return the parsed level or null
     */
    @Nullable
    private Double parseLevel(@NotNull final String level, @NotNull final Debug debug) {
        try {
            return Double.parseDouble(level);
        } catch (final NumberFormatException e) {
            invalidateMetaType(debug, "Invalid level '" + level + "'.");
            return null;
        }
    }

    /**
     * Send the debug message and invalid the comparator.
     *
     * @param debug   Debug
     * @param message Error message to send
     */
    private void invalidateMetaType(@NotNull final Debug debug, @NotNull final String message) {
        debug.clone().add("&c" + message).sendDebug();
        setValid(false);
    }
}