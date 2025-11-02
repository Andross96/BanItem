/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem;

import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.items.BannedItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Database containing blacklisted and whitelisted items.
 */
public class BanDatabase {
    private final Blacklist blacklist;
    private final Whitelist whitelist;
    private final Map<String, BannedItem> customItems;
    private final Map<String, BannedItem> metaItems;

    public BanDatabase(@NotNull final ModMain plugin, @NotNull final BanConfig config) {
        this.blacklist = new Blacklist();
        this.whitelist = new Whitelist();
        this.customItems = new HashMap<>();
        this.metaItems = new HashMap<>();
        
        // Load blacklist and whitelist from config
        loadFromConfig(config);
    }

    private void loadFromConfig(@NotNull final BanConfig config) {
        final Map<String, Object> configData = config.getConfig();
        
        // Load blacklist
        final Map<String, Object> blacklistData = (Map<String, Object>) configData.getOrDefault("blacklist", new HashMap<>());
        // TODO: Parse and load blacklist entries
        
        // Load whitelist
        final Map<String, Object> whitelistData = (Map<String, Object>) configData.getOrDefault("whitelist", new HashMap<>());
        // TODO: Parse and load whitelist entries
        
        ModMain.LOGGER.info("Loaded {} blacklisted items and {} whitelisted items",
                blacklist.getTotalBlacklistedItems(), whitelist.getTotalWhitelistedItems());
    }

    @NotNull
    public Blacklist getBlacklist() {
        return blacklist;
    }

    @NotNull
    public Whitelist getWhitelist() {
        return whitelist;
    }

    @NotNull
    public Map<String, BannedItem> getCustomItems() {
        return customItems;
    }

    @NotNull
    public Map<String, BannedItem> getMetaItems() {
        return metaItems;
    }

    public void addMetaItem(@NotNull final String name, @NotNull final BannedItem item) {
        metaItems.put(name, item);
    }

    public void removeMetaItem(@NotNull final String name) {
        metaItems.remove(name);
    }

    /**
     * Get the name of an item (for config/display purposes).
     */
    @NotNull
    public String getName(@NotNull final BannedItem item) {
        // Check if it's a meta item
        for (final Map.Entry<String, BannedItem> entry : metaItems.entrySet()) {
            if (entry.getValue().equals(item)) {
                return entry.getKey();
            }
        }
        
        // Check if it's a custom item
        for (final Map.Entry<String, BannedItem> entry : customItems.entrySet()) {
            if (entry.getValue().equals(item)) {
                return entry.getKey();
            }
        }
        
        // Return the item's registry name
        return item.getType().toString().toLowerCase();
    }

    /**
     * Blacklist handler.
     */
    public static class Blacklist {
        private final Map<String, Map<BannedItem, Map<BanAction, BanActionData>>> items = new HashMap<>();

        public void addNewBan(@NotNull final Level world,
                            @NotNull final BannedItem item,
                            @NotNull final Map<BanAction, BanActionData> actions) {
            final String worldName = world.dimension().location().toString();
            items.computeIfAbsent(worldName, k -> new HashMap<>()).put(item, actions);
        }
        
        public void removeBan(@NotNull final Level world, @NotNull final BannedItem item) {
            final String worldName = world.dimension().location().toString();
            final Map<BannedItem, Map<BanAction, BanActionData>> worldItems = items.get(worldName);
            if (worldItems != null) {
                worldItems.remove(item);
            }
        }
        
        @Nullable
        public Map<BannedItem, Map<BanAction, BanActionData>> get(@NotNull final Level world) {
            final String worldName = world.dimension().location().toString();
            return items.get(worldName);
        }

        public int getTotalBlacklistedItems() {
            return items.values().stream().mapToInt(Map::size).sum();
        }

        public boolean isEmpty() {
            return items.isEmpty();
        }

        public Set<BanAction> getActions() {
            final Set<BanAction> actions = EnumSet.noneOf(BanAction.class);
            for (final Map<BannedItem, Map<BanAction, BanActionData>> worldItems : items.values()) {
                for (final Map<BanAction, BanActionData> itemActions : worldItems.values()) {
                    actions.addAll(itemActions.keySet());
                }
            }
            return actions;
        }
    }

    /**
     * Whitelist handler.
     */
    public static class Whitelist {
        private final Map<String, Map<BannedItem, Map<BanAction, BanActionData>>> items = new HashMap<>();

        public void addNewException(@NotNull final Level world,
                                   @NotNull final BannedItem item,
                                   @NotNull final Map<BanAction, BanActionData> actions) {
            final String worldName = world.dimension().location().toString();
            items.computeIfAbsent(worldName, k -> new HashMap<>()).put(item, actions);
        }

        public int getTotalWhitelistedItems() {
            return items.values().stream().mapToInt(Map::size).sum();
        }

        public boolean isEmpty() {
            return items.isEmpty();
        }
    }
}
