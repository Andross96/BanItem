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
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.list.Listable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple meta comparator to compare ItemsAdder items
 * @version 3.2
 * @author Andross
 */
public final class ItemsAdder extends MetaTypeComparator {
    private final Set<String> items = new HashSet<>();

    public ItemsAdder(final Object o, final Debug debug) {
        super(o);

        // Not available?
        try {
            Class.forName("dev.lone.itemsadder.api.ItemsAdder");
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            debug.clone().add("&cTrying to use ItemsAdder but the plugin is not enabled.").sendDebug();
            setValid(false);
            return;
        }

        items.addAll(Listable.getSplittedStringList(o).stream().map(String::toLowerCase).collect(Collectors.toList()));
    }

    @Override
    public boolean matches(@NotNull final ItemStack itemStack, @Nullable final ItemMeta itemMeta) {
        final CustomStack customStack = CustomStack.byItemStack(itemStack);
        return customStack != null && items.contains(customStack.getId().toLowerCase(Locale.ROOT));
    }
}
