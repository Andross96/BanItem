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
package fr.andross.banitem.utils.statics;

import fr.andross.banitem.utils.BanVersion;
import fr.andross.banitem.utils.EnchantmentWrapper;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An enchantment helper class to retrieve correct Bukkit enchantments object across versions
 * @version 3.0
 * @author Andross
 */
public final class EnchantmentHelper {
    private static final Map<String, String> names = new HashMap<>();

    static {
        add("water_worker", "aqua_affinity", "aquaaffinity", "aa");
        add("damage_arthropods", "bane_of_arthropods", "bane_arthropods", "baneofarthropods", "banearthropods", "boa");
        add("protection_explosions", "blast_protection", "blastprotection", "bp");
        add("binding_curse", "curse_of_binding", "bindingcurse", "curse_binding", "curseofbinding", "cursebinding", "cob");
        add("vanishing_curse", "curse_of_vanishing", "curseofvanishing", "curse_vanishing", "cursevanishing", "cov");
        add("depth_strider", "depthstrider", "ds");
        add("dig_speed", "efficiency", "digspeed", "eff");
        add("protection_fall", "feather_falling", "featherfalling", "protectionfall", "fallprotection", "ff");
        add("fire_aspect", "fireaspect", "fa");
        add("protection_fire", "fire_protection", "fireprotection", "protectionfire", "fp");
        add("arrow_fire", "flame");
        add("loot_bonus_blocks", "fortune");
        add("frost_walker", "frost_walker", "fw");
        add("arrow_infinite", "infinity");
        add("loot_bonus_mobs", "looting");
        add("luck", "luck_of_the_sea", "luckofthesea", "lots");
        add("arrow_damage", "power");
        add("protection_projectile", "projectile_protection", "projectileprotection", "pp");
        add("protection_environmental", "protection", "p");
        add("arrow_knockback", "punch");
        add("oxygen", "respiration", "resp");
        add("damage_all", "sharpness");
        add("damage_undead", "smite", "damageundead");
        add("sweeping_edge", "sweeping");
        add("durability", "unbreaking");
    }

    private static void add(final String bukkitName, final String... friendlyNames) {
        for (final String friendlyName : friendlyNames)
            names.put(friendlyName, bukkitName);
    }

    /**
     * Try to get an {@link Enchantment} object by the name
     * @param name name of the enchantment
     * @return a bukkit enchantment object, null if not found
     */
    @Nullable
    public static Enchantment getEnchantment(@NotNull final String name) {
        Enchantment ench;

        // Getting by key?
        if (BanVersion.v13OrMore) {
            ench = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(name.toLowerCase()));
            if (ench != null) return ench;
        }

        // Getting by name?
        ench = Enchantment.getByName(name.toUpperCase());
        if (ench != null) return ench;

        // Last chance, getting by my friendly names?
        return names.containsKey(name.toLowerCase()) ? Enchantment.getByName(names.get(name).toUpperCase()) : null;
    }

    /**
     * Try to get an {@link EnchantmentWrapper} from a string which synthax must be 'enchantment:level'
     * @param enchant the string
     * @return an EnchantmentWrapper if valid, otherwise null
     */
    @Nullable
    public static EnchantmentWrapper getEnchantmentWrapper(@NotNull final String enchant) {
        final String[] s = enchant.split(":");
        if (s.length != 2) return null;
        final Enchantment enchantment = getEnchantment(s[0]);
        if (enchantment == null) return null;
        final int level;
        try {
            level = Integer.parseInt(s[1]);
        } catch (final NumberFormatException e) {
            return null;
        }
        return new EnchantmentWrapper(enchantment, level);
    }

    /**
     * Get all enchantmentwrappers (all levels) for a bukkit Enchantment
     * @param enchantment the bukkit enchantment
     * @return a list which contains all levels
     */
    @NotNull
    public static List<EnchantmentWrapper> getAllEnchantmentWrappers(@NotNull final Enchantment enchantment) {
        final List<EnchantmentWrapper> list = new ArrayList<>();
        for (int i = 1; i < enchantment.getMaxLevel(); i++)
            list.add(new EnchantmentWrapper(enchantment, i));
        return list;
    }
}
