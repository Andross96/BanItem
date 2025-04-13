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

import org.jetbrains.annotations.NotNull;

/**
 * The ban data used into a ban check.
 * The object will be an instance of the described {@link BanDataType}.
 * Example: <code>if (type == BanDataType.CREATIVE)</code>, the object is a boolean.
 *
 * @author Andross
 * @version 3.1
 */
public final class BanData {
    private final BanDataType type;
    private final Object object;

    /**
     * Constructor a BanData object, where the type represents the type of data and the
     * object represents an instance of the type as described in its documentation {@link BanDataType}.
     *
     * @param type   Type of ban data
     * @param object Object related to the ban data
     */
    public BanData(@NotNull final BanDataType type, @NotNull final Object object) {
        this.type = type;
        this.object = object;
    }

    /**
     * Ban data type.
     *
     * @return the ban data type
     */
    @NotNull
    public BanDataType getType() {
        return type;
    }

    /**
     * Object, which is an instance described by the {@link BanDataType}.
     *
     * @return the object, which is an instance described by the {@link BanDataType}
     */
    @NotNull
    public Object getObject() {
        return object;
    }
}
