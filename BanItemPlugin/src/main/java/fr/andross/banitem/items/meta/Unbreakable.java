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
import fr.andross.banitem.utils.MinecraftVersion;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.hooks.OldItemUtils;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * A simple meta comparator to check if the item is unbreakable.
 *
 * @author Andross
 * @version 3.1
 */
public final class Unbreakable extends MetaTypeComparator {
    private final boolean unbreakable;

    public Unbreakable(final Object o, final Debug debug) {
        super(o);
        unbreakable = (o instanceof Boolean) ? (boolean) o : Boolean.parseBoolean(o.toString());
    }

    @Override
    public boolean matches(@NotNull final BannedItem bannedItem) {
        final ItemMeta itemMeta = bannedItem.getItemMeta();
        final boolean isUnbreakable = itemMeta != null && (MinecraftVersion.v11OrMore ? itemMeta.isUnbreakable() : OldItemUtils.isUnbreakable(itemMeta));
        return isUnbreakable && unbreakable;
    }
}
