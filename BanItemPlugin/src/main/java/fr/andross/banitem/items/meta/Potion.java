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
import fr.andross.banitem.utils.MinecraftVersion;
import fr.andross.banitem.utils.Utils;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.list.Listable;
import fr.andross.banitem.utils.potions.PotionHelper;
import fr.andross.banitem.utils.potions.PotionWrapper;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple meta comparator to compare potions.
 *
 * @author Andross
 * @version 3.4.1
 */
public final class Potion extends MetaTypeComparator {
    private final Set<Object> potionsWithoutLevels = new HashSet<>();
    private final Set<PotionWrapper> potions = new HashSet<>(); // Specific potions with specific levels
    private final Map<Object, Integer[]> potionsIntervals = new HashMap<>();

    /**
     * Prepare the configured property to be compared with an item.
     *
     * @param configurationProperties the configured property value
     * @param debug                   the debug handler
     */
    public Potion(final Object configurationProperties, final Debug debug) {
        super(configurationProperties, debug);

        for (final String string : Listable.getSplitStringList(configurationProperties)) {
            final String[] s = string.split(":");

            // 'Potion': if the item contains this potion effect, does not consider the level;
            if (s.length == 1) {
                final PotionEffectType potionEffectType = PotionHelper.getPotionEffectType(s[0]);
                if (potionEffectType == null) {
                    debug.clone().add("&cUnknown potion effect '" + s[0] + "'.").sendDebug();
                    setValid(false);
                    return;
                }

                // Adding
                potionsWithoutLevels.add(MinecraftVersion.v13OrMore ? potionEffectType : potionEffectType.getName());
                continue;
            }

            // 'Potion:Level': if the item contains this potion effect with this level;
            if (s.length == 2) {
                final PotionEffectType potionEffectType = PotionHelper.getPotionEffectType(s[0]);
                if (potionEffectType == null) {
                    debug.clone().add("&cUnknown potion effect '" + s[0] + "'.").sendDebug();
                    setValid(false);
                    return;
                }

                final int level;
                try {
                    level = Integer.parseInt(s[1]);
                } catch (final NumberFormatException e) {
                    debug.clone().add("&cInvalid level '" + s[1] + "' in value '" + string + "'.").sendDebug();
                    setValid(false);
                    return;
                }

                // Adding
                potions.add(new PotionWrapper(potionEffectType, level));
                continue;
            }

            // 'Potion:MinLevel:MaxLevel': if the item contains this potion effect, within the min & max level interval [inclusive];
            if (s.length >= 3) {
                final PotionEffectType potionEffectType = PotionHelper.getPotionEffectType(s[0]);
                if (potionEffectType == null) {
                    debug.clone().add("&cUnknown potion effect '" + s[0] + "'.").sendDebug();
                    setValid(false);
                    return;
                }

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
                potionsIntervals.put(MinecraftVersion.v13OrMore ? potionEffectType : potionEffectType.getName(), new Integer[]{minLevel, maxLevel});
            }
        }
    }

    @Override
    public boolean matches(@NotNull final BannedItem bannedItem) {
        // Not an item ?
        if (bannedItem.getItemStack() == null && !bannedItem.getType().isItem()) {
            return false;
        }

        final Map<PotionEffectType, Integer> potions = Utils.getAllPotionEffects(bannedItem.toItemStack());
        if (potions.isEmpty()) {
            return false;
        }

        for (final Map.Entry<PotionEffectType, Integer> e : potions.entrySet()) {
            final PotionEffectType potionEffectType = e.getKey();

            if (potionEffectType == null) {
                continue;
            }

            final int level = e.getValue();
            final Object object = MinecraftVersion.v13OrMore ? potionEffectType : potionEffectType.getName();

            // Containing potion effect (not considering level) ?
            if (potionsWithoutLevels.contains(object)) {
                return true;
            }

            // Containing potion effect (considering level)?
            if (this.potions.contains(new PotionWrapper(potionEffectType, level))) {
                return true;
            }

            // Containing potion effect (in interval)?
            if (potionsIntervals.containsKey(object)) {
                final Integer[] interval = potionsIntervals.get(object);
                if (level >= interval[0] && level <= interval[1]) {
                    return true;
                }
            }
        }

        return false;
    }
}
