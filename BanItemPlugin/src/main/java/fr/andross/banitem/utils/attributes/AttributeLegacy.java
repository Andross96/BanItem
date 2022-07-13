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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * An enum which represents the minecraft attributes.
 * Only useful for Minecraft lower to 1.9
 * @version 3.4
 * @author EpiCanard
 */
public enum AttributeLegacy {
    GENERIC_MAX_HEALTH("generic.maxHealth"),
    GENERIC_FOLLOW_RANGE("generic.followRange"),
    GENERIC_KNOCKBACK_RESISTANCE("generic.knockbackResistance"),
    GENERIC_MOVEMENT_SPEED("generic.movementSpeed"),
    GENERIC_FLYING_SPEED("generic.flyingSpeed"),
    GENERIC_ATTACK_DAMAGE("generic.attackDamage"),
    GENERIC_ATTACK_SPEED("generic.attackSpeed"),
    GENERIC_ARMOR("generic.armor"),
    GENERIC_ARMOR_TOUGHNESS("generic.armorToughness"),
    GENERIC_LUCK("generic.luck"),
    HORSE_JUMP_STRENGTH("horse.jumpStrength"),
    ZOMBIE_SPAWN_REINFORCEMENTS("zombie.spawnReinforcements");

    private final String name;

    AttributeLegacy(@NotNull final String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Retrieve an AttributeLegacy from its name
     * @param name Name of attribute to find
     * @return the AttributeLegacy matching or null
     */
    @Nullable
    public static AttributeLegacy valueFromName(final String name) {
        return Arrays.stream(AttributeLegacy.values()).filter(attr -> attr.name.equals(name)).findFirst().orElse(null);
    }
}