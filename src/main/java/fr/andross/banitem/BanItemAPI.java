package fr.andross.banitem;

import fr.andross.banitem.Maps.WhitelistWorld;
import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BannedItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.rmi.activation.UnknownObjectException;
import java.util.List;
import java.util.Map;

/**
 * BanItemAPI
 *
 * @author Andross
 * @version 1.9.4
 */
public class BanItemAPI {
    private final BanItem pl;

    BanItemAPI(final BanItem pl) {
        this.pl = pl;
    }

    /**
     * Get the BanItem database, containing <b>blacklist</b>, <b>whitelist</b> and <b>custom items</b>.
     *
     * @return the BanDatabase object
     */
    @NotNull
    public BanDatabase getDatabase() {
        return pl.getDb();
    }

    /**
     * Reload the plugin configuration files. Any error messages will be sent to the sender.
     *
     * @param sender send messages to
     */
    public void reload(@NotNull org.bukkit.command.CommandSender sender) {
        pl.load(sender);
    }


    /**
     * Get a BanOption enum from string.
     *
     * @param option ban option
     * @return a BanOption constant enum if exists, otherwise null
     */
    @Nullable
    public BanOption getBanOption(@NotNull String option) {
        try {
            return BanOption.valueOf(option.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }


    /*------------------------------
     * **********************
     *       BLACKLIST
     * **********************
    ------------------------------*/
    /**
     * Check if the <b>item</b> is blacklisted into the world <b>worldName</b>.
     * <p>
     * This method will check first for a matching material (ItemStack#getType()), then for a custom item (a kind of ItemStack#isSimilar)
     *
     * @param worldName the name of the blacklisted world
     * @param item the ItemStack to match
     * @return a Map containing options banned with their respective ban message (messages can be null or empty)
     */
    @NotNull
    public Map<BanOption, String> getBlacklist(@NotNull String worldName, @NotNull ItemStack item) {
        return getDatabase().getBlacklist().getBanOptions(worldName, item);
    }

    /**
     * Add a new material to blacklisted worlds and save the config.yml file. <i>(comments in file may be removed)</i>
     *
     * @param material the Material to ban
     * @param options a map containing options and their respective message to ban
     * @param worlds worlds by name where the ban apply
     */
    public void addToBlacklist(@NotNull Material material, @NotNull Map<BanOption, String> options, @NotNull String... worlds) {
        for (String w : worlds) {
            getDatabase().getBlacklist().addNewBan(w, material, options);
            for (Map.Entry<BanOption, String> entry : options.entrySet()) pl.getConfig().set("blacklist." + w + "." + material.name().toLowerCase() + "." + entry.getKey().name().toLowerCase(), entry.getValue());
        }
        pl.saveConfig();
    }

    /**
     * Add a custom item to blacklisted worlds and save the config.yml file. <i>(comments in file may be removed)</i>
     * <p>
     * The custom name can be retrieved with <i>getCustomName(ItemStack)</i>.
     *
     * @param customName the name of the custom item to ban
     * @param options a map containing options and their respective message to ban
     * @param worlds worlds by name where the ban apply
     * @throws UnknownObjectException if the custom name is not found
     */
    public void addToBlacklist(@NotNull String customName, @NotNull Map<BanOption, String> options, @NotNull String... worlds) throws UnknownObjectException {
        final BannedItem bi = getDatabase().getCustomItems().get(customName);
        if (bi == null) throw new UnknownObjectException("Unknown custom item named '" + customName + "'.");
        for (String w : worlds) {
            getDatabase().getBlacklist().addNewBan(w, bi, options);
            for (Map.Entry<BanOption, String> entry : options.entrySet()) pl.getConfig().set("blacklist." + w + "." + customName + "." + entry.getKey().name().toLowerCase(), entry.getValue());
        }
        pl.saveConfig();
    }

    /**
     * Remove the material from blacklisted worlds and save the config.yml file. <i>(comments in file may be removed)</i>
     *
     * @param material the Material to ban
     * @param worlds worlds by name where the ban apply
     */
    public void removeFromBlacklist(@NotNull Material material, @NotNull String... worlds) {
        for (String w : worlds) {
            final Map<Material, Map<BanOption, String>> map = getDatabase().getBlacklist().get(w);
            map.remove(material);
            getDatabase().getBlacklist().put(w, map);
            pl.getConfig().set("blacklist." + w + "." + material.name().toLowerCase(), null);
        }
        pl.saveConfig();
    }

    /**
     * Remove a custom item from blacklisted worlds and save the config.yml file. <i>(comments in file may be removed)</i>
     * <p>
     * The custom name can be retrieved with <i>getCustomName(ItemStack)</i>.
     *
     * @param customName the name of the custom item to ban
     * @param worlds worlds by name where the ban apply
     * @throws UnknownObjectException if the custom name is not found
     */
    public void removeFromBlacklist(@NotNull String customName, @NotNull String... worlds) throws UnknownObjectException {
        final BannedItem bi = getDatabase().getCustomItems().get(customName);
        if (bi == null) throw new UnknownObjectException("Unknown custom item named '" + customName + "'.");
        for (String w : worlds) {
            final Map<BannedItem, Map<BanOption, String>> map = getDatabase().getBlacklist().getCustomItems(w);
            map.remove(bi);
            pl.getConfig().set("blacklist." + w + "." + customName, null);
        }
        pl.saveConfig();
    }



    /*------------------------------
     * **********************
     *       WHITELIST
     * **********************
    ------------------------------*/
    /**
     * Get a WhitelistWorld object for the world <b>worldName</b>.
     *
     * @param worldName the name of the whitelisted world
     * @return a WhitelistWorld object or null if the world does not contains whitelist datas
     */
    @Nullable
    public WhitelistWorld getWhitelistWorld(@NotNull String worldName) {
        return getDatabase().getWhitelist().get(worldName);
    }

    /**
     * Add a material to the whitelist
     *
     * @param material the material to add
     * @param options list of ban options
     * @param message set or replace the whitelist world message (leave it null to ignore)
     * @param worlds worlds by name where the ban apply
     */
    public void addToWhitelist(@NotNull Material material, @NotNull List<BanOption> options, @Nullable String message, @NotNull String... worlds) {
        final StringBuilder list = new StringBuilder();
        for (BanOption o : options) list.append(o.name().toLowerCase()).append(",");
        String finalList = list.toString();
        final String option = finalList.substring(0, finalList.length() - 1);
        for (String w : worlds) {
            getDatabase().getWhitelist().addNewException(w, message == null ? " " : message, null, material, options);
            pl.getConfig().set("whitelist." + w + "." + material.name().toLowerCase(), option);
        }
        pl.saveConfig();
    }

    /**
     * Add a material to the whitelist and save the config.yml file. <i>(comments in file may be removed)</i>
     * <p>
     * The custom name can be retrieved with <i>getCustomName(ItemStack)</i>.
     *
     * @param customName custom item name to add
     * @param options list of ban options
     * @param message set or replace the whitelist world message (leave it null to ignore)
     * @param worlds worlds by name where the ban apply
     * @throws UnknownObjectException if the custom name is not found
     */
    public void addToWhitelist(@NotNull String customName, @NotNull List<BanOption> options, @Nullable String message, @NotNull String... worlds) throws UnknownObjectException {
        final BannedItem bi = getDatabase().getCustomItems().get(customName);
        if (bi == null) throw new UnknownObjectException("Unknown custom item named '" + customName + "'.");
        final StringBuilder list = new StringBuilder();
        for (BanOption o : options) list.append(o.name().toLowerCase()).append(",");
        String finalList = list.toString();
        final String option = finalList.substring(0, finalList.length() - 1);
        for (String w : worlds) {
            getDatabase().getWhitelist().addNewException(w, message == null ? " " : message, null, bi, options);
            pl.getConfig().set("whitelist." + w + "." + customName, option);
        }
        pl.saveConfig();
    }

    /**
     * Remove a material from the whitelist <i>(comments in file may be removed)</i>
     *
     * @param material the material
     * @param worlds worlds by name where the ban apply
     */
    public void removeFromWhitelist(@NotNull Material material, @NotNull String... worlds) {
        for (String w : worlds) {
            final WhitelistWorld ww = getDatabase().getWhitelist().get(w);
            if (ww == null) continue;
            ww.remove(material);
            pl.getConfig().set("whitelist." + w + "." + material.name().toLowerCase(), null);
        }
        pl.saveConfig();
    }

    /**
     * Add a material to the whitelist and save the config.yml file. <i>(comments in file may be removed)</i>
     * <p>
     * The custom name can be retrieved with <i>getCustomName(ItemStack)</i>.
     *
     * @param customName custom item name to add
     * @param options list of ban options
     * @param message set or replace the whitelist world message (leave it null to ignore)
     * @param worlds worlds by name where the ban apply
     * @throws UnknownObjectException if the custom name is not found
     */
    public void removeFromWhitelist(@NotNull String customName, @NotNull List<BanOption> options, @Nullable String message, @NotNull String... worlds) throws UnknownObjectException {
        final BannedItem bi = getDatabase().getCustomItems().get(customName);
        if (bi == null) throw new UnknownObjectException("Unknown custom item named '" + customName + "'.");
        for (String w : worlds) {
            final WhitelistWorld ww = getDatabase().getWhitelist().get(w);
            if (ww == null) continue;
            ww.remove(bi);
            pl.getConfig().set("whitelist." + w + "." + customName, null);
        }
        pl.saveConfig();
    }



    /*------------------------------
     * **********************
     *     CUSTOM ITEMS
     * **********************
    ------------------------------*/
    /**
     * Get an ItemStack for the custom item named <b>customName</b>.
     *
     * @param customName the name of the custom item
     * @return the ItemStack of the custom item if exists, otherwise null
     */
    @Nullable
    public ItemStack getCustomItem(@NotNull String customName) {
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
    public String getCustomItemName(@NotNull ItemStack item) {
        final BannedItem bi = new BannedItem(item);
        return getDatabase().getCustomItems().getName(bi);
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
    public void addCustomItem(@NotNull String name, @NotNull ItemStack item) throws Exception {
        getDatabase().addCustomItem(name, item);
    }

    /**
     * Remove the custom ItemStack named <b>name</b>
     *
     * @param name the name of the custom ItemStack
     * @throws Exception if any error occurs (any null value or unable to save the items.yml file)
     */
    public void removeCustomItem(@NotNull String name) throws Exception {
        getDatabase().removeCustomItem(name);
    }
}
