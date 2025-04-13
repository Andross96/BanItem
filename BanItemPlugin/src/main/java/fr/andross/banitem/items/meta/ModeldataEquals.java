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
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * A simple meta comparator to compare the model data.
 *
 * @author Andross
 * @version 3.3
 */
public final class ModeldataEquals extends MetaTypeComparator {
    private int modelData;

    /**
     * Prepare the configured property to be compared with an item.
     *
     * @param o     the configured property value
     * @param debug the debug handler
     */
    public ModeldataEquals(final Object o, final Debug debug) {
        super(o);

        if (!MinecraftVersion.v14OrMore) {
            debug.clone().add("&cCan not use model data on MC<1.14 '" + o + ".").sendDebug();
            setValid(false);
            return;
        }

        try {
            modelData = Integer.parseInt(o.toString());
        } catch (final NumberFormatException e) {
            debug.clone().add("&cInvalid integer model data '" + o + ".").sendDebug();
            setValid(false);
        }
    }

    @Override
    public boolean matches(@NotNull final BannedItem bannedItem) {
        final ItemMeta itemMeta = bannedItem.getItemMeta();
        return itemMeta != null && itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() == modelData;
    }
}
