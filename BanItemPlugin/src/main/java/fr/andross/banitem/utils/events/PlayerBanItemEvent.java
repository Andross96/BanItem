package fr.andross.banitem.utils.events;

import fr.andross.banitem.options.BanData;
import fr.andross.banitem.options.BanOption;
import fr.andross.banitem.options.BanOptionData;
import fr.andross.banitem.utils.item.BannedItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when an item should be banned
 * This is only for check purpose, no modifications can be made.
 * Cancelling the event will cancel the ban process.
 * @version 2.0
 * @author Andross
 */
public class PlayerBanItemEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Type type;
    private final BannedItem bannedItem;
    private final BanOption option;
    private final BanOptionData optionData;
    private final BanData[] data;
    private boolean cancelled;

    public PlayerBanItemEvent(@NotNull final Player player, @NotNull Type type, @NotNull final BannedItem bannedItem, @NotNull final BanOption option, @NotNull final BanOptionData optionData, @Nullable final BanData... data) {
        super(player);
        this.type = type;
        this.bannedItem = bannedItem;
        this.optionData = optionData;
        this.option = option;
        this.data = data;
    }

    /**
     * Type of banning
     * @return the type of ban, BLACKLIST or WHITELIST
     */
    public Type getType() {
        return type;
    }

    /**
     * The banned item
     * @return the banned item involved into this event
     */
    @NotNull
    public BannedItem getBannedItem() {
        return bannedItem;
    }

    /**
     * The ban option
     * @return the option triggered
     */
    @NotNull
    public BanOption getOption() {
        return option;
    }

    /**
     * The ban option data
     * @return the ban option data that the banned item has in database
     */
    @NotNull
    public BanOptionData getOptionData() {
        return optionData;
    }

    /**
     * The data used
     * @return all the ban datas used into this event
     */
    @Nullable
    public BanData[] getData() {
        return data;
    }

    /**
     * The ban type
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
