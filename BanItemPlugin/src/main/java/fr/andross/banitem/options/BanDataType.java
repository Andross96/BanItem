package fr.andross.banitem.options;

import fr.andross.banitem.utils.item.BannedItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * A simple enum indicating what kind of data is used
 * @version 2.1
 * @author Andross
 */
public enum BanDataType {
    /**
     * Type: Long (millis)
     * Used to check if the banned item has a cooldown
     */
    COOLDOWN("cooldown"),

    /**
     * Type: {@link BannedItemMeta}
     * Used to check the item metadata
     */
    METADATA("metadata"),

    /**
     * Type: Set of EntityType {@link org.bukkit.entity.EntityType}
     * Used to check if the ban will applies on this entity
     */
    ENTITY("entity"),

    /**
     * Type: Set of {@link org.bukkit.GameMode}
     * Used to check if the ban applies on current player gamemode
     */
    GAMEMODE("gamemode"),

    /**
     * Type: Set of InventoryType {@link org.bukkit.event.inventory.InventoryType}
     * Used to check if the ban will applies if the source inventory is included into this set
     */
    INVENTORY_FROM("inventory-from"),

    /**
     * Type: Set of InventoryType {@link org.bukkit.event.inventory.InventoryType}
     * Used to check if the ban will applies if the destination inventory is included into this set
     */
    INVENTORY_TO("inventory-to"),

    /**
     * Type: boolean
     * Used to check if a log message will be sent to players with log activated
     */
    LOG("log"),

    /**
     * Type: Set of {@link org.bukkit.Material}
     * Used to check if the ban will applies if a material is in the set
     */
    MATERIAL("material"),

    /**
     * Type: List of <i>(already colored)</i> String
     * Used to get the ban message(s)
     */
    MESSAGE("message"),

    /**
     * Type: Set of {@link com.sk89q.worldguard.protection.regions.ProtectedRegion}
     * Used to check if the ban applies into the region
     */
    REGION("region");

    private final String name;

    BanDataType(@NotNull final String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
