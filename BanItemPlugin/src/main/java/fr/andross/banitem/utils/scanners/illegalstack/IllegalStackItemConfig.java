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

import org.jetbrains.annotations.NotNull;

/**
 * A simple wrapper to store the amount and the block type of illegal stack material.
 *
 * @author Andross
 * @version 3.4
 */
public final class IllegalStackItemConfig {
    private final int amount;
    private final IllegalStackBlockType blockType;

    public IllegalStackItemConfig(final int amount,
                                  @NotNull final IllegalStackBlockType blockType) {
        this.amount = amount;
        this.blockType = blockType;
    }

    /**
     * Max amount allowed of the material.
     *
     * @return the max amount allowed of the material
     */
    public int getAmount() {
        return amount;
    }

    /**
     * The block type.
     *
     * @return the block type
     */
    @NotNull
    public IllegalStackBlockType getBlockType() {
        return blockType;
    }
}
