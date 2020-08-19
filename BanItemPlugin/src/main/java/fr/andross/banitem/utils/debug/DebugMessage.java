/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2020 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.andross.banitem.utils.debug;

import fr.andross.banitem.utils.Listable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A debug message with its type
 * @version 2.4
 * @author Andross
 */
public final class DebugMessage {
    private final Listable.Type type;
    private final String node;

    public DebugMessage(@Nullable final Listable.Type type, @NotNull final String node) {
        this.type = type;
        this.node = node;
    }

    /**
     * Get the type of node
     * @return the type of node, null if its not an important node
     */
    @Nullable
    public Listable.Type getType() {
        return type;
    }

    /**
     * The the message for this node
     * @return the message of the node
     */
    @NotNull
    public String getNode() {
        return node;
    }
}
