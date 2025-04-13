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
package fr.andross.banitem.utils.debug;

import fr.andross.banitem.utils.list.ListType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A debug message with its type.
 *
 * @author Andross
 * @version 3.1
 */
public final class DebugMessage {
    private final ListType type;
    private final String node;

    public DebugMessage(@NotNull final String node) {
        this(null, node);
    }

    public DebugMessage(@Nullable final ListType type, @NotNull final String node) {
        this.type = type;
        this.node = node;
    }

    /**
     * Get the type of node.
     *
     * @return the type of node, null if it's not an important node
     */
    @Nullable
    public ListType getType() {
        return type;
    }

    /**
     * The message for this node.
     *
     * @return the message of the node
     */
    @NotNull
    public String getNode() {
        return node;
    }
}
