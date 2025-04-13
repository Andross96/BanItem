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
package fr.andross.banitem.actions;

import fr.andross.banitem.utils.enchantments.EnchantmentWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * A simple enumeration indicating what kind of data is used.
 *
 * @author Andross
 * @version 3.3
 */
public enum BanDataType {

    /**
     * Used to check if the banned item has a cooldown.
     * <p>Type: Long (millis)
     */
    COOLDOWN("cooldown"),

    /**
     * Used to get the custom/meta item name.
     * <p>Type: String
     */
    CUSTOMNAME("customname"),

    /**
     * Used to check if the enchantments are banned on an item.
     * <p>Type: Set of {@link EnchantmentWrapper}
     */
    ENCHANTMENT("enchantment"),

    /**
     * Used to check if the ban will apply on this entity.
     * <p>Type: Set of {@link org.bukkit.entity.EntityType}
     */
    ENTITY("entity"),

    /**
     * Used to check if the ban applies on current player gamemode.
     * <p>Type: Set of {@link org.bukkit.GameMode}
     */
    GAMEMODE("gamemode"),

    /**
     * Used to check if the ban will apply if the source inventory is included into this set.
     * <p>Type: Set of {@link org.bukkit.event.inventory.InventoryType}
     */
    INVENTORY_FROM("inventory-from"),

    /**
     * Used to check if the ban will apply if the destination inventory is included into this set.
     * <p>Type: Set of {@link org.bukkit.event.inventory.InventoryType}
     */
    INVENTORY_TO("inventory-to"),

    /**
     * Used to check if a log message will be sent to players with log activated.
     * <p>Type: boolean
     */
    LOG("log"),

    /**
     * Used to check if the ban will apply if a material is in the set.
     * <p>Type: Set of {@link org.bukkit.Material}
     */
    MATERIAL("material"),

    /**
     * Used to get the ban message(s).
     * <p>Type: List of <i>(already colored)</i> String
     */
    MESSAGE("message"),

    /**
     * Used to get a custom permission for an action.
     * <p>Type: String
     */
    PERMISSION("permission"),

    /**
     * Used to check if the ban applies into the region.
     * <p>Type: Set of {@link com.sk89q.worldguard.protection.regions.ProtectedRegion}
     */
    REGION("region"),

    /**
     * Used to run commands when the banned action occurs.
     * <p>Type: List of String
     */
    RUN("run");

    private final String name;

    BanDataType(@NotNull final String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
