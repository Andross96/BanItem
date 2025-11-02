/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.andross.banitem.ModMain;
import fr.andross.banitem.items.BannedItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * /banitem remove command implementation
 * Usage: /banitem remove [-m materials] [-w worlds]
 */
public class CommandRemove {
    
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        final ModMain plugin = ModMain.getInstance();
        
        if (plugin == null) {
            source.sendFailure(Component.literal("§cMod not initialized!"));
            return 0;
        }
        
        // Parse the full argument string
        final String fullArgs = context.getArgument("args", String.class);
        final String[] args = fullArgs.split("\\s+");
        
        // Parse materials and worlds from arguments
        final List<Item> materials = new ArrayList<>();
        final List<ServerLevel> worlds = new ArrayList<>();
        
        int i = 0;
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
        
        // Convert materials to BannedItems
        final List<BannedItem> bannedItems = materials.stream()
                .map(item -> new BannedItem(new ItemStack(item)))
                .toList();
        
        // Remove from blacklist
        final ServerLevel[] worldsArray = worlds.toArray(new ServerLevel[0]);
        plugin.getApi().removeFromBlacklist(bannedItems, worldsArray);
        
        source.sendSuccess(() -> Component.literal(
            "§7§m     §r §l[§6§lRemove§r§l] §7§m     §r"
        ), false);
        
        if (materials.size() == 1) {
            source.sendSuccess(() -> Component.literal("§aThe item is successfully unbanned."), true);
        } else {
            source.sendSuccess(() -> Component.literal("§aThe items are successfully unbanned."), true);
        }
        
        // Reload listeners
        if (plugin.getListener() != null) {
            plugin.getListener().load();
        }
        
        ModMain.LOGGER.info("Removed ban by {}: {} materials in {} worlds", 
            source.getTextName(), materials.size(), worlds.size());
        return 1;
    }
    
    private static void sendHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(
            "§7§m     §r §l[§6§lRemove§r§l] §7§m     §r\n" +
            "§7Usage:\n" +
            "§b/bi remove §3[-m materials] [-w worlds]\n" +
            "§7 >> Will unban the item (material).\n" +
            "§7 >> If no materials are entered, uses item in your hand.\n" +
            "§7 >> If no worlds are entered, uses your current world.\n" +
            "§2§oExample: /bi remove -m minecraft:stone -w minecraft:overworld"
        ), false);
    }
}
