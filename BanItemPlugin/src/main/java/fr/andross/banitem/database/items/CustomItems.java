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
package fr.andross.banitem.database.items;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.items.CustomBannedItem;
import fr.andross.banitem.utils.DoubleMap;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.debug.DebugMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Locale;

/**
 * Map that contains all the custom items
 * This is a double map <i>(include a reversed map)</i>, for easier access of
 * custom items names and their respective banned item.
 *
 * @author Andross
 * @version 3.1
 */
public final class CustomItems extends DoubleMap<String, CustomBannedItem> {
    /**
     * Represents the customitems.yml file.
     */
    private final File file;

    /**
     * Represents the loaded configuration from customitems.yml file.
     */
    private final FileConfiguration config;

    /**
     * This will create a new instance of custom items map, with the items from <i>customitems.yml</i> file.
     * This should not be used externally, as it could create two different instance of this object.
     * You should use {@link fr.andross.banitem.BanItemAPI#load(CommandSender, File)} instead.
     *
     * @param plugin     main instance
     * @param sender the sender who executed this command, for debug
     */
    public CustomItems(@NotNull final BanItem plugin, @NotNull final CommandSender sender) {
        this.file = new File(plugin.getDataFolder(), "customitems.yml");
        if (!file.exists()) {
            plugin.saveResource("customitems.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);

        // Loading custom items
        for (final String key : config.getKeys(false)) {
            final ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            final Debug d = new Debug(plugin.getBanConfig(), sender, new DebugMessage("customitems.yml"), new DebugMessage(key));
            final CustomBannedItem customBannedItem = new CustomBannedItem(key.toLowerCase(Locale.ROOT), section, d);
            if (customBannedItem.isValid()) {
                put(key, customBannedItem);
            }
        }
    }

    /**
     * Represents the file "customitems.yml".
     *
     * @return The "customitems.yml" file of the BanItem plugin
     */
    @NotNull
    public File getFile() {
        return file;
    }

    /**
     * Represents the loaded configuration from "customitems.yml" file.
     *
     * @return The "customitems.yml" file configuration
     */
    @NotNull
    public FileConfiguration getConfig() {
        return config;
    }
}