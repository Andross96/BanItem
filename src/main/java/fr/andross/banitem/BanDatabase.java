package fr.andross.banitem;

import fr.andross.banitem.Database.Blacklist;
import fr.andross.banitem.Database.CustomItems;
import fr.andross.banitem.Database.Whitelist;
import fr.andross.banitem.Options.BanOption;
import fr.andross.banitem.Utils.Item.BannedItem;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that contains all the maps
 * @version 2.0
 * @author Andross
 */
public final class BanDatabase {
    private final CustomItems customItems;
    private final Blacklist blacklist;
    private final Whitelist whitelist;

    /**
     * This should not be instantiate. Use {@link BanItem#load(CommandSender, FileConfiguration)} instead.
     * @param pl the main instance
     * @param sender the command sender who wants to (re)load
     * @param config the configuration file used
     */
    BanDatabase(@NotNull final BanItem pl, @NotNull final CommandSender sender, @NotNull final FileConfiguration config) {
        this.customItems = new CustomItems(pl, sender);
        // Setting the customItems instance
        pl.getUtils().setCustomItems(customItems);
        this.blacklist = new Blacklist(pl, sender, config.getConfigurationSection("blacklist"));
        this.whitelist = new Whitelist(pl, sender, config.getConfigurationSection("whitelist"));
    }

    /**
     * Getting an immutable set of used ban options <i>({@link BanOption})</i>
     * This is actually used to register the specific listeners for the specific options
     * @return An immutable set containing all used ban options in the blacklist
     */
    @NotNull
    public Set<BanOption> getBlacklistOptions() {
        final Set<BanOption> options = new HashSet<>();
        blacklist.values().forEach(v1 -> v1.forEach((k,v) -> options.addAll(v.keySet())));
        return Collections.unmodifiableSet(options);
    }

    /**
     * This checks if the whitelist is enabled <i>(contains items)</i>
     * This method is mainly used to check if the listeners have to be registered to activate a whitelist on a world.
     * @return if the whitelist is enabled
     */
    public boolean isWhitelistEnabled() {
        return !whitelist.isEmpty();
    }

    /**
     * Try to add a custom item <i>({@link BannedItem} object)</i> into the map and the config file
     * @param customName name of the custom item
     * @param customItem ItemStack
     * @throws Exception if any exception occurs (like writting in file)
     */
    public void addCustomItem(@NotNull final String customName, @NotNull final ItemStack customItem) throws Exception {
        // Adding in map
        customItems.put(customName, new BannedItem(customItem, true));

        // Adding in file
        final FileConfiguration config = customItems.getItemsConfig();
        if (config == null) throw new Exception("Config file not initialized");
        config.set(customName, customItem);
        config.save(customItems.getItemsFile());
    }

    /**
     * Try to remove the custom item with the said name.
     * You should check if the custom item exists <i>({@link CustomItems#getName(BannedItem)})</i> before calling this method
     * @param customName name of the custom item
     * @throws Exception if any exception occurs (like writting in file)
     */
    public void removeCustomItem(@NotNull final String customName) throws Exception {
        // Removing from map
        customItems.remove(customName);

        // Removing from file
        final FileConfiguration config = customItems.getItemsConfig();
        if (config == null) throw new Exception("Config file not initialized");
        config.set(customName, null);
        config.save(customItems.getItemsFile());
    }

    /**
     * Get the custom items map
     * @return map of custom items <i>({@link BannedItem})</i>
     */
    @NotNull
    public CustomItems getCustomItems() {
        return customItems;
    }

    /**
     * Get the blacklist map
     * @return map containing the blacklisted items
     */
    @NotNull
    public Blacklist getBlacklist() {
        return blacklist;
    }

    /**
     * Get the whitelist map
     * @return map containing the whitelisted items
     */
    @NotNull
    public Whitelist getWhitelist() {
        return whitelist;
    }
}
