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

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An item wrapper for meta items
 * @version 3.3
 * @author Andross
 */
public final class MetaItem extends BannedItem implements ICustomName {
    private final String name;

    public MetaItem(@NotNull final String name, @NotNull final ItemStack itemStack) {
        super(itemStack);
        this.name = name;
    }

    /**
     * Get the custom banned item name from customitems.yml
     * @return the custom banned item name from customitems.yml
     */
    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
