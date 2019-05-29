package fr.banitem;

final class BannedItem {
    private final boolean blockPlace;
    private final boolean blockBreak;
    private final boolean pickUp;
    private final boolean drop;
    private final boolean interact;
    private final String message;

    BannedItem(boolean blockPlace, boolean blockBreak, boolean pickUp, boolean drop, boolean interact, String message){
        this.blockPlace = !blockPlace;
        this.blockBreak = !blockBreak;
        this.pickUp = !pickUp;
        this.drop = !drop;
        this.interact = !interact;
        this.message = message;
    }

    boolean canPlace() { return blockPlace; }
    boolean canBreak() { return blockBreak; }
    boolean canPickUp() { return pickUp; }
    boolean canDrop() { return drop; }
    boolean canInteract(){ return interact; }
    String getMessage(){ return message; }
}