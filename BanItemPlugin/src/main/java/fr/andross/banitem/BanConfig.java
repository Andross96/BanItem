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
package fr.andross.banitem;

import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.utils.BanAnimation;
import fr.andross.banitem.utils.Chat;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.debug.DebugMessage;
import fr.andross.banitem.utils.list.ListType;
import fr.andross.banitem.utils.list.Listable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * A cached ban configuration from a FileConfiguration
 * @version 3.1
 * @author Andross
 */
public final class BanConfig {
    private final File configFile;
    private final FileConfiguration config;
    private final String prefix;
    private final Set<BanAction> priority = EnumSet.noneOf(BanAction.class);
    private final BanAnimation animation;
    private final Set<String> ignoredInventoryTitles = new HashSet<>();

    /**
     * Loading the FileConfiguration.
     * This should not be used externally.
     * Use {@link fr.andross.banitem.BanItemAPI#load(CommandSender, File)} instead.
     */
    BanConfig(@NotNull final BanItem pl, @NotNull final CommandSender sender, @Nullable final File configFile) {
        if (configFile == null) {
            pl.saveDefaultConfig();
            pl.reloadConfig();
            this.configFile = new File(pl.getDataFolder(), "config.yml");
            this.config = pl.getConfig();
        } else {
            this.configFile = configFile;
            this.config = new YamlConfiguration();
            try {
                config.load(configFile);
            } catch (final IOException | InvalidConfigurationException e) {
                pl.getLogger().log(Level.WARNING, "Can not load config file '" + configFile.getName() + "': " + e.getMessage(), e);
                sender.sendMessage(Chat.color("&cCan not load config file '" + configFile.getName() + "': " + e.getMessage()));
                sender.sendMessage(Chat.color("&cDetailled error message on console."));
            }
        }

        // Loading prefix
        final String prefix = this.config.getString("prefix");
        this.prefix = prefix == null ? "" : Chat.color(prefix);

        // Loading priority
        final List<String> priority = Listable.getSplittedStringList(this.config.get("priority"));
        if (!priority.isEmpty())
            this.priority.addAll(Listable.getList(ListType.ACTION, priority, new Debug(this, sender, new DebugMessage(null, this.config.getName()), new DebugMessage(null, "priority"))));

         // Loading animation
        animation = new BanAnimation(sender, this);

        // Loading action configuration
        // Loading ignored inventory titles
        ignoredInventoryTitles.addAll(this.config.getStringList("actions.delete.ignored-inventories-titles"));
    }

    /**
     * Get the current config file name used
     * @return the current config file name used
     */
    @NotNull
    public String getConfigName() {
        return configFile.getName();
    }

    /**
     * Get the config file used in this instance
     * @return the config file used in this instance
     */
    @NotNull
    public File getConfigFile() {
        return configFile;
    }

    /**
     * FileConfiguration loaded in this instance
     * @return the FileConfiguration loaded
     */
    @NotNull
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Save the current config into the file
     * @return true if successfully saved, otherwise false
     */
    public boolean saveConfig() {
        try {
            config.save(configFile);
        } catch (final IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Unable to save file '" + configFile.getName() + "': " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * Gives the plugin prefix used in the config (already colored)
     * @return plugin prefix used in the config (already colored)
     */
    @NotNull
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get the ban actions which requires the highest priority
     * @return the ban actions which requires the highest priority
     */
    @NotNull
    public Set<BanAction> getPriority() {
        return priority;
    }

    /**
     * Get the ban animation
     * @return the ban animation
     */
    @NotNull
    public BanAnimation getAnimation() {
        return animation;
    }

    /**
     * Get the ignored inventories titles
     * @return the ignored inventories titles
     */
    @NotNull
    public Set<String> getIgnoredInventoryTitles() {
        return ignoredInventoryTitles;
    }
}
