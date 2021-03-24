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
package fr.andross.banitem.actions;

import fr.andross.banitem.utils.enchantments.EnchantmentWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * List of available ban actions
 * @version 3.1
 * @author Andross
 */
public enum BanAction {
    /**
     * When the player try to place the item on an armorstand
     * Special data: none
     */
    ARMORSTANDPLACE("armorstandplace"),

    /**
     * When the player try to take an item from an armorstand
     * Special data: none
     */
    ARMORSTANDTAKE("armorstandtake"),

    /**
     * When the player attack <i>(left click on)</i> an entity with the item
     * Special data: <b>entity</b> - the {@link org.bukkit.entity.EntityType} of the attacked entity
     */
    ATTACK("attack"),

    /**
     * When the player edits or signs a book and quill item
     * Special data: none
     */
    BOOKEDIT("bookedit"),

    /**
     * When the player try to break <i>(left click on)</i> a block
     * Special data: <b>material</b> - the {@link org.bukkit.Material} item in the hand of the player
     */
    BREAK("break"),

    /**
     * When an item is brewed into a brewer
     * Special data: none
     */
    BREW("brew"),

    /**
     * When the player use left click <i>(either on block or on air)</i> with the item
     * Special data: <b>material</b> - the {@link org.bukkit.Material} item if a block is clicked
     */
    CLICK("click"),

    /**
     * When the player consume an item <i>(food)</i>
     * Special data: none
     */
    CONSUME("consume"),

    /**
     * When the player try to craft an item <i>(crafting result)</i>
     * Special data: none
     */
    CRAFT("craft"),

    /**
     * When the player open or close an inventory, it will delete the banned items
     * Special data: none
     */
    DELETE("delete"),

    /**
     * When the item is dispensed from a block
     * Special data: none
     */
    DISPENSE("dispense"),

    /**
     * When the player try to drop an item
     * Special data: none
     */
    DROP("drop"),

    /**
     * This will disable a block break drops if any drops contains the banned item
     * Special data: <b>material</b> - the {@link org.bukkit.Material} of the item in player hand
     */
    DROPS("drops"),

    /**
     * When a player enchants the item
     * Special data: <b>set of EnchantmentWrapper</b> - the {@link EnchantmentWrapper} applied to the item
     */
    ENCHANT("enchant"),

    /**
     * When an entity dies and drop the item
     * Special data: <b>entity</b> - the {@link org.bukkit.entity.EntityType} of the entity
     */
    ENTITYDROP("entitydrop"),

    /**
     * When a player right click on an entity
     * Special data: <b>entity</b> - the {@link org.bukkit.entity.EntityType} of the right clicked entity
     */
    ENTITYINTERACT("entityinteract"),

    /**
     * When a player try to fill an item (like a bucket)
     * Special data: <b>material</b> - the {@link org.bukkit.Material} collected
     */
    FILL("fill"),

    /**
     * When a player try to use <i>(activate)</i> an elytra
     * Special data: none
     */
    GLIDE("glide"),

    /**
     * When a player try to place a hanging item <i>(itemframe, painting...)</i>
     * Special data: <b>entity</b> - the {@link org.bukkit.entity.EntityType} created by this hanging place
     */
    HANGINGPLACE("hangingplace"),

    /**
     * When a player try to hold the item
     * Special data: none
     */
    HOLD("hold"),

    /**
     * When a player use right click on this item
     * Special data: <b>material</b> - the {@link org.bukkit.Material} in the player hand
     */
    INTERACT("interact"),

    /**
     * When a player try to click the item in its inventory
     * Special data: <b>inventory-from</b> the {@link org.bukkit.event.inventory.InventoryType} clicked
     */
    INVENTORYCLICK("inventoryclick"),

    /**
     * When a player try to pickup an item
     * Special data: none
     */
    PICKUP("pickup"),

    /**
     * When a player try to use or to place <i>(right click)</i> an item
     * Special data: <b>material</b> - the {@link org.bukkit.Material} clicked, if one clicked
     */
    PLACE("place"),

    /**
     * When a player try to rename an item, either with a command or with an anvil
     * Special data: none
     */
    RENAME("rename"),

    /**
     * When an item is smelted into a furnace
     * Special data: none
     */
    SMELT("smelt"),

    /**
     * When a player try to swap an item in hands.
     * Will check both offhand item and main hand item
     * Special data: none
     */
    SWAP("swap"),

    /**
     * When an item is transfered from an inventory to another
     * Special data:
     *   - <b>inventory-from</b> - the {@link org.bukkit.event.inventory.InventoryType} where the item comes from
     *   - <b>inventory-to</b> - the {@link org.bukkit.event.inventory.InventoryType} where the item goes to
     */
    TRANSFER("transfer"),

    /**
     * When a player unfill a bucket
     * Special data: <b>material</b> - the {@link org.bukkit.Material} fluid type
     */
    UNFILL("unfill"),

    /**
     * When a player try to wear an item
     * Special data: none
     */
    WEAR("wear");

    /**
     * Name of the action, to lower case
     * Used to perform permission check, for example
     */
    private final String name;

    BanAction(final String name) {
        this.name = name;
    }

    /**
     * @return the name to lower case of this action
     */
    @NotNull
    public String getName() {
        return name;
    }
}