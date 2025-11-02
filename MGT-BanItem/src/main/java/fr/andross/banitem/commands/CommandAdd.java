/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.andross.banitem.ModMain;
import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.actions.BanDataType;
import fr.andross.banitem.items.BannedItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * /banitem add command implementation
 * Usage: /banitem add <actions> [-m materials] [-w worlds] [message]
 */
public class CommandAdd {
    
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        final ModMain plugin = ModMain.getInstance();
        
        if (plugin == null) {
            source.sendFailure(Component.literal("§cMod not initialized!"));
            return 0;
        }
        
        // Parse the full argument string
        final String fullArgs = context.getArgument("actions", String.class);
        final String[] args = fullArgs.split("\\s+");
        
        if (args.length < 1) {
            sendHelp(source);
            return 0;
        }
        
        // Parse actions
        final List<BanAction> actions = CommandHelper.parseActions(args[0]);
        if (actions.isEmpty()) {
            source.sendFailure(Component.literal("§cInvalid actions: §e" + args[0]));
            source.sendSuccess(() -> Component.literal("§7Valid actions: §o" + CommandHelper.getActionsList()), false);
            return 0;
        }
        
        // Parse materials and worlds from arguments
        final List<Item> materials = new ArrayList<>();
        final List<ServerLevel> worlds = new ArrayList<>();
        final StringBuilder messageBuilder = new StringBuilder();
        
        int i = 1;
        while (i < args.length) {
            if (args[i].equalsIgnoreCase("-m") && i + 1 < args.length) {
                // Parse materials
                final List<Item> parsed = CommandHelper.parseItems(args[i + 1]);
                if (parsed.isEmpty()) {
                    source.sendFailure(Component.literal("§cInvalid materials: §e" + args[i + 1]));
                    return 0;
                }
                materials.addAll(parsed);
                i += 2;
            } else if (args[i].equalsIgnoreCase("-w") && i + 1 < args.length) {
                // Parse worlds
                final List<ServerLevel> parsed = CommandHelper.parseWorlds(args[i + 1], source.getServer());
                if (parsed.isEmpty()) {
                    source.sendFailure(Component.literal("§cInvalid worlds: §e" + args[i + 1]));
                    source.sendSuccess(() -> Component.literal("§7Valid worlds: §o" + 
                        CommandHelper.getWorldsList(source.getServer())), false);
                    return 0;
                }
                worlds.addAll(parsed);
                i += 2;
            } else {
                // Rest is the message
                if (messageBuilder.length() > 0) messageBuilder.append(" ");
                messageBuilder.append(args[i]);
                i++;
            }
        }
        
        // Get material from player's hand if not specified
        if (materials.isEmpty()) {
            if (source.getEntity() instanceof ServerPlayer player) {
                final ItemStack heldItem = player.getMainHandItem();
                if (!heldItem.isEmpty()) {
                    materials.add(heldItem.getItem());
                } else {
                    source.sendFailure(Component.literal("§cYou must specify materials with -m or hold an item!"));
                    return 0;
                }
            } else {
                source.sendFailure(Component.literal("§cYou must specify materials with -m <materials>"));
                sendHelp(source);
                return 0;
            }
        }
        
        // Get player's world if not specified
        if (worlds.isEmpty()) {
            if (source.getEntity() instanceof ServerPlayer player) {
                worlds.add(player.serverLevel());
            } else {
                source.sendFailure(Component.literal("§cYou must specify worlds with -w <worlds>"));
                sendHelp(source);
                return 0;
            }
        }
        
        // Create action data
        final BanActionData actionData = new BanActionData();
        if (messageBuilder.length() > 0) {
            final String message = CommandHelper.colorize(messageBuilder.toString());
            actionData.getMap().put(BanDataType.MESSAGE, Collections.singletonList(message));
        }
        
        // Create actions map
        final Map<BanAction, BanActionData> actionsData = new EnumMap<>(BanAction.class);
        for (final BanAction action : actions) {
            actionsData.put(action, actionData);
        }
        
        // Convert materials to BannedItems
        final List<BannedItem> bannedItems = materials.stream()
                .map(item -> new BannedItem(new ItemStack(item)))
                .toList();
        
        // Add to blacklist
        final ServerLevel[] worldsArray = worlds.toArray(new ServerLevel[0]);
        if (plugin.getApi().addToBlacklist(bannedItems, actionsData, worldsArray)) {
            source.sendSuccess(() -> Component.literal(
                "§7§m     §r §l[§6§lAdd§r§l] §7§m     §r"
            ), false);
            
            final String materialsName = materials.size() > 10 ? "these materials" : 
                String.join(", ", materials.stream().map(Item::toString).toList());
            final String worldsName = worlds.size() == source.getServer().getAllLevels().size() ? "§2ALL" :
                String.join(", ", worlds.stream().map(w -> w.dimension().location().toString()).toList());
            
            source.sendSuccess(() -> Component.literal(
                "§aSuccessfully banned §e" + materialsName + " §afor world §2" + worldsName + "§a."
            ), true);
            
            if (source.getEntity() instanceof ServerPlayer) {
                source.sendSuccess(() -> Component.literal(
                    "§7§oNote: You may have bypass permissions, so the ban may not apply to you."
                ), false);
            }
            
            // Reload listeners
            if (plugin.getListener() != null) {
                plugin.getListener().load();
            }
            
            ModMain.LOGGER.info("Added ban by {}: {} materials in {} worlds", 
                source.getTextName(), materials.size(), worlds.size());
            return 1;
        }
        
        source.sendFailure(Component.literal("§cAn error occurred while banning the item."));
        source.sendFailure(Component.literal("§cCheck the console for more information."));
        return 0;
    }
    
    private static void sendHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(
            "§7§m     §r §l[§6§lAdd§r§l] §7§m     §r\n" +
            "§7Usage:\n" +
            "§b/bi add §3<actions> [-m materials] [-w worlds] [message]\n" +
            "§7 >> Will ban the item (material).\n" +
            "§7 >> If no materials are entered, uses item in your hand.\n" +
            "§7 >> If no worlds are entered, uses your current world.\n" +
            "§2§oExample: /bi add place,break This item is banned.\n" +
            "§7§oPlayers will not be able to place nor break the item.\n" +
            "§2§oExample2: /bi add place -m minecraft:stone -w minecraft:overworld"
        ), false);
    }
}
