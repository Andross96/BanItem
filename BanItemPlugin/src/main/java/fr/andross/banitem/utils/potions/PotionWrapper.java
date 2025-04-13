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

import fr.andross.banitem.utils.MinecraftVersion;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A potion wrapper class that stores the Bukkit potion type with the level.
 *
 * @author Andross
 * @version 3.1
 */
public final class PotionWrapper {
    private final PotionEffectType potionEffectType;
    private final int level;

    public PotionWrapper(@NotNull final PotionEffectType potionEffectType, final int level) {
        this.potionEffectType = potionEffectType;
        this.level = level;
    }

    /**
     * Get the PotionEffectType.
     *
     * @return the PotionEffectType
     */
    @NotNull
    public PotionEffectType getPotionEffectType() {
        return potionEffectType;
    }

    /**
     * Get the level of the potion effect.
     *
     * @return the level of the potion effect
     */
    public int getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotionWrapper that = (PotionWrapper) o;
        return level == that.level && (MinecraftVersion.v13OrMore ?
                Objects.equals(potionEffectType, that.potionEffectType) :
                Objects.equals(potionEffectType.getName(), that.potionEffectType.getName()));
    }

    @Override
    public int hashCode() {
        return MinecraftVersion.v13OrMore ? Objects.hash(potionEffectType, level) : Objects.hash(potionEffectType.getName(), level);
    }
}
