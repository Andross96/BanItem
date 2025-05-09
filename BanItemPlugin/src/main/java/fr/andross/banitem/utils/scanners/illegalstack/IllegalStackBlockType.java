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
package fr.andross.banitem.utils.scanners.illegalstack;

/**
 * An enum for the block type of illegal stacked item.
 *
 * @author Andross
 * @version 3.4
 */
public enum IllegalStackBlockType {
    /**
     * Split the item, give it back to the player until the correct max stack size is respected
     */
    SPLIT,

    /**
     * Delete the whole stack
     */
    DELETE,

    /**
     * Delete only what's more from the max allowed stack size
     */
    DELETEMORE
}
