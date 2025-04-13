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
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.actions.BanData;
import fr.andross.banitem.database.Blacklist;
import fr.andross.banitem.database.Whitelist;
import fr.andross.banitem.database.WhitelistedWorld;
import fr.andross.banitem.database.items.CustomItems;
import fr.andross.banitem.database.items.Items;
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BanItemAPI
 * <p><b><u>IMPORTANT:</u></b></p>
 * <p>This api is loaded on next tick after server load, to allow other custom worlds plugins to load worlds.
 * So this API is <u>not</u> available on server load, but will be on next available tick!</p>
 * <p>If you add/remove any action from a map <i>(blacklist/whitelist)</i>, you have to reload the plugin listeners
 * so it can handle correctly the actions, using {@link BanListener#load(CommandSender)} ()}</p>
 *
 * @author Andross
 * @version 3.3
 */
public final class BanItemAPI {
    private static BanItemAPI instance;
    private final BanItem plugin;

    /**
     * This should not be instantiated.
     *
     * @param plugin ban item plugin
     */
    BanItemAPI(@NotNull final BanItem plugin) {
        instance = this;
        this.plugin = plugin;
    }

    /**
     * Get a static instance of the api.
     * You should use {@link BanItem#getApi()} instead.
     *
     * @return a static instance of the api
     */
    @NotNull
    public static BanItemAPI getInstance() {
        return instance;
    }

    /*------------------------------
     * **********************
     *       GENERAL
     * **********************
    ------------------------------*/

    /**
     * Get the BanItem database, containing <b>blacklist</b>, <b>whitelist</b>,
     * <b>custom items</b> and <b>meta items</b>.
     *
     * @return the BanDatabase object
     */
    @NotNull
    public BanDatabase getDatabase() {
        return plugin.getBanDatabase();
    }

    /**
     * (re)Loading the plugin with this configuration file
     *
     * @param sender     command sender <i>(send the message debug to)</i>
     * @param configFile the file configuration to load. If null, using (and reloading) the default config
     */
    public void load(@NotNull final CommandSender sender, @Nullable final File configFile) {
        plugin.load(sender, configFile);
    }

    /*------------------------------
     * **********************
     *     MATERIAL CHECK
     * **********************
    ------------------------------*/

    /**
     * Check if the material is banned, in both blacklist and whitelist.
     * This also consider the player bypass permissions.
     *
     * @param player the {@link Player} involved into this action
     * @param m      the {@link Material} used
     * @param action the {@link BanAction} to check
     * @param data   optional action data, leave it blank if not needed for the action
     * @return true if this item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final Player player,
                            @NotNull final Material m,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(player, null, new BannedItem(m), false, action, data);
    }

    /**
     * Check if the material is banned, in both blacklist and whitelist.
     * This also consider the player bypass permissions.
     *
     * @param player the {@link Player} involved into this action
     * @param loc    the effective {@link Location} of the action
     * @param m      the {@link Material} used
     * @param action the {@link BanAction} to check
     * @param data   optional action data, leave it blank if not needed for the action
     * @return true if this item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final Player player,
                            @Nullable final Location loc,
                            @NotNull final Material m,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(player, loc, new BannedItem(m), false, action, data);
    }

    /**
     * Check if the material is banned, in both blacklist and whitelist.
     * This also consider the player bypass permissions.
     *
     * @param player      the {@link Player} involved into this action
     * @param m           the {@link Material} used
     * @param sendMessage if the banned message should be sent to the player
     * @param action      the {@link BanAction} to check
     * @param data        optional action data, leave it blank if not needed for the action
     * @return true if this item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final Player player,
                            @NotNull final Material m,
                            final boolean sendMessage,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(player, null, new BannedItem(m), sendMessage, action, data);
    }

    /**
     * Check if the material is banned, in both blacklist and whitelist.
     * This also consider the player bypass permissions.
     *
     * @param player      the {@link Player} involved into this action
     * @param loc         the effective {@link Location} of the action
     * @param m           the {@link Material} used
     * @param sendMessage if the banned message should be sent to the player
     * @param action      the {@link BanAction} to check
     * @param data        optional action data, leave it blank if not needed for the action
     * @return true if this item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final Player player,
                            @Nullable final Location loc,
                            @NotNull final Material m,
                            final boolean sendMessage,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(player, loc, new BannedItem(m), sendMessage, action, data);
    }


    /*------------------------------
     * **********************
     *     ITEMSTACK CHECK
     * **********************
    ------------------------------*/

    /**
     * Check if the ItemStack is banned, in both blacklist and whitelist.
     * This also consider the player bypass permissions.
     *
     * @param player the {@link Player} involved into this action
     * @param item   the {@link ItemStack} used
     * @param action the {@link BanAction} to check
     * @param data   optional action data, leave it blank if not needed for the action
     * @return true if this item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final Player player,
                            @NotNull final ItemStack item,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(player, null, new BannedItem(item), false, action, data);
    }

    /**
     * Check if the ItemStack is banned, in both blacklist and whitelist.
     * This also consider the player bypass permissions.
     *
     * @param player the {@link Player} involved into this action
     * @param loc    the effective {@link Location} of the action
     * @param item   the {@link ItemStack} used
     * @param action the {@link BanAction} to check
     * @param data   optional action data, leave it blank if not needed for the action
     * @return true if this item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final Player player,
                            @Nullable final Location loc,
                            @NotNull final ItemStack item,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(player, loc, new BannedItem(item), false, action, data);
    }

    /**
     * Check if the ItemStack is banned, in both blacklist and whitelist.
     * This also consider the player bypass permissions.
     *
     * @param player      the {@link Player} involved into this action
     * @param item        the {@link ItemStack} used
     * @param sendMessage if the banned message should be sent to the player
     * @param action      the {@link BanAction} to check
     * @param data        optional action data, leave it blank if not needed for the action
     * @return true if this item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final Player player,
                            @NotNull final ItemStack item,
                            final boolean sendMessage,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(player, null, new BannedItem(item), sendMessage, action, data);
    }

    /**
     * Check if the ItemStack is banned, in both blacklist and whitelist.
     * This also consider the player bypass permissions.
     *
     * @param player      the {@link Player} involved into this action
     * @param loc         the effective {@link Location} of the action
     * @param item        the {@link ItemStack} used
     * @param sendMessage if the banned message should be sent to the player
     * @param action      the {@link BanAction} to check
     * @param data        optional action data, leave it blank if not needed for the action
     * @return true if this item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final Player player,
                            @Nullable final Location loc,
                            @NotNull final ItemStack item,
                            final boolean sendMessage,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(player, loc, new BannedItem(item), sendMessage, action, data);
    }


    /*------------------------------
     * **********************
     *     BANNEDITEM CHECK
     * **********************
    ------------------------------*/

    /**
     * Check if the BannedItem object is banned, in both blacklist and whitelist.
     * This also consider the player bypass permissions.
     *
     * @param player the {@link Player} involved into this action
     * @param item   the {@link BannedItem} object
     * @param action the {@link BanAction} to check
     * @param data   optional action data, leave it blank if not needed for the action
     * @return true if this item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final Player player,
                            @NotNull final BannedItem item,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(player, null, item, false, action, data);
    }

    /**
     * Check if the BannedItem object is banned, in both blacklist and whitelist.
     * This also consider the player bypass permissions.
     *
     * @param player the {@link Player} involved into this action
     * @param loc    the effective {@link Location} of the action
     * @param item   the {@link BannedItem} object
     * @param action the {@link BanAction} to check
     * @param data   optional action data, leave it blank if not needed for the action
     * @return true if this item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final Player player,
                            @Nullable final Location loc,
                            @NotNull final BannedItem item,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(player, loc, item, false, action, data);
    }

    /**
     * Check if the BannedItem object is banned, in both blacklist and whitelist.
     * This also consider the player bypass permissions.
     *
     * @param player      the {@link Player} involved into this action
     * @param item        the {@link BannedItem} object
     * @param sendMessage if the banned message should be sent to the player
     * @param action      the {@link BanAction} to check
     * @param data        optional action data, leave it blank if not needed for the action
     * @return true if this item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final Player player,
                            @NotNull final BannedItem item,
                            final boolean sendMessage,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(player, null, item, sendMessage, action, data);
    }

    /**
     * Check if the BannedItem object is banned, in both blacklist and whitelist.
     * This also consider the player bypass permissions.
     *
     * @param player      the {@link Player} involved into this action
     * @param loc         the effective {@link Location} of the action
     * @param item        the {@link BannedItem} object
     * @param sendMessage if the banned message should be sent to the player
     * @param action      the {@link BanAction} to check
     * @param data        the optional ban action data, leave it blank if not needed for the action
     * @return true if this item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final Player player,
                            @Nullable final Location loc,
                            @NotNull final BannedItem item,
                            final boolean sendMessage,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        if (plugin.getBanDatabase().getBlacklist().isBlacklisted(player, loc, item, sendMessage, action, data)) {
            return true;
        }
        return !plugin.getBanDatabase().getWhitelist().isWhitelisted(player, loc, item, sendMessage, action, data);
    }

    /**
     * This method is used to check if the item is banned, in both blacklist and whitelist, not involving a player.
     * This method is mainly used to check dispensers <i>dispense</i> and hoppers <i>transfer</i>.
     *
     * @param world    bukkit world <i>({@link World})</i>
     * @param material the involved material
     * @param action   the ban action to check
     * @param data     the optional ban action data
     * @return true if the item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final World world,
                            @NotNull final Material material,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(world, new BannedItem(material), action, data);
    }

    /**
     * This method is used to check if the item is banned, in both blacklist and whitelist, not involving a player.
     * This method is mainly used to check dispensers <i>dispense</i> and hoppers <i>transfer</i>.
     *
     * @param world  bukkit world <i>({@link World})</i>
     * @param item   the involved ItemStack
     * @param action the ban action to check
     * @param data   the optional ban action data
     * @return true if the item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final World world,
                            @NotNull final ItemStack item,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        return isBanned(world, new BannedItem(item), action, data);
    }

    /**
     * This method is used to check if the item is banned, in both blacklist and whitelist, not involving a player.
     * This method is mainly used to check dispensers <i>dispense</i> and hoppers <i>transfer</i>.
     *
     * @param world  bukkit world <i>({@link World})</i>
     * @param item   the involved item
     * @param action the ban action to check
     * @param data   the optional ban action data
     * @return true if the item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final World world,
                            @NotNull final BannedItem item,
                            @NotNull final BanAction action,
                            @Nullable final BanData... data) {
        if (plugin.getBanDatabase().getBlacklist().isBlacklisted(world, item, action, data)) {
            return true;
        }
        return !plugin.getBanDatabase().getWhitelist().isWhitelisted(world, item, action, data);
    }

    /**
     * Check if the item is blacklisted. If sendMessage is on true, the ban message will be sent
     * to the player.
     *
     * @param player      the {@link Player} involved into this action
     * @param location    the effective location where the action occurs
     * @param item        the {@link BannedItem} involved into this action
     * @param sendMessage if the item is banned, send the ban message configured
     * @param action      the {@link BanAction} action to check
     * @param data        additional ban data
     * @return true if this item is banned for those characteristics, otherwise false
     */
    public boolean isBlacklisted(@NotNull final Player player,
                                 @NotNull final Location location,
                                 @NotNull final BannedItem item,
                                 final boolean sendMessage,
                                 @NotNull final BanAction action,
                                 @Nullable final BanData... data) {
        return plugin.getBanDatabase().getBlacklist().isBlacklisted(player, location, item, sendMessage, action, data);
    }

    /**
     * Check if the item is blacklisted, not involving a player.
     *
     * @param world  bukkit world <i>({@link World})</i>
     * @param item   the involved item
     * @param action the ban action to check
     * @param data   the optional ban action data
     * @return true if the item is banned, otherwise false
     */
    public boolean isBlacklisted(@NotNull final World world,
                                 @NotNull final BannedItem item,
                                 @NotNull final BanAction action,
                                 @Nullable final BanData... data) {
        return plugin.getBanDatabase().getBlacklist().isBlacklisted(world, item, action, data);
    }

    /**
     * Check if the item is whitelisted <i>(allowed)</i>, sending a message or not to the player.
     *
     * @param player      the {@link Player} involved into this action
     * @param location    the effective location where the action occurs
     * @param item        the {@link BannedItem} involved into this action
     * @param sendMessage if the item is banned, send the ban message configured
     * @param action      the {@link BanAction} action to check
     * @param data        the optional ban action data
     * @return true if this item is allowed, otherwise false
     */
    public boolean isWhitelisted(@NotNull final Player player,
                                 @NotNull final Location location,
                                 @NotNull final BannedItem item,
                                 final boolean sendMessage,
                                 @NotNull final BanAction action,
                                 @Nullable final BanData... data) {
        return plugin.getBanDatabase().getWhitelist().isWhitelisted(player, location, item, sendMessage, action, data);
    }

    /**
     * Check if the item is whitelisted, not involving a player.
     *
     * @param world  bukkit world <i>({@link World})</i>
     * @param item   the involved item
     * @param action the ban action to check
     * @param data   the optional ban action data
     * @return true if the item is allowed, otherwise false
     */
    public boolean isWhitelisted(@NotNull final World world,
                                 @NotNull final BannedItem item,
                                 @NotNull final BanAction action,
                                 @Nullable final BanData... data) {
        return plugin.getBanDatabase().getWhitelist().isWhitelisted(world, item, action, data);
    }


    /*------------------------------
     * **********************
     *       BLACKLIST
     * **********************
    ------------------------------*/

    /**
     * Blacklist handler which contains all blacklisted items.
     *
     * @return blacklist handler which contains all blacklisted items.
     */
    @NotNull
    public Blacklist getBlacklist() {
        return getDatabase().getBlacklist();
    }

    /**
     * Add a new banned item to blacklist and save the config file used.
     *
     * @param item    the item to ban
     * @param actions a map containing actions and their respective data
     * @param worlds  worlds where the ban apply. If null, including all worlds
     * @return true if the item is successfully added, otherwise false
     */
    public boolean addToBlacklist(@NotNull final BannedItem item,
                                  @NotNull final Map<BanAction, BanActionData> actions,
                                  @Nullable final World... worlds) {
        return addToBlacklist(Collections.singletonList(item), actions, worlds);
    }

    /**
     * Add the banned items to the blacklist and save the config file used.
     *
     * @param items   the items to ban
     * @param actions a map containing actions and their respective data
     * @param worlds  worlds where the ban apply. If null, including all worlds
     * @return true if the item is successfully added, otherwise false
     */
    public boolean addToBlacklist(@NotNull final Collection<? extends BannedItem> items,
                                  @NotNull final Map<BanAction, BanActionData> actions,
                                  @Nullable final World... worlds) {
        // Preparing variables
        final boolean allWorlds = Utils.isNullOrEmpty(worlds);
        final List<World> appliedWorlds = allWorlds ? Bukkit.getWorlds() : Arrays.asList(worlds);

        for (final BannedItem item : items) {
            final String name = plugin.getBanDatabase().getName(item);

            // Adding in map
            appliedWorlds.forEach(w -> getDatabase().getBlacklist().addNewBan(w, item, actions));

            // Adding in config
            if (allWorlds) {
                // If all data are equals, we can unify them
                if (Utils.areAllEquals(actions.values())) {
                    final Map<String, Object> serializedData = actions.values().iterator().next().serialize();
                    final String actionNames = actions.size() == BanAction.values().length ? "*" : actions.keySet().stream().map(BanAction::getName).map(String::toLowerCase).collect(Collectors.joining(","));
                    plugin.getBanConfig().getConfig().set("blacklist.*." + name + "." + actionNames, serializedData);
                } else {
                    actions.forEach((k, v) -> plugin.getConfig().set("blacklist.*." + name + "." + k.name().toLowerCase(), v.serialize()));
                }
            } else {
                // If all data are equals, we can unify them
                if (Utils.areAllEquals(actions.values())) {
                    final Map<String, Object> serializedData = actions.values().iterator().next().serialize();
                    final String actionNames = actions.size() == BanAction.values().length ? "*" : actions.keySet().stream().map(BanAction::getName).map(String::toLowerCase).collect(Collectors.joining(","));
                    for (final World world : appliedWorlds) {
                        plugin.getBanConfig().getConfig().set("blacklist." + world.getName() + "." + name + "." + actionNames, serializedData);
                    }
                } else {
                    for (final World world : appliedWorlds) {
                        plugin.getBanConfig().getConfig().set("blacklist." + world.getName() + "." + name, null);
                        actions.forEach((k, v) -> plugin.getBanConfig().getConfig().set("blacklist." + world.getName() + "." + name + "." + k.name().toLowerCase(), v.serialize()));
                    }
                }
            }
        }

        return plugin.getBanConfig().saveConfig();
    }

    /**
     * Remove the banned item from blacklisted worlds and save the config file used.
     *
     * @param item   the banned item to remove
     * @param worlds worlds where the banned item can be present, all worlds if null
     * @return true if the item is successfully removed, otherwise false
     */
    public boolean removeFromBlacklist(@NotNull final BannedItem item,
                                       @Nullable final World... worlds) {
        return removeFromBlacklist(Collections.singletonList(item), worlds);
    }

    public boolean removeFromBlacklist(@NotNull final Collection<? extends BannedItem> items,
                                       @Nullable final World... worlds) {
        // Preparing variables
        final List<World> appliedWorlds = Utils.isNullOrEmpty(worlds) ? Bukkit.getWorlds() : Arrays.asList(worlds);

        boolean removed = false;
        for (final BannedItem item : items) {
            final BannedItem typeItem = new BannedItem(item.getType());
            final String name = plugin.getBanDatabase().getName(item);
            final String typeName = item.getType().name().toLowerCase();

            for (final World world : appliedWorlds) {
                final Items map = getDatabase().getBlacklist().get(world);
                if (map == null) {
                    continue;
                }
                if (map.getItems().remove(typeItem) != null) {
                    removed = true;
                    plugin.getBanConfig().getConfig().set("blacklist." + world.getName() + "." + typeName, null);
                }
                if (map.getItems().remove(item) != null) {
                    removed = true;
                    plugin.getBanConfig().getConfig().set("blacklist." + world.getName() + "." + name, null);
                }
            }
            if (removed) {
                // Removing from '*' configuration
                plugin.getBanConfig().getConfig().set("blacklist.*." + name, null);
                plugin.getBanConfig().getConfig().set("blacklist.*." + typeName, null);
            }
        }

        if (removed) {
            plugin.getBanConfig().saveConfig();
        }
        return removed;
    }

    /*------------------------------
     * **********************
     *       WHITELIST
     * **********************
    ------------------------------*/

    /**
     * Whitelist handler which contains all whitelisted items.
     *
     * @return whitelist handler which contains all whitelisted items.
     */
    @NotNull
    public Whitelist getWhitelist() {
        return getDatabase().getWhitelist();
    }

    /**
     * Add an item on the whitelist of a world and save in the used config.
     *
     * @param whitelistedWorld the whitelisted world object, recoverable from {@link Whitelist}
     * @param item             the item to add
     * @param actions          map of ban actions and their respective data
     * @return true if successfully added, otherwise false
     */
    public boolean addToWhitelist(@NotNull final WhitelistedWorld whitelistedWorld,
                                  @NotNull final BannedItem item,
                                  @NotNull final Map<BanAction, BanActionData> actions) {
        // Adding in map
        getWhitelist().addNewException(whitelistedWorld, item, actions);

        // Adding in config
        // Getting the name of the item
        final String name = plugin.getBanDatabase().getName(item);

        final ConfigurationSection section = plugin.getBanConfig().getConfig().createSection("whitelist." + whitelistedWorld.getWorld().getName() + "." + name);
        for (final Map.Entry<BanAction, BanActionData> entry : actions.entrySet()) {
            section.set(entry.getKey().getName(), entry.getValue().serialize());
        }
        plugin.getBanConfig().getConfig().set("whitelist." + whitelistedWorld.getWorld().getName() + "." + name, section);
        return plugin.getBanConfig().saveConfig();
    }

    /**
     * Remove the item from the whitelist and save in config.yml <i>(comments in file may be removed)</i>.
     *
     * @param whitelistedWorld the whitelisted world
     * @param item             the item
     * @return true if successfully removed, otherwise false
     */
    public boolean removeFromWhitelist(@NotNull final WhitelistedWorld whitelistedWorld,
                                       @NotNull final BannedItem item) {
        // Removing from map
        if (whitelistedWorld.getItems().remove(item) == null) {
            return true; // Nothing to remove
        }

        // Removing from config
        // Getting the name of the item
        final String name = plugin.getBanDatabase().getName(item);
        plugin.getBanConfig().getConfig().set("whitelist." + whitelistedWorld.getWorld().getName() + "." + name, null);
        return plugin.getBanConfig().saveConfig();
    }


    /*------------------------------
     * **********************
     *     CUSTOM ITEMS
     * **********************
    ------------------------------*/

    /**
     * CustomItems handler which contains all custom items.
     *
     * @return customItems handler which contains all custom items.
     */
    @NotNull
    public CustomItems getCustomItems() {
        return getDatabase().getCustomItems();
    }

    /**
     * Get the configured custom item by its name.
     *
     * @param customItemName the name of the custom item
     * @return a BannedItem object of the custom item if exists, otherwise null
     */
    @Nullable
    public BannedItem getCustomItem(@NotNull final String customItemName) {
        return getDatabase().getCustomItems().get(customItemName);
    }

    /*------------------------------
     * **********************
     *     META ITEMS
     * **********************
    ------------------------------*/

    /**
     * Get the configured meta item by its name.
     *
     * @param metaItemName the name of the meta item
     * @return a BannedItem object of the meta item if exists, otherwise null
     */
    @Nullable
    public BannedItem getMetaItem(@NotNull final String metaItemName) {
        return getDatabase().getMetaItems().get(metaItemName);
    }

    /**
     * Try to get the meta item name of the given <b>item</b>.
     *
     * @param item the ItemStack
     * @return the name of saved the meta item if exists, otherwise null
     */
    @Nullable
    public String getMetaItemName(@NotNull final ItemStack item) {
        return getMetaItemName(new BannedItem(item));
    }

    /**
     * Try to get the meta item name of the given <b>item</b>.
     *
     * @param item the item
     * @return the name of saved the meta item if exists, otherwise null
     */
    @Nullable
    public String getMetaItemName(@NotNull final BannedItem item) {
        return getDatabase().getMetaItems().getKey(item);
    }

    /**
     * Add an ItemStack as a meta item and save it in metaitems.yml.
     * <p>
     * <b>Will replace existing value</b>
     *
     * @param name the name of the custom ItemStack
     * @param item the custom ItemStack
     */
    public void addMetaItem(@NotNull final String name, @NotNull final ItemStack item) {
        getDatabase().addMetaItem(name, item);
    }

    /**
     * Remove the meta ItemStack named <b>name</b>.
     *
     * @param name the name of the custom ItemStack
     */
    public void removeMetaItem(@NotNull final String name) {
        getDatabase().removeMetaItem(name);
    }
}
