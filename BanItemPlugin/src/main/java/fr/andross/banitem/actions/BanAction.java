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
 * List of available ban actions.
 *
 * @author Andross
 * @version 3.4
 */
public enum BanAction {
    /**
     * When the player try to place the item on an armorstand.
     * <p>Special data: none
     */
    ARMORSTANDPLACE("armorstandplace"),

    /**
     * When the player try to take an item from an armorstand.
     * <p>Special data: none
     */
    ARMORSTANDTAKE("armorstandtake"),

    /**
     * When the player attack <i>(left click on)</i> an entity with the item.
     * <p>Special data: <b>entity</b> - the {@link org.bukkit.entity.EntityType} of the attacked entity
     */
    ATTACK("attack"),

    /**
     * When the player edits or signs a book and quill item.
     * <p>Special data: none
     */
    BOOKEDIT("bookedit"),

    /**
     * When the player try to break <i>(left click on)</i> a block.
     * <p>Special data: <b>material</b> - the {@link org.bukkit.Material} item in the hand of the player
     */
    BREAK("break"),

    /**
     * When an item is brewed into a brewer.
     * <p>Special data: none
     */
    BREW("brew"),

    /**
     * When the player use left click <i>(either on block or on air)</i> with the item.
     * <p>Special data: <b>material</b> - the {@link org.bukkit.Material} item if a block is clicked
     */
    CLICK("click"),

    /**
     * When the player consume an item <i>(food)</i>.
     * <p>Special data: none
     */
    CONSUME("consume"),

    /**
     * When the player try to craft an item <i>(crafting result)</i>.
     * <p>Special data: none
     */
    CRAFT("craft"),

    /**
     * When the crafter item try to craft an item.
     * <p>Special data: none
     */
    CRAFTER("crafter"),

    /**
     * When the player open or close an inventory, it will delete the banned items.
     * <p>Special data: none
     */
    DELETE("delete"),

    /**
     * When the item is dispensed from a block.
     * <p>Special data: none
     */
    DISPENSE("dispense"),

    /**
     * When the player try to drop an item.
     * <p>Special data: none
     */
    DROP("drop"),

    /**
     * This will disable a block break drops if any drops contains the banned item.
     * <p>Special data: <b>material</b> - the {@link org.bukkit.Material} of the item in player hand
     */
    DROPS("drops"),

    /**
     * When a player enchants the item.
     * <p>Special data: <b>set of EnchantmentWrapper</b> - the {@link EnchantmentWrapper} applied to the item
     */
    ENCHANT("enchant"),

    /**
     * When an entity dies and drop the item.
     * <p>Special data: <b>entity</b> - the {@link org.bukkit.entity.EntityType} of the entity
     */
    ENTITYDROP("entitydrop"),

    /**
     * When a player right-click on an entity.
     * <p>Special data: <b>entity</b> - the {@link org.bukkit.entity.EntityType} of the right clicked entity
     */
    ENTITYINTERACT("entityinteract"),

    /**
     * When a player try to fill an item (like a bucket).
     * <p>Special data: <b>material</b> - the {@link org.bukkit.Material} collected
     */
    FILL("fill"),

    /**
     * When a player try to use <i>(activate)</i> an elytra.
     * <p>Special data: none
     */
    GLIDE("glide"),

    /**
     * When a player try to place a hanging item <i>(itemframe, painting...)</i>.
     * <p>Special data: <b>entity</b> - the {@link org.bukkit.entity.EntityType} created by this hanging place
     */
    HANGINGPLACE("hangingplace"),

    /**
     * When a player try to hold the item.
     * <p>Special data: none
     */
    HOLD("hold"),

    /**
     * When a player use right-click on this item.
     * <p>Special data: <b>material</b> - the {@link org.bukkit.Material} in the player hand
     */
    INTERACT("interact"),

    /**
     * When a player try to click the item in its inventory.
     * <p>Special data: <b>inventory-from</b> the {@link org.bukkit.event.inventory.InventoryType} clicked
     */
    INVENTORYCLICK("inventoryclick"),

    /**
     * When a player has an item repaired via the mending enchantment.
     * <p>Special data: none
     */
    MENDING("mending"),

    /**
     * When a player try to pick up an item.
     * <p>Special data: none
     */
    PICKUP("pickup"),

    /**
     * When a player try to place a block.
     * <p>Special data: none
     */
    PLACE("place"),

    /**
     * When a player try to rename an item, either with a command or with an anvil.
     * <p>Special data: none
     */
    RENAME("rename"),

    /**
     * When an item is smelted into a furnace.
     * <p>Special data: none
     */
    SMELT("smelt"),

    /**
     * When the recipe of an item is completed inside a smithing table.
     * <p>Special data: none
     */
    SMITH("smith"),

    /**
     * When a player try to swap an item in hands.
     * Will check both offhand item and main hand item.
     * <p>Special data: none
     */
    SWAP("swap"),

    /**
     * When a player attacks with an item which has
     * sweeping edge enchantment.
     * <p>Special data: <b>entity</b> - the {@link org.bukkit.entity.EntityType} of the attacked entity
     */
    SWEEPINGEDGE("sweepingedge"),

    /**
     * When an item is transfered from an inventory to another.
     * <p>Special data:
     * <ul>
     * <li><b>inventory-from</b> - the {@link org.bukkit.event.inventory.InventoryType} where the item comes from</li>
     * <li><b>inventory-to</b> - the {@link org.bukkit.event.inventory.InventoryType} where the item goes to</li>
     * </ul>
     */
    TRANSFER("transfer"),

    /**
     * When a player unfill a bucket.
     * Special data: <b>material</b> - the {@link org.bukkit.Material} fluid type
     */
    UNFILL("unfill"),

    /**
     * When a player use (right click) the item.
     * <p>Special data: <b>material</b> - the {@link org.bukkit.Material} clicked, if one clicked
     */
    USE("use"),

    /**
     * When a player try to wear an item.
     * <p>Special data: none
     */
    WEAR("wear");

    /**
     * Name of the action, to lower case.
     * Used to perform permission check, for example.
     */
    private final String name;

    BanAction(final String name) {
        this.name = name;
    }

    /**
     * Name, to lower case, of this action.
     *
     * @return the name to lower case of this action
     */
    @NotNull
    public String getName() {
        return name;
    }
}