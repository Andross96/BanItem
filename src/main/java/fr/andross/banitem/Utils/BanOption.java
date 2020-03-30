package fr.andross.banitem.Utils;

public enum BanOption {
    // General
    PLACE("place"),
    BREAK("break"),
    PICKUP("pickup"),
    DROP("drop"),
    INTERACT("interact"),
    CLICK("click"),
    ATTACK("attack"),
    INVENTORY("inventory"),
    CONSUME("consume"),
    WEAR("wear"),
    SWAP("swap"),
    ARMORSTANDPLACE("armorstandplace"),
    ARMORSTANDTAKE("armorstandtake"),
    DISPENSE("dispense"),
    CRAFT("craft"),
    SMELT("smelt"),

    // Options
    CREATIVE("creative"),
    DELETE("delete");

    private final String name;

    BanOption(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}