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
package fr.andross.banitem.utils.list;

/**
 * List of elements that can be listed.
 *
 * @author Andross
 * @version 3.1
 */
public enum ListType {
    /**
     * Represents a banned action.
     */
    ACTION,

    /**
     * Represents additional information about an action (specificities).
     */
    ACTIONDATA,

    /**
     * Represents a Minecraft enchantment.
     */
    ENCHANTMENT,

    /**
     * Represents a Minecraft entity.
     */
    ENTITY,

    /**
     * Represents a Minecraft game mode.
     */
    GAMEMODE,

    /**
     * Represents a Minecraft inventory type.
     */
    INVENTORY,

    /**
     * Represents a Minecraft item.
     */
    ITEM,

    /**
     * Represents a Minecraft item material.
     */
    MATERIAL,

    /**
     * Represents a BanItem custom item meta.
     */
    METATYPE,

    /**
     * Represents a WorldGuard protected region.
     */
    REGION,

    /**
     * Represents a Minecraft world.
     */
    WORLD,
}
