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
package fr.andross.banitem.items.meta;

import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.utils.debug.Debug;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract meta type comparator.
 *
 * @author Andross
 * @version 3.1
 */
public abstract class MetaTypeComparator {
    protected final Object configurationProperties;
    private boolean valid = true;

    /**
     * Represents a meta type which can be used to be compared with an item.
     * Implementer will handle the propertiesConfiguration representing the properties.
     *
     * @param configurationProperties the property configuration used
     * @param debug                   the debug object
     */
    public MetaTypeComparator(final Object configurationProperties,
                              final Debug debug) {
        this.configurationProperties = configurationProperties;
    }

    /**
     * If the instantiation and the load of the configured properties has been successfully.
     *
     * @return true if the properties has correctly been loaded, otherwise false
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Change the state of validation of this meta type comparator.
     * If false, the meta type implementation will not be used into the custom item
     * system, to check if an item matches.
     *
     * @param valid if the meta type is correctly loaded
     */
    public void setValid(final boolean valid) {
        this.valid = valid;
    }

    /**
     * Check if the meta type configured matches on the item involved.
     *
     * @param bannedItem the current item involved
     * @return true if the meta is present on the item, otherwise false
     */
    public abstract boolean matches(@NotNull final BannedItem bannedItem);
}
