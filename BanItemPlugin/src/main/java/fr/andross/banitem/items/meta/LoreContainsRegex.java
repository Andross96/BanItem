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
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A simple meta comparator to check if the lore matches a specific regex.
 *
 * @author Andross
 * @version 3.2
 */
public final class LoreContainsRegex extends MetaTypeComparator {
    private Pattern pattern;

    public LoreContainsRegex(final Object o, final Debug debug) {
        super(o);

        try {
            pattern = Pattern.compile(o.toString());
        } catch (final PatternSyntaxException e) {
            debug.clone().add("&cInvalid regex pattern entered.").sendDebug();
            setValid(false);
        }
    }

    @Override
    public boolean matches(@NotNull final BannedItem bannedItem) {
        final ItemMeta itemMeta = bannedItem.getItemMeta();
        final List<String> itemLore = itemMeta == null ? null : (itemMeta.hasLore() ? itemMeta.getLore() : null); // made it that way to ignore the @Nullable annotation warning
        return itemLore != null && itemLore.stream().anyMatch(l -> pattern.matcher(l).find());
    }
}
