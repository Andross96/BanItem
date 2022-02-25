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

import org.jetbrains.annotations.NotNull;

/**
 * A simple enumeration for customizable item metas
 * @version 3.2
 * @author Andross
 */
public enum MetaType {
    ADVANCEDENCHANTMENTS(AdvancedEnchantments.class),
    DISPLAYNAME_CONTAINS(DisplayNameContains.class),
    DISPLAYNAME_EQUALS(DisplayNameEquals.class),
    DURABILITY(Durability.class),
    ENCHANTMENT_CONTAINS(EnchantmentContains.class),
    ENCHANTMENT_EQUALS(EnchantmentEquals.class),
    ITEMSADDER(ItemsAdder.class),
    LORE_CONTAINS(LoreContains.class),
    LORE_CONTAINS_REGEX(LoreContainsRegex.class),
    LORE_EQUALS(LoreEquals.class),
    LORE_LINE_CONTAINS(LoreLineContains.class),
    MODELDATA_EQUALS(ModeldataEquals.class),
    NBTAPI(NBTAPI.class),
    POTION(Potion.class),
    UNBREAKABLE(Unbreakable.class),
    ATTRIBUTE(AttributeContains.class);

    private final Class<? extends MetaTypeComparator> clazz;

    MetaType(final Class<? extends MetaTypeComparator> clazz) {
        this.clazz = clazz;
    }

    /**
     * Get the handler class of this meta
     * @return the handler class of this meta
     */
    @NotNull
    public Class<? extends MetaTypeComparator> getClazz() {
        return clazz;
    }
}
