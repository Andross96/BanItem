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
import fr.andross.banitem.utils.Chat;
import fr.andross.banitem.utils.debug.Debug;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * A simple meta comparator to compare the display name.
 *
 * @author Andross
 * @version 3.1
 */
public final class DisplayNameContains extends MetaTypeComparator {
    private final String displayName;

    /**
     * Prepare the configured property to be compared with an item.
     *
     * @param configurationProperties     the configured property value
     * @param debug the debug handler
     */
    public DisplayNameContains(final Object configurationProperties, final Debug debug) {
        super(configurationProperties, debug);
        displayName = Chat.color(configurationProperties.toString());
    }

    @Override
    public boolean matches(@NotNull final BannedItem bannedItem) {
        final ItemMeta itemMeta = bannedItem.getItemMeta();
        return itemMeta != null && itemMeta.hasDisplayName() && itemMeta.getDisplayName().contains(displayName);
    }
}
