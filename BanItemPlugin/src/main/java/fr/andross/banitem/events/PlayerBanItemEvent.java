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

import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.actions.BanData;
import fr.andross.banitem.items.BannedItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when an item should be banned.
 * This is only for check purpose, no modifications can be made.
 * Cancelling the event will cancel the ban process.
 *
 * @author Andross
 * @version 3.3
 */
public final class PlayerBanItemEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Type type;
    private final BannedItem bannedItem;
    private final BanAction action;
    private final BanActionData actionData;
    private final BanData[] data;
    private boolean cancelled;

    public PlayerBanItemEvent(@NotNull final Player player,
                              @NotNull final Type type,
                              @NotNull final BannedItem bannedItem,
                              @NotNull final BanAction action,
                              @NotNull final BanActionData actionData,
                              @Nullable final BanData... data) {
        super(player);
        this.type = type;
        this.bannedItem = bannedItem;
        this.action = action;
        this.actionData = actionData;
        this.data = data;
    }

    /**
     * Type of banning.
     *
     * @return the type of ban, BLACKLIST or WHITELIST
     */
    @NotNull
    public Type getType() {
        return type;
    }

    /**
     * The banned item.
     *
     * @return the banned item involved into this event
     */
    @NotNull
    public BannedItem getBannedItem() {
        return bannedItem;
    }

    /**
     * The ban action.
     *
     * @return the action triggered
     */
    @NotNull
    public BanAction getAction() {
        return action;
    }

    /**
     * The ban action data.
     *
     * @return the ban action data that the banned item has in database
     */
    @NotNull
    public BanActionData getActionData() {
        return actionData;
    }

    /**
     * The data used.
     *
     * @return all the ban datas used into this event
     */
    @Nullable
    public BanData[] getData() {
        return data;
    }

    /**
     * The ban type.
     */
    public enum Type {
        BLACKLIST,
        WHITELIST
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
