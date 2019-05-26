package fr.banitem;

final class BannedItem {
    private final boolean blockPlace;
    private final boolean blockBreak;
    private final boolean pickUp;
    private final boolean drop;
    private final boolean interact;
    private final String message;

    BannedItem(boolean blockPlace, boolean blockBreak, boolean pickUp, boolean drop, boolean interact, String message){
        this.blockPlace = blockPlace;
        this.blockBreak = blockBreak;
        this.pickUp = pickUp;
        this.drop = drop;
        this.interact = interact;
        this.message = message;
    }

    boolean ignorePlaced() { return !blockPlace; }
    boolean ignoreBreaked() { return !blockBreak; }
    boolean ignorePickUp() { return !pickUp; }
    boolean ignoreDrop() { return !drop; }
    boolean ignoreInteract(){ return !interact; }
    String getMessage(){ return message; }
}