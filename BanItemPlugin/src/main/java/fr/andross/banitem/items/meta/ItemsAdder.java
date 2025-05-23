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

import dev.lone.itemsadder.api.CustomStack;
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.list.Listable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple meta comparator to compare ItemsAdder items.
 *
 * @author Andross
 * @version 3.2
 */
public final class ItemsAdder extends MetaTypeComparator {
    private final Set<String> items = new HashSet<>();

    /**
     * Prepare the configured property to be compared with an item.
     *
     * @param configurationProperties the configured property value
     * @param debug                   the debug handler
     */
    public ItemsAdder(final Object configurationProperties, final Debug debug) {
        super(configurationProperties, debug);

        // Not available?
        try {
            Class.forName("dev.lone.itemsadder.api.ItemsAdder");
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            debug.clone().add("&cTrying to use ItemsAdder but the plugin is not enabled.").sendDebug();
            setValid(false);
            return;
        }

        items.addAll(Listable.getSplitStringList(configurationProperties).stream().map(String::toLowerCase).collect(Collectors.toList()));
    }

    @Override
    public boolean matches(@NotNull final BannedItem bannedItem) {
        // Not an item ?
        if (bannedItem.getItemStack() == null && !bannedItem.getType().isItem()) {
            return false;
        }

        final CustomStack customStack = CustomStack.byItemStack(bannedItem.toItemStack());
        return customStack != null && items.contains(customStack.getId().toLowerCase(Locale.ROOT));
    }
}
