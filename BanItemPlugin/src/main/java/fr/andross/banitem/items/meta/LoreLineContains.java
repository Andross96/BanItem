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

import fr.andross.banitem.utils.Chat;
import fr.andross.banitem.utils.debug.Debug;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A simple meta comparator to compare the lore
 * @version 3.1
 * @author Andross
 */
public final class LoreLineContains extends MetaTypeComparator {
    private final String lore;

    public LoreLineContains(final Object o, final Debug debug) {
        super(o);
        lore = Chat.color(o.toString());
    }

    @Override
    public boolean matches(@NotNull final ItemStack itemStack, @Nullable final ItemMeta itemMeta) {
        final List<String> itemLore = itemMeta == null ? null : (itemMeta.hasLore() ? itemMeta.getLore() : null); // made it that way to ignore the @Nullable annotation warning
        return itemLore != null && itemLore.stream().anyMatch(l -> l.contains(lore));
    }
}