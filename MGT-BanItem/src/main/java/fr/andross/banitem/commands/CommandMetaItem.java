/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.andross.banitem.BanUtils;
import fr.andross.banitem.ModMain;
import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.actions.BanDataType;
import fr.andross.banitem.items.BannedItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * /banitem metaitem command implementation
 * Usage: /banitem metaitem <add|remove|get|list>
 */
public class CommandMetaItem {
    
    public static int helpCommand(CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();
        
        source.sendSuccess(() -> Component.literal(
            "§7§m     §r §l[§6§lMetaItems - Help§r§l] §7§m     §r\n" +
            "§7Usages:\n" +
            "§b/bi mi add §3<name> [actions] [message]\n" +
            "§7 >> Will save and ban the item (including meta)\n" +
            "§7 >> currently in your hand in your current world.\n" +
            "§b/bi mi get §3<name>\n" +
            "§7 >> Will give you the item (with meta) in your inventory.\n" +
            "§b/bi mi list\n" +
            "§7 >> Displays a list of meta item names saved.\n" +
            "§b/bi mi remove §3<name>\n" +
            "§7 >> Will remove & unban the metaitem.\n" +
            "§cMetaItems match exactly the current item. If the item changes,\n" +
            "§cit will not be recognized anymore."
        ), false);
        
        return 1;
    }
    
    public static int addCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
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
        
        // Get item in hand
        final ItemStack heldItem = player.getMainHandItem();
        if (BanUtils.isNullOrAir(heldItem)) {
            source.sendFailure(Component.literal("§cYou must hold an item to save as meta item!"));
            return 0;
        }
        
        // Parse arguments
        final String fullArgs = context.getArgument("args", String.class);
        final String[] args = fullArgs.split("\\s+");
        
        if (args.length < 1) {
            source.sendSuccess(() -> Component.literal(
                "§7§m     §r §l[§6§lMetaItems - Add§r§l] §7§m     §r\n" +
                "§b/bi mi add §3<name> [actions] [message]\n" +
                "§7 >> Will save and ban the item (with meta) currently in your hand.\n" +
                "§7 >> Actions can be multiple separated with commas."
            ), false);
            return 0;
        }
        
        final String name = args[0];
        
        // Only save meta item without banning?
        if (args.length == 1) {
            try {
                plugin.getApi().addMetaItem(name, heldItem);
                source.sendSuccess(() -> Component.literal(
                    "§7§m     §r §l[§6§lMetaItems - Add§r§l] §7§m     §r"
                ), false);
                source.sendSuccess(() -> Component.literal(
                    "§aMeta item successfully saved with name §2" + name + "§a."
                ), true);
                
                ModMain.LOGGER.info("Meta item '{}' saved by {}", name, player.getName().getString());
                return 1;
            } catch (Exception e) {
                source.sendFailure(Component.literal("§cError during saving. Check the console for more info."));
                ModMain.LOGGER.error("Error saving meta item", e);
                return 0;
            }
        }
        
        // Parse actions
        final List<BanAction> actions = CommandHelper.parseActions(args[1]);
        if (actions.isEmpty()) {
            source.sendFailure(Component.literal("§cInvalid actions: §e" + args[1]));
            source.sendSuccess(() -> Component.literal("§7Valid actions: §o" + CommandHelper.getActionsList()), false);
            return 0;
        }
        
        // Parse message
        final BanActionData actionData = new BanActionData();
        if (args.length > 2) {
            final StringBuilder messageBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (i > 2) messageBuilder.append(" ");
                messageBuilder.append(args[i]);
            }
            final String message = CommandHelper.colorize(messageBuilder.toString());
            actionData.getMap().put(BanDataType.MESSAGE, Collections.singletonList(message));
        }
        
        // Create actions map
        final Map<BanAction, BanActionData> actionsData = new EnumMap<>(BanAction.class);
        for (final BanAction action : actions) {
            actionsData.put(action, actionData);
        }
        
        // Save meta item
        try {
            plugin.getBanDatabase().addMetaItem(name, new BannedItem(heldItem));
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cUnable to save meta item."));
            source.sendFailure(Component.literal("§cCheck the console for more information."));
            ModMain.LOGGER.error("Error saving meta item", e);
            return 0;
        }
        
        // Add to blacklist
        plugin.getApi().addToBlacklist(
            Collections.singletonList(new BannedItem(heldItem)),
            actionsData,
            player.serverLevel()
        );
        
        if (plugin.getListener() != null) {
            plugin.getListener().load();
        }
        
        source.sendSuccess(() -> Component.literal(
            "§aThis item §e" + name + "§a is now successfully banned for world §2" + 
            player.serverLevel().dimension().location() + "§a."
        ), true);
        source.sendSuccess(() -> Component.literal(
            "§7§oNote: You may have bypass permissions, so the ban may not apply to you."
        ), false);
        
        ModMain.LOGGER.info("Meta item '{}' saved and banned by {}", name, player.getName().getString());
        return 1;
    }
    
    public static int getCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
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
        
        final String name = context.getArgument("name", String.class);
        final BannedItem bannedItem = plugin.getBanDatabase().getMetaItems().get(name);
        
        if (bannedItem == null) {
            source.sendSuccess(() -> Component.literal(
                "§7§m     §r §l[§6§lMetaItems - Get§r§l] §7§m     §r"
            ), false);
            source.sendFailure(Component.literal("§cUnknown meta item §e" + name + "§c."));
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal(
            "§7§m     §r §l[§6§lMetaItems - Get§r§l] §7§m     §r"
        ), false);
        
        final ItemStack itemToGive = bannedItem.toItemStack();
        if (!player.getInventory().add(itemToGive)) {
            source.sendFailure(Component.literal("§cCannot get the meta item, your inventory is full."));
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal("§aSuccessfully received §e" + name + "§a."), true);
        ModMain.LOGGER.info("Player {} received meta item '{}'", player.getName().getString(), name);
        return 1;
    }
    
    public static int removeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        final ModMain plugin = ModMain.getInstance();
        
        if (plugin == null) {
            source.sendFailure(Component.literal("§cMod not initialized!"));
            return 0;
        }
        
        final String name = context.getArgument("name", String.class);
        
        if (!plugin.getBanDatabase().getMetaItems().containsKey(name)) {
            source.sendSuccess(() -> Component.literal(
                "§7§m     §r §l[§6§lMetaItems - Remove§r§l] §7§m     §r"
            ), false);
            source.sendFailure(Component.literal("§cThere is no meta item named §e" + name + "§c."));
            return 0;
        }
        
        try {
            plugin.getBanDatabase().removeMetaItem(name);
            
            if (plugin.getListener() != null) {
                plugin.getListener().load();
            }
            
            source.sendSuccess(() -> Component.literal(
                "§7§m     §r §l[§6§lMetaItems - Remove§r§l] §7§m     §r"
            ), false);
            source.sendSuccess(() -> Component.literal("§aMeta item §e" + name + "§a removed."), true);
            
            ModMain.LOGGER.info("Meta item '{}' removed by {}", name, source.getTextName());
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cUnable to remove meta item."));
            source.sendFailure(Component.literal("§cCheck the console for more information."));
            ModMain.LOGGER.error("Error removing meta item", e);
            return 0;
        }
    }
    
    public static int listCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        final ModMain plugin = ModMain.getInstance();
        
        if (plugin == null) {
            source.sendFailure(Component.literal("§cMod not initialized!"));
            return 0;
        }
        
        final Set<String> metaItems = plugin.getBanDatabase().getMetaItems().keySet();
        
        source.sendSuccess(() -> Component.literal(
            "§7§m     §r §l[§6§lMetaItems - List§r§l] §7§m     §r"
        ), false);
        
        if (metaItems.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7There is no meta item added yet."), false);
            return 1;
        }
        
        final String itemsList = String.join("§7, §6", metaItems);
        source.sendSuccess(() -> Component.literal("§aMeta items: §6" + itemsList + "§7."), false);
        return 1;
    }
}
