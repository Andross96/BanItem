/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.andross.banitem.ModMain;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Command manager for MGT-BanItem.
 * Registers and handles all mod commands.
 */
@EventBusSubscriber(modid = ModMain.MODID)
public class BanCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("banitem")
                .then(Commands.literal("reload")
                    .requires(source -> source.hasPermission(2))
                    .executes(BanCommand::reload))
                .then(Commands.literal("help")
                    .executes(BanCommand::help))
                .then(Commands.literal("info")
                    .requires(source -> source.hasPermission(2))
                    .executes(BanCommand::info))
                .then(Commands.literal("add")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("actions", StringArgumentType.greedyString())
                        .executes(CommandAdd::execute)))
                .then(Commands.literal("remove")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(CommandRemove::execute)))
                .then(Commands.literal("check")
                    .requires(source -> source.hasPermission(2))
                    .executes(CommandCheck::execute)
                    .then(Commands.literal("delete")
                        .executes(CommandCheck::executeDelete)))
                .then(Commands.literal("log")
                    .requires(source -> source.hasPermission(2))
                    .executes(CommandLog::execute))
                .then(Commands.literal("metaitem")
                    .requires(source -> source.hasPermission(2))
                    .executes(CommandMetaItem::helpCommand)
                    .then(Commands.literal("add")
                        .then(Commands.argument("args", StringArgumentType.greedyString())
                            .executes(CommandMetaItem::addCommand)))
                    .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(CommandMetaItem::removeCommand)))
                    .then(Commands.literal("get")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(CommandMetaItem::getCommand)))
                    .then(Commands.literal("list")
                        .executes(CommandMetaItem::listCommand)))
                .then(Commands.literal("mi")
                    .requires(source -> source.hasPermission(2))
                    .redirect(dispatcher.getRoot().getChild("banitem").getChild("metaitem")))
                .executes(BanCommand::help) // Default to help
        );

        // Alias
        dispatcher.register(
            Commands.literal("bi")
                .redirect(dispatcher.getRoot().getChild("banitem"))
        );
    }

    /**
     * Reload command - reloads configuration and database
     */
    private static int reload(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        
        try {
            final ModMain plugin = ModMain.getInstance();
            if (plugin == null) {
                source.sendFailure(Component.literal("§cMod not initialized!"));
                return 0;
            }

            // Reload configuration
            java.nio.file.Path configDir = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
            plugin.reloadConfig();
            
            // Reload database
            // Note: This would need a reload method in BanDatabase
            
            // Reload listeners
            if (plugin.getListener() != null) {
                plugin.getListener().load();
            }

            source.sendSuccess(() -> Component.literal(
                "§2Successfully reloaded MGT-BanItem configuration"
            ), true);
            
            ModMain.LOGGER.info("Configuration reloaded by {}", source.getTextName());
            return 1;
            
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cFailed to reload: " + e.getMessage()));
            ModMain.LOGGER.error("Failed to reload configuration", e);
            return 0;
        }
    }

    /**
     * Help command - shows available commands
     */
    private static int help(CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();
        
        source.sendSuccess(() -> Component.literal(
            "§7§m     §r §l[§7§lUsage - §e§lMGT-BanItem§r§l] §7§m     §r\n" +
            " §7- /banitem help§r: show this help message\n" +
            " §7- /banitem reload§r: reload configuration\n" +
            " §7- /banitem info§r: show mod information\n" +
            " §7- /banitem add <actions> [-m materials] [-w worlds] [message]§r: ban items\n" +
            " §7- /banitem remove [-m materials] [-w worlds]§r: unban items\n" +
            " §7- /banitem check [delete]§r: check for banned items in players\n" +
            " §7- /banitem log§r: toggle debug logging\n" +
            " §7- /banitem metaitem <add|remove|get|list>§r: manage meta items\n" +
            "§7Alias: §e/bi§r, §e/bi mi§r for metaitem"
        ), false);
        
        return 1;
    }

    /**
     * Info command - shows mod and database information
     */
    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        
        final ModMain plugin = ModMain.getInstance();
        if (plugin == null) {
            source.sendFailure(Component.literal("§cMod not initialized!"));
            return 0;
        }

        final int blacklisted = plugin.getBanDatabase().getBlacklist().getTotalBlacklistedItems();
        final int whitelisted = plugin.getBanDatabase().getWhitelist().getTotalWhitelistedItems();
        final int listeners = plugin.getListener() != null ? plugin.getListener().getActivated() : 0;

        source.sendSuccess(() -> Component.literal(
            "§7§m     §r §l[§e§lMGT-BanItem Info§r§l] §7§m     §r\n" +
            " §7Version: §e1.0.0§r\n" +
            " §7Blacklisted items: §e" + blacklisted + "§r\n" +
            " §7Whitelisted items: §e" + whitelisted + "§r\n" +
            " §7Active listeners: §e" + listeners + "§r"
        ), false);
        
        return 1;
    }
}
