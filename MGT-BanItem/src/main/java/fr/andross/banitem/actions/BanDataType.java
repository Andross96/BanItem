/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem.actions;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the type of additional data that can be associated with a ban action.
 */
public enum BanDataType {
    /**
     * Material/Block type data
     */
    MATERIAL("material"),

    /**
     * Entity type data
     */
    ENTITY("entity"),

    /**
     * Inventory type data (source)
     */
    INVENTORY_FROM("inventory-from"),

    /**
     * Inventory type data (destination)
     */
    INVENTORY_TO("inventory-to"),

    /**
     * Enchantment data
     */
    ENCHANTMENT("enchantment");

    private final String name;

    BanDataType(final String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
