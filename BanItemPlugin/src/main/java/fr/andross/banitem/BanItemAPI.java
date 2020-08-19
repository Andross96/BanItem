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

import fr.andross.banitem.database.Blacklist;
import fr.andross.banitem.database.CustomItems;
import fr.andross.banitem.database.Whitelist;
import fr.andross.banitem.database.WhitelistedWorld;
import fr.andross.banitem.options.BanData;
import fr.andross.banitem.options.BanOption;
import fr.andross.banitem.options.BanOptionData;
import fr.andross.banitem.utils.item.BannedItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * BanItemAPI
 * <p><b><u>IMPORTANT:</u></b></p>
 * <p>This api is loaded on next tick after server load, to allow other custom worlds plugins to load worlds.
 * So this API is <u>not</u> available on server load, but will be on next available tick!</p>
 * <p>If you add/remove any option from a map <i>(blacklist/whitelist)</i>, you have to reload the plugin listeners
 * so it can handle correctly the options, using {@link BanListener#load(CommandSender)} ()}</p>
 * @author Andross
 * @version 2.4
 */
public class BanItemAPI {
    private static BanItemAPI instance;
    private final BanItem pl;

    /**
     * This should not be instantiate.
     * @param pl ban item plugin
     */
    BanItemAPI(@NotNull final BanItem pl) {
        instance = this;
        this.pl = pl;
    }

    /**
     * Get a static instance of the api.
     * You should use {@link BanItem#getApi()} instead.
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
     * Get the BanItem database, containing <b>blacklist</b>, <b>whitelist</b> and <b>custom items</b>.
     * @return the BanDatabase object
     */
    @NotNull
    public BanDatabase getDatabase() {
        return pl.getBanDatabase();
    }

    /**
     * (re)Loading the plugin with this configuration file
     * @param sender command sender <i>(send the message debug to)</i>
     * @param config the file configuration to load. If null, using (and reloading) the default config
     */
    public void load(@NotNull final CommandSender sender, @Nullable FileConfiguration config) {
        pl.load(sender, config);
    }

    /**
     * Check if the item is banned, in both blacklist and whitelist, sending a message to the player if it's the case.
     * This also consider the player bypass permissions.
     * @param player the {@link org.bukkit.entity.Player} involved into this action
     * @param location the effective location where the action occurs
     * @param material the {@link Material} involved into this action
     * @param option the {@link BanOption} option to check
     * @param data optional ban datas
     * @return true if this item is banned for those characteristics, otherwise false
     */
    public boolean isBanned(@NotNull final Player player, @NotNull final Location location, @NotNull final Material material, @NotNull final BanOption option, @Nullable final BanData... data) {
        return isBanned(player, location, new BannedItem(material), true, option, data);
    }

    /**
     * Check if the item is banned, in both blacklist and whitelist, sending a message to the player if it's the case.
     * This also consider the player bypass permissions.
     * @param player the {@link org.bukkit.entity.Player} involved into this action
     * @param location the effective location where the action occurs
     * @param item the {@link org.bukkit.inventory.ItemStack} involved into this action
     * @param option the {@link BanOption} option to check
     * @param data optional ban datas
     * @return true if this item is banned for those characteristics, otherwise false
     */
    public boolean isBanned(@NotNull final Player player, @NotNull final Location location, @NotNull final ItemStack item, @NotNull final BanOption option, @Nullable final BanData... data) {
        return isBanned(player, location, new BannedItem(item), true, option, data);
    }

    /**
     * Check if the item is banned, in both blacklist and whitelist, sending a message to the player if it's the case.
     * This also consider the player bypass permissions.
     * @param player the {@link org.bukkit.entity.Player} involved into this action
     * @param location the effective location where the action occurs
     * @param item the {@link BannedItem} involved into this action
     * @param option the {@link BanOption} option to check
     * @param data optional ban datas
     * @return true if this item is banned for those characteristics, otherwise false
     */
    public boolean isBanned(@NotNull final Player player, @NotNull final Location location, @NotNull final BannedItem item, @NotNull final BanOption option, @Nullable final BanData... data) {
        return isBanned(player, location, item, true, option, data);
    }

    /**
     * Check if the item is banned, in both blacklist and whitelist, sending a message or not to the player.
     * This also consider the player bypass permissions.
     * @param player the {@link org.bukkit.entity.Player} involved into this action
     * @param location the effective location where the action occurs
     * @param item the {@link org.bukkit.inventory.ItemStack} involved into this action
     * @param sendMessage if the item is banned, send the ban message configured
     * @param option the {@link BanOption} option to check
     * @param data optional ban datas
     * @return true if this item is banned for those characteristics, otherwise false
     */
    public boolean isBanned(@NotNull final Player player, @NotNull final Location location, @NotNull final ItemStack item, final boolean sendMessage, @NotNull final BanOption option, @Nullable final BanData... data) {
        return isBanned(player, location, new BannedItem(item), sendMessage, option, data);
    }

    /**
     * Check if the item is banned, in both blacklist and whitelist, sending a message or not to the player.
     * This also consider the player bypass permissions.
     * @param player the {@link org.bukkit.entity.Player} involved into this action
     * @param location the effective location where the action occurs
     * @param item the {@link BannedItem} involved into this action
     * @param sendMessage if the item is banned, send the ban message configured
     * @param option the {@link BanOption} option to check
     * @param data optional ban datas
     * @return true if this item is banned for those characteristics, otherwise false
     */
    public boolean isBanned(@NotNull final Player player, @NotNull final Location location, @NotNull final BannedItem item, final boolean sendMessage, @NotNull final BanOption option, @Nullable final BanData... data) {
        // Checking permission bypass
        if (pl.getUtils().hasPermission(player, item.getType().name().toLowerCase(), pl.getBanDatabase().getCustomItems().getName(item), option, data)) return false;
        // Checking blacklist?
        if (pl.getBanDatabase().getBlacklist().isBlacklisted(player, location, item, sendMessage, option, data)) return true;
        // Checking whitelist?
        return !pl.getBanDatabase().getWhitelist().isWhitelisted(player, location, item, sendMessage, option, data);
    }

    /**
     * This method is used to check if the item is banned, in both blacklist and whitelist, not involving a player
     * This method is mainly used to check dispensers <i>dispense</i> and hoppers <i>transfer</i>
     * @param world bukkit world <i>({@link World})</i>
     * @param material the involved material
     * @param option the ban option to check
     * @param data the ban option datas to check
     * @return true if the item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final World world, @NotNull final Material material, @NotNull final BanOption option, @Nullable final BanData... data) {
        return isBanned(world, new BannedItem(material), option, data);
    }

    /**
     * This method is used to check if the item is banned, in both blacklist and whitelist, not involving a player
     * This method is mainly used to check dispensers <i>dispense</i> and hoppers <i>transfer</i>
     * @param world bukkit world <i>({@link World})</i>
     * @param item the involved ItemStack
     * @param option the ban option to check
     * @param data the ban option datas to check
     * @return true if the item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final World world, @NotNull final ItemStack item, @NotNull final BanOption option, @Nullable final BanData... data) {
        return isBanned(world, new BannedItem(item), option, data);
    }

    /**
     * This method is used to check if the item is banned, in both blacklist and whitelist, not involving a player
     * This method is mainly used to check dispensers <i>dispense</i> and hoppers <i>transfer</i>
     * @param world bukkit world <i>({@link World})</i>
     * @param item the involved item
     * @param option the ban option to check
     * @param data the ban option datas to check
     * @return true if the item is banned, otherwise false
     */
    public boolean isBanned(@NotNull final World world, @NotNull final BannedItem item, @NotNull final BanOption option, @Nullable final BanData... data) {
        // Checking blacklist?
        if (pl.getBanDatabase().getBlacklist().isBlacklisted(world, item, option, data)) return true;
        // Checking whitelist?
        return !pl.getBanDatabase().getWhitelist().isWhitelisted(world, item, option, data);
    }

    /**
     * Check if the item is blacklisted, sending a message or not to the player.
     * @param player the {@link org.bukkit.entity.Player} involved into this action
     * @param location the effective location where the action occurs
     * @param item the {@link BannedItem} involved into this action
     * @param sendMessage if the item is banned, send the ban message configured
     * @param option the {@link BanOption} option to check
     * @param data optional ban datas
     * @return true if this item is banned for those characteristics, otherwise false
     */
    public boolean isBlacklisted(@NotNull final Player player, @NotNull final Location location, @NotNull final BannedItem item, final boolean sendMessage, @NotNull final BanOption option, @Nullable final BanData... data) {
        // Checking permission bypass
        if (pl.getUtils().hasPermission(player, item.getType().name().toLowerCase(), pl.getBanDatabase().getCustomItems().getName(item), option)) return false;
        // Checking blacklist
        return pl.getBanDatabase().getBlacklist().isBlacklisted(player, location, item, sendMessage, option, data);
    }

    /**
     * Check if the item is blacklisted, not involving a player
     * @param world bukkit world <i>({@link World})</i>
     * @param item the involved item
     * @param option the ban option to check
     * @param data the ban option datas to check
     * @return true if the item is banned, otherwise false
     */
    public boolean isBlacklisted(@NotNull final World world, @NotNull final BannedItem item, @NotNull final BanOption option, @Nullable final BanData... data) {
        // Checking blacklist?
        return pl.getBanDatabase().getBlacklist().isBlacklisted(world, item, option, data);
    }

    /**
     * Check if the item is whitelisted <i>(allowed)</i>, sending a message or not to the player.
     * @param player the {@link org.bukkit.entity.Player} involved into this action
     * @param location the effective location where the action occurs
     * @param item the {@link BannedItem} involved into this action
     * @param sendMessage if the item is banned, send the ban message configured
     * @param option the {@link BanOption} option to check
     * @param data optional ban datas
     * @return true if this item is allowed, otherwise false
     */
    public boolean isWhitelisted(@NotNull final Player player, @NotNull final Location location, @NotNull final BannedItem item, final boolean sendMessage, @NotNull final BanOption option, @Nullable final BanData... data) {
        // Checking permission bypass
        if (pl.getUtils().hasPermission(player, item.getType().name().toLowerCase(), pl.getBanDatabase().getCustomItems().getName(item), option, data)) return true;
        // Checking blacklist
        return pl.getBanDatabase().getWhitelist().isWhitelisted(player, location, item, sendMessage, option, data);
    }

    /**
     * Check if the item is whitelisted, not involving a player
     * @param world bukkit world <i>({@link World})</i>
     * @param item the involved item
     * @param option the ban option to check
     * @param data the ban option datas to check
     * @return true if the item is allowed, otherwise false
     */
    public boolean isWhitelisted(@NotNull final World world, @NotNull final BannedItem item, @NotNull final BanOption option, @Nullable final BanData... data) {
        // Checking whitelist?
        return pl.getBanDatabase().getWhitelist().isWhitelisted(world, item, option, data);
    }


    /*------------------------------
     * **********************
     *       BLACKLIST
     * **********************
    ------------------------------*/

    /**
     * @return the blacklisted map
     */
    @NotNull
    public Blacklist getBlacklist() {
        return getDatabase().getBlacklist();
    }

    /**
     * Add a new banned item to blacklisted worlds and save the config.yml file. <i>(comments in file may be removed)</i>
     *
     * @param item the item to ban
     * @param options a map containing options and their respective data
     * @param worlds worlds where the ban apply, all worlds if null
     */
    public void addToBlacklist(@NotNull final BannedItem item, @NotNull final Map<BanOption, BanOptionData> options, @Nullable final World... worlds) {
        // Preparing variables
        final List<World> appliedWorlds = worlds == null ? Bukkit.getWorlds() : Arrays.asList(worlds);
        // Getting the name of the item
        final CustomItems items = getDatabase().getCustomItems();
        final String name = items.getReversed().containsKey(item) ? items.getReversed().get(item) : item.getType().name().toLowerCase();

        for (final World world : appliedWorlds) {
            // Adding in map
            getDatabase().getBlacklist().addNewBan(world, item, options);
            // Adding in file
            pl.getConfig().set("blacklist." + world.getName() + "." + name, null);
            for (Map.Entry<BanOption, BanOptionData> entry : options.entrySet()) {
                pl.getConfig().set("blacklist." + world.getName() + "." + name + "." + entry.getKey().name().toLowerCase(), entry.getValue().serialize());
            }
        }
        pl.saveConfig();
    }

    /**
     * Remove the banned item from blacklisted worlds and save the config.yml file. <i>(comments in file may be removed)</i>
     *
     * @param item the banned item to remove
     * @param worlds worlds where the banned item can be present, all worlds if null
     */
    public void removeFromBlacklist(@NotNull final BannedItem item, @Nullable final World... worlds) {
        // Preparing variables
        final List<World> appliedWorlds = worlds == null ? Bukkit.getWorlds() : Arrays.asList(worlds);
        // Getting the name of the item
        final CustomItems items = getDatabase().getCustomItems();
        final String name = items.getReversed().containsKey(item) ? items.getReversed().get(item) : item.getType().name().toLowerCase();

        for (final World world : appliedWorlds) {
            // Removing from map
            final Map<BannedItem, Map<BanOption, BanOptionData>> map = getDatabase().getBlacklist().get(world);
            if (map == null) continue; // nothing in this world
            map.remove(item);
            // Removing from file
            pl.getConfig().set("blacklist." + world.getName() + "." + name, null);
        }
        pl.saveConfig();
    }

    /*------------------------------
     * **********************
     *       WHITELIST
     * **********************
    ------------------------------*/

    /**
     * @return the whitelist map
     */
    @NotNull
    public Whitelist getWhitelist() {
        return getDatabase().getWhitelist();
    }

    /**
     * Add an item on the whitelist of a world and save in config
     *
     * @param ww the whitelistedworld object, recoverable from {@link Whitelist}
     * @param item the item to add
     * @param options map of ban options and their respective data
     */
    public void addToWhitelist(@NotNull final WhitelistedWorld ww, @NotNull final BannedItem item, @NotNull final Map<BanOption, BanOptionData> options) {
        // Adding in map
        getWhitelist().addNewException(ww, item, options);

        // Adding in config
        // Getting the name of the item
        final CustomItems items = getDatabase().getCustomItems();
        final String name = items.getReversed().containsKey(item) ? items.getReversed().get(item) : item.getType().name().toLowerCase();

        final ConfigurationSection section = pl.getConfig().createSection("whitelist." + ww.getWorld().getName() + "." + name);
        for (Map.Entry<BanOption, BanOptionData> entry : options.entrySet()) {
            section.set(entry.getKey().getName(), entry.getValue().serialize());
        }
        pl.getConfig().set("whitelist." + ww.getWorld().getName() + "." + name, section);
        pl.saveConfig();
    }

    /**
     * Remove the item from the whitelist and save in config.yml <i>(comments in file may be removed)</i>
     *
     * @param ww the whitelisted world
     * @param item the item
     */
    public void removeFromWhitelist(@NotNull final WhitelistedWorld ww, @NotNull final BannedItem item) {
        // Removing from map
        ww.remove(item);

        // Removing from config
        // Getting the name of the item
        final CustomItems items = getDatabase().getCustomItems();
        final String name = items.getReversed().containsKey(item) ? items.getReversed().get(item) : item.getType().name().toLowerCase();
        pl.getConfig().set("whitelist." + ww.getWorld().getName() + "." + name, null);
        pl.saveConfig();
    }


    /*------------------------------
     * **********************
     *     CUSTOM ITEMS
     * **********************
    ------------------------------*/

    /**
     * @return the custom items map
     */
    @NotNull
    public CustomItems getCustomItems() {
        return getDatabase().getCustomItems();
    }

    /**
     * Get a cloned ItemStack for the custom item named <b>customName</b>.
     *
     * @param customName the name of the custom item
     * @return a cloned ItemStack of the custom item if exists, otherwise null
     */
    @Nullable
    public ItemStack getCustomItem(@NotNull final String customName) {
        final BannedItem bi = getDatabase().getCustomItems().get(customName);
        return bi == null ? null : bi.toItemStack();
    }

    /**
     * Try to get the custom item name of the given <b>item</b>.
     *
     * @param item the ItemStack
     * @return the name of saved the custom item if exists, otherwise null
     */
    @Nullable
    public String getCustomItemName(@NotNull final ItemStack item) {
        return getCustomItemName(new BannedItem(item));
    }

    /**
     * Try to get the custom item name of the given <b>item</b>.
     *
     * @param item the item
     * @return the name of saved the custom item if exists, otherwise null
     */
    @Nullable
    public String getCustomItemName(@NotNull final BannedItem item) {
        return getCustomItems().getName(item);
    }

    /**
     * Add an ItemStack as a custom item and save it in items.yml
     * <p>
     * <b>Will replace existing value</b>
     *
     * @param name the name of the custom ItemStack
     * @param item the custom ItemStack
     * @throws Exception if any error occurs (any null value or unable to save the items.yml file)
     */
    public void addCustomItem(@NotNull final String name, @NotNull final ItemStack item) throws Exception {
        getDatabase().addCustomItem(name, item);
    }

    /**
     * Remove the custom ItemStack named <b>name</b>
     *
     * @param name the name of the custom ItemStack
     * @throws Exception if any error occurs (any null value or unable to save the items.yml file)
     */
    public void removeCustomItem(@NotNull final String name) throws Exception {
        getDatabase().removeCustomItem(name);
    }
}
