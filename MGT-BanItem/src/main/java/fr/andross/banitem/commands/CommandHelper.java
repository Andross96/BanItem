/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem.commands;

import fr.andross.banitem.actions.BanAction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for command parsing and execution.
 */
public class CommandHelper {
    
    /**
     * Parse a comma-separated list of action names into BanAction enum values.
     *
     * @param actionsString comma-separated action names
     * @return list of BanAction enums, empty if none valid
     */
    @NotNull
    public static List<BanAction> parseActions(@NotNull final String actionsString) {
        final List<BanAction> actions = new ArrayList<>();
        final String[] parts = actionsString.toLowerCase().split(",");
        
        for (final String part : parts) {
            final String trimmed = part.trim();
            
            // Check for wildcard
            if ("*".equals(trimmed)) {
                return Arrays.asList(BanAction.values());
            }
            
            // Find matching action
            for (final BanAction action : BanAction.values()) {
                if (action.getName().equalsIgnoreCase(trimmed)) {
                    actions.add(action);
                    break;
                }
            }
        }
        
        return actions;
    }
    
    /**
     * Parse a comma-separated list of item names into Item objects.
     *
     * @param itemsString comma-separated item names
     * @return list of Items, empty if none valid
     */
    @NotNull
    public static List<Item> parseItems(@NotNull final String itemsString) {
        final List<Item> items = new ArrayList<>();
        final String[] parts = itemsString.toLowerCase().split(",");
        
        for (final String part : parts) {
            final String trimmed = part.trim();
            
            // Check for wildcard
            if ("*".equals(trimmed)) {
                return new ArrayList<>(BuiltInRegistries.ITEM.stream().toList());
            }
            
            // Try to parse as resource location
            try {
                ResourceLocation loc;
                if (trimmed.contains(":")) {
                    loc = ResourceLocation.parse(trimmed);
                } else {
                    loc = ResourceLocation.withDefaultNamespace(trimmed);
                }
                
                final Item item = BuiltInRegistries.ITEM.get(loc);
                if (item != null && !net.minecraft.world.item.Items.AIR.equals(item)) {
                    items.add(item);
                }
            } catch (Exception e) {
                // Invalid item name, skip
            }
        }
        
        return items;
    }
    
    /**
     * Parse a comma-separated list of world names into ServerLevel objects.
     *
     * @param worldsString comma-separated world names
     * @param server       the Minecraft server
     * @return list of ServerLevels, empty if none valid
     */
    @NotNull
    public static List<ServerLevel> parseWorlds(@NotNull final String worldsString, 
                                               @NotNull final MinecraftServer server) {
        final List<ServerLevel> worlds = new ArrayList<>();
        final String[] parts = worldsString.toLowerCase().split(",");
        
        for (final String part : parts) {
            final String trimmed = part.trim();
            
            // Check for wildcard
            if ("*".equals(trimmed)) {
                return new ArrayList<>(server.getAllLevels());
            }
            
            // Try common world names
            ServerLevel level = null;
            if ("overworld".equalsIgnoreCase(trimmed) || "world".equalsIgnoreCase(trimmed)) {
                level = server.getLevel(Level.OVERWORLD);
            } else if ("nether".equalsIgnoreCase(trimmed) || "the_nether".equalsIgnoreCase(trimmed)) {
                level = server.getLevel(Level.NETHER);
            } else if ("end".equalsIgnoreCase(trimmed) || "the_end".equalsIgnoreCase(trimmed)) {
                level = server.getLevel(Level.END);
            } else {
                // Try to parse as resource location
                try {
                    ResourceLocation loc;
                    if (trimmed.contains(":")) {
                        loc = ResourceLocation.parse(trimmed);
                    } else {
                        loc = ResourceLocation.withDefaultNamespace(trimmed);
                    }
                    
                    // Search for matching dimension
                    for (final ServerLevel serverLevel : server.getAllLevels()) {
                        if (serverLevel.dimension().location().equals(loc)) {
                            level = serverLevel;
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Invalid world name, skip
                }
            }
            
            if (level != null) {
                worlds.add(level);
            }
        }
        
        return worlds;
    }
    
    /**
     * Get a formatted list of action names.
     *
     * @return comma-separated action names
     */
    @NotNull
    public static String getActionsList() {
        return Arrays.stream(BanAction.values())
                .map(BanAction::getName)
                .collect(Collectors.joining(", "));
    }
    
    /**
     * Get a formatted list of world names.
     *
     * @param server the Minecraft server
     * @return comma-separated world names
     */
    @NotNull
    public static String getWorldsList(@NotNull final MinecraftServer server) {
        return server.getAllLevels().stream()
                .map(level -> level.dimension().location().toString())
                .collect(Collectors.joining(", "));
    }
    
    /**
     * Colorize a string using &amp; codes.
     */
    @NotNull
    public static String colorize(@NotNull final String text) {
        return text.replace('&', '§');
    }
}
