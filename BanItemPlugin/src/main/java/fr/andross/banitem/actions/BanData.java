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
 * The ban data used into a ban check
 * The object will be an instance of the described {@link BanDataType}
 * Example: if type == BanDataType.CREATIVE, the object is a boolean.
 * @version 3.1
 * @author Andross
 */
public final class BanData {
    private final BanDataType type;
    private final Object o;

    public BanData(@NotNull final BanDataType type, @NotNull final Object o) {
        this.type = type;
        this.o = o;
    }

    /**
     * @return the ban data type
     */
    @NotNull
    public BanDataType getType() {
        return type;
    }

    /**
     * @return the object, which is an instance described by the {@link BanDataType}
     */
    @NotNull
    public Object getObject() {
        return o;
    }
}
