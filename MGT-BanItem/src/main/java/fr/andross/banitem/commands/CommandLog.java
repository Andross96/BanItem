/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.andross.banitem.ModMain;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * /banitem log command implementation
 * Usage: /banitem log
 */
public class CommandLog {
    
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        final ModMain plugin = ModMain.getInstance();
        
        if (plugin == null) {
            source.sendFailure(Component.literal("§cMod not initialized!"));
            return 0;
        }
        
        // Must be a player
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("§cThis command can only be used by players."));
            return 0;
        }
        
        final UUID playerUUID = player.getUUID();
        
        source.sendSuccess(() -> Component.literal(
            "§7§m     §r §l[§6§lLog§r§l] §7§m     §r"
        ), false);
        
        // Toggle logging
        if (plugin.getUtils().getLogging().contains(playerUUID)) {
            plugin.getUtils().getLogging().remove(playerUUID);
            source.sendSuccess(() -> Component.literal("§7[LOG]: §c§lOFF"), false);
            ModMain.LOGGER.info("Debug logging disabled for {}", player.getName().getString());
        } else {
            plugin.getUtils().getLogging().add(playerUUID);
            source.sendSuccess(() -> Component.literal("§7[LOG]: §a§lON"), false);
            ModMain.LOGGER.info("Debug logging enabled for {}", player.getName().getString());
        }
        
        return 1;
    }
}
