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
package fr.andross.banitem.items;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * A simple enumeration for customizable item metas
 * @version 3.0
 * @author Andross
 */
public enum MetaType {
    DISPLAYNAME_EQUALS(o -> (o instanceof String)),
    DISPLAYNAME_CONTAINS(o -> (o instanceof String)),
    LORE_EQUALS(o -> (o instanceof String) || (o instanceof List) || (o instanceof String[])),
    LORE_CONTAINS(o -> (o instanceof String) || (o instanceof List) || (o instanceof String[])),
    LORE_LINE_CONTAINS(o -> (o instanceof String)),
    DURABILITY(o -> (o instanceof Integer)),
    ENCHANTMENT_EQUALS(o -> (o instanceof String) || (o instanceof List) || (o instanceof String[])),
    ENCHANTMENT_CONTAINS(o -> (o instanceof String) || (o instanceof List) || (o instanceof String[])),
    POTION(o -> (o instanceof String) || (o instanceof List) || (o instanceof String[])),
    MODELDATA_EQUALS(o -> (o instanceof Integer));

    private final Predicate<Object> p;

    MetaType(final Predicate<Object> p) {
        this.p = p;
    }

    /**
     * Attempt to validate the object class, to match the meta type
     * @param o the object to validate
     */
    public boolean validate(@NotNull final Object o) {
        return p.test(o);
    }
}
