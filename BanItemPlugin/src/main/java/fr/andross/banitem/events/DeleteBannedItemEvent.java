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
package fr.andross.banitem.events;

import fr.andross.banitem.items.BannedItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a banned item has been detected
 * and should be deleted from the current inventory view of the player.
 * Cancelling the event will cancel the delete process.
 *
 * @author Andross
 * @version 3.1
 */
public final class DeleteBannedItemEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final BannedItem bannedItem;
    private boolean cancelled = false;

    /**
     * Event triggered when an item will be deleted by BanItem because it is banned.
     *
     * @param who the player involved
     * @param bannedItem the item involved
     */
    public DeleteBannedItemEvent(@NotNull final Player who,
                                 @NotNull final BannedItem bannedItem) {
        super(who);
        this.bannedItem = bannedItem;
    }

    /**
     * The banned item involved.
     *
     * @return the banned item
     */
    @NotNull
    public BannedItem getBannedItem() {
        return bannedItem;
    }

    /**
     * Check if the event is cancelled.
     *
     * @return true if the event is cancelled, otherwise false
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancel the event.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(final boolean cancel) {
        cancelled = cancel;
    }

    /**
     * Internal Bukkit event handler list.
     *
     * @return internal bukkit event handler list
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Bukkit event handler list.
     *
     * @return the bukkit event handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
