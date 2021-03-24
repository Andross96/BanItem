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
package fr.andross.banitem.utils.enchantments;

import fr.andross.banitem.utils.BanVersion;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An enchantment wrapper class that stores the Bukkit enchantment with a level
 * @version 3.1
 * @author Andross
 */
public final class EnchantmentWrapper {
    private final Enchantment enchantment;
    private final int level;

    public EnchantmentWrapper(@NotNull final Enchantment enchantment, final int level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    /**
     * Get the Bukkit enchantment
     * @return the bukkit enchantment
     */
    @NotNull
    public Enchantment getEnchantment() {
        return enchantment;
    }

    /**
     * Get the level of the enchantment
     * @return the level of the enchantment
     */
    public int getLevel() {
        return level;
    }

    /**
     * Create a set of this wrapper from a map of enchantements
     * @param map map of enchantments
     * @return set of this wrapper from the map
     */
    @NotNull
    public static Set<EnchantmentWrapper> from(@NotNull final Map<Enchantment, Integer> map) {
        return map.entrySet().stream().map(e -> new EnchantmentWrapper(e.getKey(), e.getValue())).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnchantmentWrapper that = (EnchantmentWrapper) o;
        return level == that.level && (BanVersion.v13OrMore ?
                Objects.equals(enchantment, that.enchantment) :
                Objects.equals(enchantment.getName(), that.enchantment.getName()));
    }

    @Override
    public int hashCode() {
        return BanVersion.v13OrMore ? Objects.hash(enchantment, level) : Objects.hash(enchantment.getName(), level);
    }
}
