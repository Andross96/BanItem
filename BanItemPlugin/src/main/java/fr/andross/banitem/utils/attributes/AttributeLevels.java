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

/**
 * An attribute wrapper class that stores the levels and
 * the comparator in which the attribute must match.
 *
 * @author EpiCanard
 * @version 3.4
 */
public final class AttributeLevels {
    /**
     * An enum to define how to compare attribute levels
     */
    public enum Comparator {
        BETWEEN,
        EQUALS,
        LOWER,
        HIGHER;

        /**
         * Retrieve a Comparator from a string sign.
         *
         * @param operator Operator as string
         * @return The Comparator that match with operator
         */
        @NotNull
        public static Comparator fromString(@NotNull final String operator) {
            switch (operator) {
                case ">":
                    return HIGHER;
                case "<":
                    return LOWER;
                default:
                    return EQUALS;
            }
        }
    }

    private final Comparator comparator;
    private final Double minLevel;
    private final Double maxLevel;

    public AttributeLevels(@NotNull final Double level, @NotNull final Comparator comparator) {
        this.comparator = comparator;
        this.minLevel = level;
        this.maxLevel = null;
    }

    public AttributeLevels(@NotNull final Double minLevel, @NotNull final Double maxLevel) {
        this.comparator = Comparator.BETWEEN;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    /**
     * Define if the param level match with the attribute levels.
     *
     * @param level Level to check if it matches
     * @return if the input level match
     */
    @NotNull
    public Boolean matches(@NotNull final Double level) {
        switch (this.comparator) {
            case BETWEEN:
                return level >= minLevel && (maxLevel == null || level <= maxLevel);
            case EQUALS:
                return level.equals(minLevel);
            case LOWER:
                return level < minLevel;
            case HIGHER:
                return level > minLevel;
        }
        return false;
    }
}