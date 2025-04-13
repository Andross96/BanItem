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
 * Only useful for Minecraft lower to 1.9.
 *
 * @author EpiCanard
 * @version 3.4
 */
public enum AttributeLegacy {
    /**
     * Represents the legacy generic max health attribute.
     */
    GENERIC_MAX_HEALTH("generic.maxHealth"),

    /**
     * Represents the legacy follow range attribute.
     */
    GENERIC_FOLLOW_RANGE("generic.followRange"),

    /**
     * Represents the legacy knock back resistance attribute.
     */
    GENERIC_KNOCKBACK_RESISTANCE("generic.knockbackResistance"),

    /**
     * Represents the legacy movement speed attribute.
     */
    GENERIC_MOVEMENT_SPEED("generic.movementSpeed"),

    /**
     * Represents the legacy flying speed attribute.
     */
    GENERIC_FLYING_SPEED("generic.flyingSpeed"),

    /**
     * Represents the legacy attack damage attribute.
     */
    GENERIC_ATTACK_DAMAGE("generic.attackDamage"),

    /**
     * Represents the legacy attack speed attribute.
     */
    GENERIC_ATTACK_SPEED("generic.attackSpeed"),

    /**
     * Represents the legacy generic armor attribute.
     */
    GENERIC_ARMOR("generic.armor"),

    /**
     * Represents the legacy armor toughness attribute.
     */
    GENERIC_ARMOR_TOUGHNESS("generic.armorToughness"),

    /**
     * Represents the legacy luck attribute.
     */
    GENERIC_LUCK("generic.luck"),

    /**
     * Represents the legacy horse jump strength attribute.
     */
    HORSE_JUMP_STRENGTH("horse.jumpStrength"),

    /**
     * Represents the legacy zombie spawn reinforcements attribute.
     */
    ZOMBIE_SPAWN_REINFORCEMENTS("zombie.spawnReinforcements");

    private final String name;

    /**
     * Enum constructor.
     *
     * @param name Legal attribute name
     */
    AttributeLegacy(@NotNull final String name) {
        this.name = name;
    }

    /**
     * Get the legacy name of the attribute.
     *
     * @return The legacy name of the attribute
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Retrieve an AttributeLegacy from its name.
     *
     * @param name Name of attribute to find
     * @return the AttributeLegacy matching or null
     */
    @Nullable
    public static AttributeLegacy valueFromName(final String name) {
        return Arrays.stream(AttributeLegacy.values()).filter(attr -> attr.name.equals(name)).findFirst().orElse(null);
    }
}