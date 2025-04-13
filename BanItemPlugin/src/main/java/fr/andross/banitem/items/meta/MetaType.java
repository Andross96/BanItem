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
 * A simple enumeration for customizable item metas.
 *
 * @author Andross
 * @version 3.4.1
 */
public enum MetaType {
    /**
     * Match specific AdvancedEnchantments plugin enchantments.
     */
    ADVANCEDENCHANTMENTS(AdvancedEnchantments.class),

    /**
     * Match the item attribute.
     */
    ATTRIBUTE(AttributeContains.class),

    /**
     * If the display name of the item contains a specific value.
     */
    DISPLAYNAME_CONTAINS(DisplayNameContains.class),

    /**
     * If the display name of the item is exactly equals to a specific value.
     */
    DISPLAYNAME_EQUALS(DisplayNameEquals.class),

    /**
     * If the display name of the item match a specific regex.
     */
    DISPLAYNAME_EQUALS_REGEX(DisplayNameEqualsRegex.class),

    /**
     * If the durability of the item match a specific value.
     */
    DURABILITY(Durability.class),

    /**
     * If the item contains specific enchantment(s).
     */
    ENCHANTMENT_CONTAINS(EnchantmentContains.class),

    /**
     * If the item enchantments match exactly the specified enchantment(s).
     */
    ENCHANTMENT_EQUALS(EnchantmentEquals.class),

    /**
     * If the item match an ItemsAdder item.
     */
    ITEMSADDER(ItemsAdder.class),

    /**
     * If the item lore contains a lore.
     */
    LORE_CONTAINS(LoreContains.class),

    /**
     * If the item lore match a regex.
     */
    LORE_CONTAINS_REGEX(LoreContainsRegex.class),

    /**
     * If the item lore is exactly the same as a specified one.
     */
    LORE_EQUALS(LoreEquals.class),

    /**
     * If a line from an item lore contains a specific value.
     */
    LORE_LINE_CONTAINS(LoreLineContains.class),

    /**
     * If the item model data is equals to a specified value.
     */
    MODELDATA_EQUALS(ModeldataEquals.class),

    /**
     * If the NBT tags (using NBTAPI) matches on the item.
     */
    NBTAPI(NBTAPI.class),

    /**
     * If the item is a specific potion.
     */
    POTION(Potion.class),

    /**
     * If the item is unbreakable.
     */
    UNBREAKABLE(Unbreakable.class);

    private final Class<? extends MetaTypeComparator> clazz;

    /**
     * List all meta type.
     *
     * @param clazz the class used to instantiate the meta type comparator.
     */
    MetaType(final Class<? extends MetaTypeComparator> clazz) {
        this.clazz = clazz;
    }

    /**
     * Get the handler class of this meta.
     *
     * @return the handler class of this meta
     */
    @NotNull
    public Class<? extends MetaTypeComparator> getClazz() {
        return clazz;
    }
}
