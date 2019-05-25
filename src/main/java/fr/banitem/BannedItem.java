package fr.banitem;

final class BannedItem {
    private final boolean blockPlace;
    private final boolean blockBreak;
    private final boolean pickUp;
    private final boolean drop;
    private final String notAllowedMessage;

    BannedItem(boolean blockPlace, boolean blockBreak, boolean pickUp, boolean drop, String notAllowedMessage){
        this.blockPlace = blockPlace;
        this.blockBreak = blockBreak;
        this.pickUp = pickUp;
        this.drop = drop;
        this.notAllowedMessage = notAllowedMessage;
    }

    boolean ignorePlaced() { return !blockPlace; }
    boolean ignoreBreaked() { return !blockBreak; }
    boolean ignorePickUp() { return !pickUp; }
    boolean ignoreDrop() { return !drop; }
    String getNotAllowedMessage(){ return notAllowedMessage; }
}