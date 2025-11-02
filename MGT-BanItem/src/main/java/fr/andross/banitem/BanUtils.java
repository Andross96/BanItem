/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for the MGT-BanItem mod.
 */
public class BanUtils {
    private final ModMain plugin;
    private final Set<UUID> logging = new HashSet<>();
    
    public BanUtils(@NotNull final ModMain plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Get the set of players with logging enabled.
     */
    @NotNull
    public Set<UUID> getLogging() {
        return logging;
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
