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
package fr.andross.banitem.utils.potions;

import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A potion helper class to retrieve correct Bukkit PotionEffectType object across versions.
 *
 * @author Andross
 * @version 3.1.1
 */
public final class PotionHelper {
    private static final List<String> potionNames = Arrays.stream(PotionEffectType.values()).filter(Objects::nonNull).map(PotionEffectType::getName).collect(Collectors.toList()); // some of them are null in 1.8, why?!
    private static final Map<String, String> names = new HashMap<>();

    static {
        add("heal", "health", "healing", "instant_heal");
        add("fire_resistance", "fireresistance", "fireresist");
        add("regeneration", "regen");
        add("increase_damage", "strength", "increasedamage");
        add("speed", "swiftness", "swift");
        add("night_vision", "nightvision");
        add("water_breathing", "waterbreathing");
        add("jump", "jumping", "jumpboost", "jump_boost", "leap", "leaping");
        add("slow_falling", "slowfalling");
        add("harm", "harming", "instant_damage", "instantdamage");
        add("slow", "slowness", "turtle_master", "turtlemaster");
        add("fast_digging", "haste");
        add("damage_resistance", "resistance", "damageresistance");
    }

    private static void add(final String bukkitName, final String... friendlyNames) {
        for (final String friendlyName : friendlyNames)
            names.put(friendlyName, bukkitName);
    }

    /**
     * Try to get a {@link PotionEffectType} object by the name.
     *
     * @param name name of the potion effect type
     * @return the bukkit PotionEffectType, null if not found
     */
    @Nullable
    public static PotionEffectType getPotionEffectType(@NotNull final String name) {
        // Getting by name?
        final PotionEffectType potionEffectType = PotionEffectType.getByName(name);
        if (potionEffectType != null) {
            return potionEffectType;
        }

        // Last chance, getting by friendly names above ?
        return names.containsKey(name.toLowerCase()) ? PotionEffectType.getByName(names.get(name)) : null;
    }

    /**
     * Try to get a {@link PotionWrapper} from a string which syntax must be 'potion:level'.
     *
     * @param potion the string
     * @return aa PotionWrapper if valid, otherwise null
     */
    @Nullable
    public static PotionWrapper getPotionWrapper(@NotNull final String potion) {
        final String[] s = potion.split(":");
        if (s.length != 2) {
            return null;
        }
        final PotionEffectType potionEffectType = getPotionEffectType(s[0]);
        if (potionEffectType == null) {
            return null;
        }
        final int level;
        try {
            level = Integer.parseInt(s[1]);
        } catch (final NumberFormatException e) {
            return null;
        }
        return new PotionWrapper(potionEffectType, level);
    }

    @NotNull
    public static List<String> getPotionNames() {
        return potionNames;
    }
}

