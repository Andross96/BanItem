/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2021 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your action) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.andross.banitem.utils.scanners.illegalstack;

import fr.andross.banitem.BanConfig;
import fr.andross.banitem.BanItem;
import fr.andross.banitem.BanUtils;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.debug.DebugMessage;
import fr.andross.banitem.utils.list.Listable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Async scanner to check if players has illegal stacks into their inventories.
 * If an illegal stack is detected, a next sync task will be run to handle it.
 *
 * @author Andross
 * @version 3.4
 */
public final class IllegalStackScanner {
    private final BanItem plugin;
    private final BanUtils utils;
    private boolean enabledInConfig = false;
    private boolean enabled = false;
    private int taskId = -1;
    private final Map<World, Map<Material, IllegalStackItemConfig>> items = new HashMap<>();
    private boolean vanillaMaxStackSize = false;
    private IllegalStackBlockType defaultBlockType;

    public IllegalStackScanner(@NotNull final BanItem plugin, @NotNull final BanUtils utils) {
        this.plugin = plugin;
        this.utils = utils;
    }

    /**
     * Load the configuration file and enable (if configured) the illegal stack scanner.
     *
     * @param sender the executor
     * @param config the configuration file
     */
    public void load(@NotNull final CommandSender sender, @NotNull final BanConfig config) {
        // Clearing
        items.clear();

        // Loading config
        final ConfigurationSection section = config.getConfig().getConfigurationSection("illegal-stacks");
        if (section == null) {
            return;
        }
        enabledInConfig = section.getBoolean("enabled");
        if (!enabledInConfig) {
            return;
        }
        vanillaMaxStackSize = section.getBoolean("vanilla-max-stack-size");
        defaultBlockType = getBlockType(section.getString("block-type"));
        if (defaultBlockType == null) {
            utils.sendMessage(sender, "&c[Illegal-Stack] The default 'block-type' is not set or invalid.");
            enabledInConfig = false;
            return;
        }

        // Loading items
        final ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null) {
            final Debug d = new Debug(config, sender, new DebugMessage("illegal-stacks")).add("items");
            for (final String itemKey : itemsSection.getKeys(false)) {
                final List<Material> materials = Listable.getMaterials(itemKey, d.clone().add(itemKey));
                if (materials.isEmpty()) {
                    continue;
                }

                // Loading material info
                final ConfigurationSection itemSubSection = itemsSection.getConfigurationSection(itemKey);
                if (itemSubSection == null) {
                    continue;
                }
                final int amount = itemSubSection.getInt("amount");
                if (amount < 1) {
                    continue;
                }
                String blockTypeString = itemSubSection.getString("block-type");
                final IllegalStackBlockType blockType;
                if (blockTypeString == null) {
                    blockType = defaultBlockType;
                } else {
                    blockType = getBlockType(blockTypeString);
                    if (blockType == null) {
                        d.clone().add(itemKey).add("block-type").add("The 'block-type' is unknown.").sendDebug();
                        continue;
                    }
                }

                // Loading world(s)
                final String worldInConfig = itemSubSection.getString("world");
                final List<World> worlds;
                if (worldInConfig == null) {
                    worlds = new ArrayList<>(Bukkit.getWorlds());
                } else {
                    worlds = Listable.getWorlds(worldInConfig, d.clone().add(itemKey).add("world"));
                }

                // Adding
                final IllegalStackItemConfig illegalStackItemConfig = new IllegalStackItemConfig(amount, blockType);
                for (final World world : worlds) {
                    materials.forEach(m -> addIllegalStackItem(world, m, illegalStackItemConfig));
                }
            }
        }

        if (enabledInConfig && !enabled && (vanillaMaxStackSize || !items.isEmpty())) {
            setEnabled(true);
        }
    }

    private IllegalStackBlockType getBlockType(@Nullable final String blockType) {
        if (blockType == null) {
            return null;
        }
        try {
            return IllegalStackBlockType.valueOf(blockType.toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    public void addIllegalStackItem(final World world,
                                    final Material m,
                                    final IllegalStackItemConfig illegalStackItemConfig) {
        final Map<Material, IllegalStackItemConfig> subMap = items.getOrDefault(world, new HashMap<>());
        subMap.put(m, illegalStackItemConfig);
        items.put(world, subMap);
    }

    /**
     * Check if the scanner is running.
     *
     * @return true if the scanner is running, otherwise false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the scanner.
     *
     * @param enabled the enabled state
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            if (taskId < 0) {
                taskId = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> Bukkit.getOnlinePlayers().forEach(utils::checkPlayerIllegalStacks), 16L, 16L).getTaskId();
            }
        } else {
            if (taskId > -1) {
                plugin.getServer().getScheduler().cancelTask(taskId);
                taskId = -1;
            }
        }
    }

    /**
     * Check if the scanner should be enabled (in config).
     *
     * @return if the scanner should be enabled (in config)
     */
    public boolean isEnabledInConfig() {
        return enabledInConfig;
    }

    /**
     * Set the variable. This does not edit the config file.
     *
     * @param enabledInConfig set the variable
     */
    public void setEnabledInConfig(final boolean enabledInConfig) {
        this.enabledInConfig = enabledInConfig;
    }

    /**
     * Get the scanner Bukkit Task id, -1 if not running.
     *
     * @return the scanner Bukkit Task id, -1 if not running
     */
    public int getTaskId() {
        return taskId;
    }

    /**
     * Get the map of illegal stacks configuration loaded from config.
     *
     * @return the map of illegal stacks configuration loaded from config
     */
    @NotNull
    public Map<World, Map<Material, IllegalStackItemConfig>> getItems() {
        return items;
    }

    /**
     * Check if the vanilla max stack size is enabled.
     *
     * @return true if the vanilla max stack size is enabled, otherwise false
     */
    public boolean isVanillaMaxStackSize() {
        return vanillaMaxStackSize;
    }

    /**
     * Set the vanilla max stack size state.
     * This does not edit the config file.
     *
     * @param vanillaMaxStackSize the vanilla max stack size state
     */
    public void setVanillaMaxStackSize(final boolean vanillaMaxStackSize) {
        this.vanillaMaxStackSize = vanillaMaxStackSize;
    }

    /**
     * Get the default block type for items.
     * This can be null if an invalid block type is set from config.
     *
     * @return the default block type for items
     */
    @Nullable
    public IllegalStackBlockType getDefaultBlockType() {
        return defaultBlockType;
    }

    /**
     * Set the default block type for items.
     * Setting the parameter to null is not recommended because it will keep the task running
     * but will not block anything.
     *
     * @param defaultBlockType the default block type wanted
     */
    public void setDefaultBlockType(@Nullable final IllegalStackBlockType defaultBlockType) {
        this.defaultBlockType = defaultBlockType;
    }
}
