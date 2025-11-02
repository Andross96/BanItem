/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem;

import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanData;
import fr.andross.banitem.items.BannedItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * API for checking if items are banned.
 */
public class BanItemAPI {
    private final ModMain plugin;

    public BanItemAPI(@NotNull final ModMain plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if an ItemStack is banned for a player.
     *
     * @param player      the player
     * @param item        the ItemStack to check
     * @param sendMessage whether to send a ban message to the player
     * @param action      the action being performed
     * @param data        optional additional data
     * @return true if banned, false otherwise
     */
    public boolean isBanned(@NotNull final ServerPlayer player,
                          @NotNull final ItemStack item,
                          final boolean sendMessage,
                          @NotNull final BanAction action,
                          @Nullable final BanData... data) {
        return isBanned(player, new BannedItem(item), sendMessage, action, data);
    }

    /**
     * Check if a BannedItem is banned for a player.
     *
     * @param player      the player
     * @param item        the BannedItem to check
     * @param sendMessage whether to send a ban message to the player
     * @param action      the action being performed
     * @param data        optional additional data
     * @return true if banned, false otherwise
     */
    public boolean isBanned(@NotNull final ServerPlayer player,
                          @NotNull final BannedItem item,
                          final boolean sendMessage,
                          @NotNull final BanAction action,
                          @Nullable final BanData... data) {
        // Check blacklist
        if (isBlacklisted(player.level(), item, action, data)) {
            if (sendMessage) {
                sendBanMessage(player, item, action);
            }
            return true;
        }

        // Check whitelist (if item is not whitelisted, it's banned)
        if (!plugin.getBanDatabase().getWhitelist().isEmpty()) {
            if (!isWhitelisted(player.level(), item, action, data)) {
                if (sendMessage) {
                    sendBanMessage(player, item, action);
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Check if an item is banned in a world (no player context).
     *
     * @param world  the world
     * @param item   the ItemStack to check
     * @param action the action being performed
     * @param data   optional additional data
     * @return true if banned, false otherwise
     */
    public boolean isBanned(@NotNull final Level world,
                          @NotNull final ItemStack item,
                          @NotNull final BanAction action,
                          @Nullable final BanData... data) {
        return isBanned(world, new BannedItem(item), action, data);
    }

    /**
     * Check if a BannedItem is banned in a world (no player context).
     *
     * @param world  the world
     * @param item   the BannedItem to check
     * @param action the action being performed
     * @param data   optional additional data
     * @return true if banned, false otherwise
     */
    public boolean isBanned(@NotNull final Level world,
                          @NotNull final BannedItem item,
                          @NotNull final BanAction action,
                          @Nullable final BanData... data) {
        if (isBlacklisted(world, item, action, data)) {
            return true;
        }

        if (!plugin.getBanDatabase().getWhitelist().isEmpty()) {
            return !isWhitelisted(world, item, action, data);
        }

        return false;
    }

    /**
     * Check if an item is blacklisted.
     */
    public boolean isBlacklisted(@NotNull final Level world,
                                @NotNull final BannedItem item,
                                @NotNull final BanAction action,
                                @Nullable final BanData... data) {
        // TODO: Implement blacklist checking logic
        return false;
    }

    /**
     * Check if an item is whitelisted.
     */
    public boolean isWhitelisted(@NotNull final Level world,
                                @NotNull final BannedItem item,
                                @NotNull final BanAction action,
                                @Nullable final BanData... data) {
        // TODO: Implement whitelist checking logic
        return false;
    }

    /**
     * Send a ban message to a player.
     */
    private void sendBanMessage(@NotNull final ServerPlayer player,
                              @NotNull final BannedItem item,
                              @NotNull final BanAction action) {
        final String prefix = plugin.getBanConfig().getPrefix();
        final String message = prefix + "&cThis item is banned! &7(Action: &e" + action.getName() + "&7)";
        plugin.getUtils().sendMessage(player, message);
    }

    /**
     * Get the database.
     */
    @NotNull
    public BanDatabase getDatabase() {
        return plugin.getBanDatabase();
    }
}
