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

import fr.andross.banitem.utils.Utils;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.enchantments.EnchantmentHelper;
import fr.andross.banitem.utils.list.Listable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple meta comparator to compare the enchantments
 * @version 3.1.1
 * @author Andross
 */
public final class EnchantmentEquals extends MetaTypeComparator {
    private final Map<Enchantment, Integer> enchants = new HashMap<>();

    public EnchantmentEquals(final Object o, final Debug debug) {
        super(o);

        for (final String string : Listable.getSplittedStringList(o)) {
            final Enchantment enchantment;
            final int level;

            final String[] s = string.split(":");
            if (s.length != 2) {
                debug.clone().add("&cInvalid enchantment synthax '" + string + "'.").sendDebug();
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
    public boolean matches(@NotNull final ItemStack itemStack, @Nullable final ItemMeta itemMeta) {
        return Utils.getAllEnchants(itemStack).equals(enchants);
    }
}
