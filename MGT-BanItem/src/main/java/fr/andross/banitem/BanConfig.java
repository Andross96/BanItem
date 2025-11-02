/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.andross.banitem.actions.BanAction;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Configuration handler for MGT-BanItem mod.
 * Loads and manages configuration from config file.
 */
public class BanConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final ModMain plugin;
    private final File configFile;
    private Map<String, Object> config;
    private String prefix;
    private final Set<BanAction> priority = EnumSet.noneOf(BanAction.class);

    public BanConfig(@NotNull final ModMain plugin, @NotNull final Path configDir) {
        this.plugin = plugin;
        this.configFile = configDir.resolve("mgt-banitem.json").toFile();
        
        loadConfig();
    }

    /**
     * Load or reload configuration from file.
     */
    public void loadConfig() {
        try {
            if (!configFile.exists()) {
                // Create default config
                createDefaultConfig();
            }
            
            // Load config from file
            try (Reader reader = new FileReader(configFile)) {
                config = GSON.fromJson(reader, Map.class);
                if (config == null) {
                    config = new HashMap<>();
                }
            }
            
            // Load prefix
            prefix = (String) config.getOrDefault("prefix", "&c[&eMGT-BanItem&c] ");
            
            // Load priority actions
            final List<String> priorityList = (List<String>) config.getOrDefault("priority", new ArrayList<>());
            for (final String actionName : priorityList) {
                try {
                    priority.add(BanAction.valueOf(actionName.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    ModMain.LOGGER.warn("Unknown priority action: {}", actionName);
                }
            }
            
            ModMain.LOGGER.info("Configuration loaded successfully");
        } catch (Exception e) {
            ModMain.LOGGER.error("Failed to load configuration", e);
            config = new HashMap<>();
            prefix = "&c[&eMGT-BanItem&c] ";
        }
    }

    private void createDefaultConfig() throws IOException {
        configFile.getParentFile().mkdirs();
        
        final Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("prefix", "&c[&eMGT-BanItem&c] ");
        defaultConfig.put("check-update", true);
        defaultConfig.put("priority", Arrays.asList("delete", "wear"));
        
        // Create default blacklist structure
        final Map<String, Object> blacklist = new HashMap<>();
        defaultConfig.put("blacklist", blacklist);
        
        // Create default whitelist structure
        final Map<String, Object> whitelist = new HashMap<>();
        defaultConfig.put("whitelist", whitelist);
        
        // Save default config
        try (Writer writer = new FileWriter(configFile)) {
            GSON.toJson(defaultConfig, writer);
        }
        
        ModMain.LOGGER.info("Created default configuration file");
    }

    public boolean saveConfig() {
        try (Writer writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
            return true;
        } catch (IOException e) {
            ModMain.LOGGER.error("Failed to save configuration", e);
            return false;
        }
    }

    @NotNull
    public String getPrefix() {
        return prefix;
    }

    @NotNull
    public Set<BanAction> getPriority() {
        return priority;
    }

    @NotNull
    public Map<String, Object> getConfig() {
        return config;
    }

    @NotNull
    public File getConfigFile() {
        return configFile;
    }
}
