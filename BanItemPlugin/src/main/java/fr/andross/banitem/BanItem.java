/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2020 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import fr.andross.banitem.commands.BanCommand;
import fr.andross.banitem.config.BanConfig;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Main plugin class
 * @version 2.4
 * @author Andross
 */
public final class BanItem extends JavaPlugin {
    private static BanItem instance;
    private BanItemAPI api;
    private BanConfig banConfig;
    private BanHooks hooks;
    private BanDatabase banDatabase;
    private final BanUtils utils = new BanUtils(this);
    private final BanListener listener = new BanListener(this);

    @Override
    public void onEnable() {
        instance = this;
        api = new BanItemAPI(this);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!isEnabled()) return;

            // Metrics
            new Metrics(this, 7822);

            // Loading plugin on next tick after worlds
            load(Bukkit.getConsoleSender(), null);

            // Update checker
            if (banConfig.isCheckUpdate())
                Bukkit.getScheduler().runTaskAsynchronously(this, utils::checkForUpdate);
        }, 20L);
    }

    /**
     * (re)Loading the plugin with this configuration file
     * @param sender command sender <i>(send the message debug to)</i>
     * @param config the file configuration to load. If null, using (and reloading) the default config
     */
    protected void load(@NotNull final CommandSender sender, @Nullable FileConfiguration config) {
        // (re)Loading config
        if (config == null) {
            saveDefaultConfig();
            reloadConfig();
            config = getConfig();
        }
        banConfig = new BanConfig(this, sender, config);

        // (re)Loading hooks
        hooks = new BanHooks(this, sender);

        // (re)Loading database
        banDatabase = new BanDatabase(this, sender, config);

        // (re)Loading listeners
        listener.load(sender);

        // Result
        utils.sendMessage(sender, "&2Successfully loaded &e" + banDatabase.getBlacklist().getTotal() + "&2 blacklisted & &e" + banDatabase.getWhitelist().getTotal() + "&2 whitelisted item(s).");
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, final @NotNull String label, @NotNull final String[] args) {
        try {
            BanCommand.runCommand(this, args[0], sender, args);
        } catch (final Exception e) {
            // Permission?
            if (!sender.hasPermission("banitem.command.help")) {
                final String message = getConfig().getString("no-permission");
                if (message != null) utils.sendMessage(sender, message);
                return true;
            }

            // Help messages
            utils.sendMessage(sender,"&7&m     &r &l[&7&lUsage - &e&lv" + getDescription().getVersion() + "&r&l] &7&m     ");
            utils.sendMessage(sender," &7- /bi &3add&7: add an item in blacklist.");
            utils.sendMessage(sender," &7- /bi &3check&7: check if any player has a blacklisted item.");
            utils.sendMessage(sender," &7- /bi &3customitem&7: add/remove/list custom items.");
            utils.sendMessage(sender," &7- /bi &3help&7: gives additional informations.");
            utils.sendMessage(sender," &7- /bi &3info&7: get info about your item in hand.");
            utils.sendMessage(sender," &7- /bi &3log&7: activate the log mode.");
            utils.sendMessage(sender," &7- /bi &3reload&7: reload the config.");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String alias, @NotNull final String[] args) {
        // Has permission?
        if (!sender.hasPermission("banitem.command.help")) return new ArrayList<>();

        // Sub command
        if (args.length == 1) return StringUtil.copyPartialMatches(args[0], utils.getCommands(), new ArrayList<>());

        // Running subcommand
        try {
            return BanCommand.runTab(this, args[0], sender, args);
        } catch (final Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Gives the current instance of the plugin.
     * The plugin should not be accessed this way, but rather with {@link org.bukkit.plugin.PluginManager#getPlugin(String)}
     * @return the current instance of the plugin
     */
    @NotNull
    public static BanItem getInstance() {
        return instance;
    }

    /**
     * Get the ban api
     * @return the ban item api
     */
    @NotNull
    public BanItemAPI getApi() {
        return api;
    }

    /**
     * Get a the ban config
     * @return the ban config
     */
    @NotNull
    public BanConfig getBanConfig() {
        return banConfig;
    }

    /**
     * Get the ban hooks
     * @return the ban hooks
     */
    public BanHooks getHooks() {
        return hooks;
    }

    /**
     * Get the ban database, containing blacklist, whitelist and customitems
     * @return the ban database
     */
    @NotNull
    public BanDatabase getBanDatabase() {
        return banDatabase;
    }

    /**
     * An utility class for the plugin
     * @return an utility class
     */
    @NotNull
    public BanUtils getUtils() {
        return utils;
    }

    /**
     * The listener class
     * @return listener class of the plugin
     */
    @NotNull
    public BanListener getListener() {
        return listener;
    }
}
