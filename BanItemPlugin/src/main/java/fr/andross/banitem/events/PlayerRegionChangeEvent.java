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

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called if the wear region check action is enabled.
 * This is only for check purpose, no modifications can be made.
 * Cancelling the event will not cancel the player movement.
 *
 * @author Andross
 * @version 3.1
 */
public final class PlayerRegionChangeEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    /**
     * Create the event with the involved player.
     *
     * @param player the involved player
     */
    public PlayerRegionChangeEvent(@NotNull final Player player) {
        super(player);
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
