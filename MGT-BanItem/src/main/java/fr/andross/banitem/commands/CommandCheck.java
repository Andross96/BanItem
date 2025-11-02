/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.andross.banitem.BanDatabase;
import fr.andross.banitem.BanUtils;
import fr.andross.banitem.ModMain;
import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.items.BannedItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * /banitem check command implementation
 * Usage: /banitem check [delete]
 */
public class CommandCheck {
    
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return checkInventories(context.getSource(), false);
    }
    
    public static int executeDelete(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return checkInventories(context.getSource(), true);
    }
    
    private static int checkInventories(CommandSourceStack source, boolean delete) {
        final ModMain plugin = ModMain.getInstance();
        
        if (plugin == null) {
            source.sendFailure(Component.literal("§cMod not initialized!"));
            return 0;
        }
        
        final Set<String> playersWithBannedItems = new HashSet<>();
        final BanDatabase.Blacklist blacklist = plugin.getBanDatabase().getBlacklist();
        
        // Check all online players
        for (final ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            final Map<BannedItem, Map<BanAction, BanActionData>> worldBlacklist = 
                blacklist.get(player.level());
            
            if (worldBlacklist == null || worldBlacklist.isEmpty()) {
                continue; // No banned items in this world
            }
            
            // Check player inventory
            boolean foundBanned = false;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                final ItemStack item = player.getInventory().getItem(i);
                
                if (BanUtils.isNullOrAir(item)) {
                    continue;
                }
                
                final BannedItem bannedItem = new BannedItem(item);
                final Map<BanAction, BanActionData> actionData = worldBlacklist.get(bannedItem);
                
                if (actionData != null && !actionData.isEmpty()) {
                    // Found banned item
                    foundBanned = true;
                    
                    if (delete) {
                        player.getInventory().removeItem(i, item.getCount());
                    }
                }
            }
            
            if (foundBanned) {
                playersWithBannedItems.add(player.getName().getString());
            }
        }
        
        // Send results
        source.sendSuccess(() -> Component.literal(
            "§7§m     §r §l[§6§lCheck§r§l] §7§m     §r"
        ), false);
        
        if (playersWithBannedItems.isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                "§7No player with blacklisted item in inventory found."
            ), false);
            return 1;
        }
        
        final String playersList = String.join("§7, §6", playersWithBannedItems);
        source.sendSuccess(() -> Component.literal(
            "§7Found §2" + playersWithBannedItems.size() + "§7 player(s):"
        ), false);
        source.sendSuccess(() -> Component.literal("§6" + playersList + "§7."), false);
        
        if (delete) {
            source.sendSuccess(() -> Component.literal(
                "§7§oSuccessfully removed banned items from §e§o" + 
                playersWithBannedItems.size() + "§7§o players."
            ), true);
            
            ModMain.LOGGER.info("Removed banned items from {} players by {}", 
                playersWithBannedItems.size(), source.getTextName());
        } else {
            ModMain.LOGGER.info("Check found {} players with banned items", 
                playersWithBannedItems.size());
        }
        
        return 1;
    }
}
