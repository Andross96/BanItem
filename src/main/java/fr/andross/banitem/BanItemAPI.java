package fr.andross.banitem;

import fr.andross.banitem.Maps.Blacklist;
import fr.andross.banitem.Maps.CustomItems;
import fr.andross.banitem.Maps.Whitelist;
import fr.andross.banitem.Utils.BanDatabase;
import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BanUtils;
import fr.andross.banitem.Maps.WhitelistWorld;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface BanItemAPI {

    /**
     * Get the BanItem database, containing blacklist, whitelist & custom items
     *
     * @return the BanDatabase object
     */
    @NotNull
    BanDatabase getDatabase();

    /**
     * Get the custom items map from database containing all custom items
     *
     * @return the custom items map
     */
    @NotNull
    CustomItems getCustomItems();

    /**
     * Get the blacklist map from database containing all blacklist items
     *
     * @return the blacklist map
     */
    @NotNull
    Blacklist getBlacklist();

    /**
     * Get the whitelist map from database containing all whitelist items
     *
     * @return the whitelist map
     */
    @NotNull
    Whitelist getWhitelist();

    /**
     * Get the some utils
     *
     * @return some utilities
     */
    @NotNull
    BanUtils getUtils();

    /**
     * Reload the plugin
     *
     * @param sender Sender to send messages to
     */
    void reload(org.bukkit.command.CommandSender sender);

    /**
     * Return a Map of BanOptions with messages if the ItemStack is banned in World
     *
     * @param item the ItemStack
     * @param world the world name
     * @return A map containing the ban options & messages
     */
    @Nullable
    Map<BanOption, String> getBlacklist(@NotNull ItemStack item, @NotNull String world);

    /**
     * Return a WhitelistWorld, containing allowed items & the message for each world
     *
     * @param world the world name
     * @return A map containing the whitelist world informations
     */
    @Nullable
    WhitelistWorld getWhitelist(@NotNull String world);

    /**
     * Add a custom ItemStack into the plugin database
     * <b>Will replace existing value</b>
     *
     * @param name the name of the custom ItemStack
     * @param item the custom ItemStack
     * @throws Exception if could not save the database
     */
    void addCustomItem(@NotNull String name, @NotNull ItemStack item) throws Exception;

    /**
     * Remove a custom ItemStack from the plugin database
     *
     * @param name the name of the custom ItemStack
     * @throws Exception if could not save the database
     */
    void removeCustomItem(@NotNull String name) throws Exception;

    /**
     * Check if the database contains a custom ItemStack
     *
     * @param item the custom ItemStack
     * @return <b>true</b> if database contains this custom ItemStack, otherwise <b>false</b>
     */
    boolean containsCustomItem(@NotNull ItemStack item);

    /**
     * Check if the database contains a custom item by name
     *
     * @param name the name of the custom ItemStack
     * @return <b>true</b> if database contains this custom item name, otherwise <b>false</b>
     */
    boolean containsCustomItem(@NotNull String name);

    /**
     * Add a new blacklist item
     *
     * @param item the ItemStack to ban
     * @param options a map containing options & message to ban
     * @param worlds worlds by name where the ban apply
     */
    void addToBlacklist(@NotNull ItemStack item, @NotNull Map<BanOption, String> options, @NotNull String... worlds);

    /**
     * Remove a blacklist item
     *
     * @param item the ItemStack to remove
     * @param worlds worlds by name where the ban apply
     */
    void removeFromBlacklist(@NotNull ItemStack item, @NotNull String... worlds);

    /**
     * Add an ItemStack to the whitelist
     *
     * @param item the ItemStack to add
     * @param options list of ban options
     * @param worlds worlds by name where the ban apply
     */
    void addToWhitelist(@NotNull ItemStack item, @NotNull List<BanOption> options, @NotNull String... worlds);

    /**
     * Remove an ItemStack from the whitelist
     *
     * @param item the ItemStack to remove
     * @param worlds worlds by name where the ban apply
     */
    void removeFromWhitelist(@NotNull ItemStack item, @NotNull String... worlds);

    /**
     * Get a BanOption value
     * <b>Options are:</b> place, break, pickup, drop, interact, use, creative, inventory
     * @param option Name of the option
     * @return a BanOption value if exists, otherwise null
     */
    @Nullable
    BanOption getBanOption(@NotNull String option);

}
