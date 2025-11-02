/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 * Based on BanItem by André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package fr.andross.banitem.actions;

import org.jetbrains.annotations.NotNull;

/**
 * List of available ban actions adapted for NeoForge.
 * Original actions from Bukkit/Spigot are adapted to Forge events.
 */
public enum BanAction {
    /**
     * When the player try to break a block.
     */
    BREAK("break"),

    /**
     * When the player left-clicks with the item.
     */
    CLICK("click"),

    /**
     * When the player consumes an item (food).
     */
    CONSUME("consume"),

    /**
     * When the player try to craft an item (crafting result).
     */
    CRAFT("craft"),

    /**
     * When the player try to drop an item.
     */
    DROP("drop"),

    /**
     * When a block breaks and drops the item.
     */
    DROPS("drops"),

    /**
     * When a player try to hold the item.
     */
    HOLD("hold"),

    /**
     * When a player right-clicks with the item.
     */
    INTERACT("interact"),

    /**
     * When a player clicks the item in their inventory.
     */
    INVENTORYCLICK("inventoryclick"),

    /**
     * When a player try to pick up an item.
     */
    PICKUP("pickup"),

    /**
     * When a player try to place a block.
     */
    PLACE("place"),

    /**
     * When an item is transferred from an inventory to another.
     */
    TRANSFER("transfer"),

    /**
     * When a player uses (right click) the item.
     */
    USE("use"),

    /**
     * When a player try to wear an item (armor).
     */
    WEAR("wear"),

    /**
     * When a player attacks an entity with the item.
     */
    ATTACK("attack"),

    /**
     * Delete banned items when opening/closing inventory.
     */
    DELETE("delete");

    private final String name;

    BanAction(final String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
