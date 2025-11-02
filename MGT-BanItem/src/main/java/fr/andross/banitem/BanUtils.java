/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for the MGT-BanItem mod.
 */
public class BanUtils {
    public BanUtils(@NotNull final ModMain plugin) {
        // Constructor for future use
    }

    /**
     * Send a formatted message to a player.
     */
    public void sendMessage(@NotNull final ServerPlayer player, @NotNull final String message) {
        final String colored = colorize(message);
        player.sendSystemMessage(Component.literal(colored));
    }

    /**
     * Colorize a string using &amp; codes.
     * Converts Minecraft color codes from &amp; format to § format.
     */
    @NotNull
    public String colorize(@NotNull final String text) {
        return text.replace('&', '§');
    }

    /**
     * Check if an ItemStack is null or empty (air).
     */
    public static boolean isNullOrAir(final ItemStack item) {
        return item == null || item.isEmpty();
    }

    /**
     * Get the display name of an ItemStack.
     */
    @NotNull
    public static String getItemDisplayName(@NotNull final ItemStack item) {
        if (item.hasCustomHoverName()) {
            return item.getHoverName().getString();
        }
        return item.getDisplayName().getString();
    }
}
