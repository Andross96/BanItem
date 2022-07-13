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

import fr.andross.banitem.commands.BanCommand;
import fr.andross.banitem.utils.Chat;
import fr.andross.banitem.utils.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * BanItemPlugin
 * @version 3.4
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
            if (banConfig.getConfig().getBoolean("check-update"))
                Bukkit.getScheduler().runTaskAsynchronously(this, utils::checkForUpdate);
        }, 20L);
    }

    /**
     * (re)Loading the plugin with this configuration file.
     * If no config file set, using the default config.yml one.
     * @param sender command sender <i>(send the message debug to)</i>
     * @param configFile the file configuration to load. If null, using (and reloading) the default config
     */
    public void load(@NotNull final CommandSender sender, @Nullable final File configFile) {
        final long start = System.currentTimeMillis();

        // Removing all tasks
        utils.getWearScanner().setEnabled(false);
        utils.getIllegalStackScanner().setEnabled(false);
        getServer().getScheduler().cancelTasks(this);

        // (re)Loading config
        banConfig = new BanConfig(this, sender, configFile);

        // (re)Loading hooks
        hooks = new BanHooks(this, sender);

        // (re)Loading database
        banDatabase = new BanDatabase(this, sender, banConfig.getConfig());

        // (re)Loading listeners
        listener.load(sender);

        // (re)Loading illegal stack scanner
        utils.getIllegalStackScanner().load(sender, banConfig);

        // Result
        final long end = System.currentTimeMillis();
        final boolean moredebug = banConfig.getConfig().getBoolean("debug.reload");
        if (moredebug) {
            utils.sendMessage(sender, "&2Successfully loaded &e" + banDatabase.getBlacklist().getTotal() + "&2 blacklisted & &e" + banDatabase.getWhitelist().getTotal() + "&2 whitelisted item(s) &7&o[" + (end - start) + "ms]&2.");
            utils.sendMessage(sender, "&2Listeners activated: &e" + listener.getActivated());
            utils.sendMessage(sender, "&2Meta items loaded: &e" + banDatabase.getMetaItems().size());
            utils.sendMessage(sender, "&2Custom items loaded: &e" + banDatabase.getCustomItems().size());
        } else
            utils.sendMessage(sender, "&2Successfully loaded &e" + banDatabase.getBlacklist().getTotal() + "&2 blacklisted & &e" + banDatabase.getWhitelist().getTotal() + "&2 whitelisted item(s).");
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, final @NotNull String label, @NotNull final String[] args) {
        // Plugin not loaded yet?
        if (banConfig == null) {
            sender.sendMessage(Chat.color("&c&l[&e&lBanItem&c&l] &cThe plugin is not loaded yet. Please wait before using the command."));
            return true;
        }

        if (args.length > 0)
            try {
                final String subCommandName = args[0].toLowerCase();
                final String subCommand = utils.getCommandsAliases().getOrDefault(subCommandName, subCommandName);
                final BanCommand banCommand = (BanCommand) Class.forName("fr.andross.banitem.commands.Command" + subCommand)
                        .getDeclaredConstructor(BanItem.class, CommandSender.class, String[].class)
                        .newInstance(this, sender, args);
                banCommand.run();
                return true;
            } catch (final Exception ignored) {
                // We do not care!
            }

        // Trying to show help?
        if (!sender.hasPermission("banitem.command.help")) {
            final String message = getConfig().getString("no-permission");
            if (message != null) utils.sendMessage(sender, message);
            return true;
        }

        // Help messages
        if (sender instanceof Player) {
            utils.sendMessage(sender, "&7&m     &r &l[&7&lUsage - &e&lv" + getDescription().getVersion() + "&r&l] &7&m     ");
            utils.sendMessage(sender, " &7- /bi &3add&7: add an item in blacklist for current world.");
            utils.sendMessage(sender, " &7- /bi &3check&7: check if any player has a blacklisted item.");
            utils.sendMessage(sender, " &7- /bi &3help&7: gives additional informations.");
            utils.sendMessage(sender, " &7- /bi &3info&7: get info about your item in hand.");
            utils.sendMessage(sender, " &7- /bi &3load&7: load a specific config file.");
            utils.sendMessage(sender, " &7- /bi &3log&7: activate the log mode.");
            utils.sendMessage(sender, " &7- /bi &3metaitem&7: add/remove/list meta items.");
            utils.sendMessage(sender, " &7- /bi &3reload&7: reload the config.");
            utils.sendMessage(sender, " &7- /bi &3remove&7: remove and unban the item if banned.");
        } else {
            utils.sendMessage(sender, "&7&m     &r &l[&7&lConsole Usage - &e&lv" + getDescription().getVersion() + "&r&l] &7&m     ");
            utils.sendMessage(sender, " &7- /bi &3add&7: add an item in blacklist for current world.");
            utils.sendMessage(sender, " &7- /bi &3check&7: check if any player has a blacklisted item.");
            utils.sendMessage(sender, " &7- /bi &3help&7: gives additional informations.");
            utils.sendMessage(sender, " &7- /bi &3load&7: load a specific config file.");
            utils.sendMessage(sender, " &7- /bi &3metaitem&7: add/remove/list meta items.");
            utils.sendMessage(sender, " &7- /bi &3reload&7: reload the config.");
            utils.sendMessage(sender, " &7- /bi &3remove&7: remove and unban the item if banned.");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String alias, @NotNull final String[] args) {
        // Has permission?
        if (!sender.hasPermission("banitem.command.help")) return Collections.emptyList();

        // Sub command
        if (args.length == 1) return StringUtil.copyPartialMatches(args[0], Arrays.asList("add", "check", "help", "info", "load", "log", "metaitem", "reload", "remove"), new ArrayList<>());

        // Running subcommand
        try {
            final String subCommandName = args[0].toLowerCase();
            final String subCommand = utils.getCommandsAliases().getOrDefault(subCommandName, subCommandName);
            final BanCommand banCommand = (BanCommand) Class.forName("fr.andross.banitem.commands.Command" + subCommand)
                    .getDeclaredConstructor(BanItem.class, CommandSender.class, String[].class)
                    .newInstance(this, sender, args);
            return banCommand.runTab();
        } catch (final Exception e) {
            return Collections.emptyList();
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
     * Get a the ban config helper
     * @return the ban config helper
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
     * Get the class that handle the listeners
     * @return the class that handle the listeners
     */
    @NotNull
    public BanListener getListener() {
        return listener;
    }
}
