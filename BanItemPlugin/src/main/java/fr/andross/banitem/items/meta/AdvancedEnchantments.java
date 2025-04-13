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

import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.list.Listable;
import net.advancedplugins.ae.api.AEAPI;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A simple meta comparator to compare the AdvancedEnchantments.
 *
 * @author Andross
 * @version 3.2
 */
public final class AdvancedEnchantments extends MetaTypeComparator {
    private final Set<String> enchantsWithoutLevels = new HashSet<>(); // Any enchantment levels
    private final Map<String, Integer> enchants = new HashMap<>(); // Specific enchantments with specific levels
    private final Map<Object, Integer[]> enchantsIntervals = new HashMap<>(); // Enchantment interval

    /**
     * Prepare the configured property to be compared with an item.
     *
     * @param o     the configured property value
     * @param debug the debug handler
     */
    public AdvancedEnchantments(final Object o, final Debug debug) {
        super(o);

        // Not available?
        try {
            Class.forName("n3kas.ae.api.AEAPI");
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            debug.clone().add("&cTrying to use AdvancedEnchantments but the plugin is not enabled.").sendDebug();
            setValid(false);
            return;
        }

        for (final String string : Listable.getSplitStringList(o)) {
            final String[] s = string.split(":");

            // 'Enchantment': if the item contains this enchantment, does not consider the level;
            if (s.length == 1) {
                enchantsWithoutLevels.add(s[0].toLowerCase(Locale.ROOT));
                continue;
            }

            // 'Enchantment:Level': if the item contains this enchantment with this level;
            if (s.length == 2) {
                final int level;
                try {
                    level = Integer.parseInt(s[1]);
                } catch (final NumberFormatException e) {
                    debug.clone().add("&cInvalid level '" + s[1] + "' in value '" + string + "'.").sendDebug();
                    setValid(false);
                    return;
                }

                // Adding
                enchants.put(s[0].toLowerCase(Locale.ROOT), level);
                continue;
            }

            // 'Enchantment:MinLevel:MaxLevel': if the item contains this enchantment, within the min & max level interval [inclusive];
            if (s.length >= 3) {
                final int minLevel;
                try {
                    minLevel = Integer.parseInt(s[1]);
                } catch (final NumberFormatException e) {
                    debug.clone().add("&cInvalid minimum level '" + s[1] + "' in value '" + string + "'.").sendDebug();
                    setValid(false);
                    return;
                }

                final int maxLevel;
                try {
                    maxLevel = Integer.parseInt(s[2]);
                } catch (final NumberFormatException e) {
                    debug.clone().add("&cInvalid maximum level '" + s[2] + "' in value '" + string + "'.").sendDebug();
                    setValid(false);
                    return;
                }

                // Adding
                enchantsIntervals.put(s[0].toLowerCase(Locale.ROOT), new Integer[]{minLevel, maxLevel});
            }
        }
    }

    @Override
    public boolean matches(@NotNull final BannedItem bannedItem) {
        // Not an Item ?
        if (bannedItem.getItemStack() == null && !bannedItem.getType().isItem()) {
            return false;
        }

        final Map<String, Integer> enchantsOnItem = AEAPI.getEnchantmentsOnItem(bannedItem.toItemStack());
        if (enchantsOnItem.isEmpty()) {
            return false;
        }
        for (final Map.Entry<String, Integer> e : enchantsOnItem.entrySet()) {
            final String enchantment = e.getKey().toLowerCase(Locale.ROOT);
            final int level = e.getValue();

            // Containing enchantment (not considering level) ?
            if (enchantsWithoutLevels.contains(enchantment)) {
                return true;
            }

            // Containing enchantment (considering level)?
            if (enchants.containsKey(enchantment) && enchants.get(enchantment) == level) {
                return true;
            }

            // Containing enchantment (in interval)?
            if (enchantsIntervals.containsKey(enchantment)) {
                final Integer[] interval = enchantsIntervals.get(enchantment);
                if (level >= interval[0] && level <= interval[1]) {
                    return true;
                }
            }
        }
        return false;
    }
}
