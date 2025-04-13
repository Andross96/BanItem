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
import fr.andross.banitem.utils.Utils;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.enchantments.EnchantmentHelper;
import fr.andross.banitem.utils.list.Listable;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple meta comparator to compare the enchantments.
 *
 * @author Andross
 * @version 3.1.1
 */
public final class EnchantmentEquals extends MetaTypeComparator {
    private final Map<Enchantment, Integer> enchants = new HashMap<>();

    /**
     * Prepare the configured property to be compared with an item.
     *
     * @param configurationProperties the configured property value
     * @param debug                   the debug handler
     */
    public EnchantmentEquals(final Object configurationProperties, final Debug debug) {
        super(configurationProperties, debug);

        for (final String string : Listable.getSplitStringList(configurationProperties)) {
            final Enchantment enchantment;
            final int level;

            final String[] s = string.split(":");
            if (s.length != 2) {
                debug.clone().add("&cInvalid enchantment syntax '" + string + "'.").sendDebug();
                setValid(false);
                return;
            }

            enchantment = EnchantmentHelper.getEnchantment(s[0]);
            if (enchantment == null) {
                debug.clone().add("&cUnknown enchantment '" + s[0] + "'.").sendDebug();
                setValid(false);
                return;
            }

            try {
                level = Integer.parseInt(s[1]);
            } catch (final NumberFormatException e) {
                debug.clone().add("&cInvalid enchantment level '" + s[1] + "' for value '" + string + "'.").sendDebug();
                setValid(false);
                return;
            }

            enchants.put(enchantment, level);
        }
    }

    @Override
    public boolean matches(@NotNull final BannedItem bannedItem) {
        // Not an item ?
        if (bannedItem.getItemStack() == null && !bannedItem.getType().isItem()) {
            return false;
        }
        return Utils.getAllEnchants(bannedItem.toItemStack()).equals(enchants);
    }
}
