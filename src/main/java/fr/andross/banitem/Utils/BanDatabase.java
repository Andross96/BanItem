package fr.andross.banitem.Utils;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Maps.Blacklisted;
import fr.andross.banitem.Maps.CustomItems;
import fr.andross.banitem.Maps.Whitelisted;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public final class BanDatabase {
    // Maps
    private final BanItem pl;
    private final Blacklisted blacklist = new Blacklisted();
    private final Whitelisted whitelist = new Whitelisted();
    private final CustomItems customItems = new CustomItems();
    private final Map<UUID, Long> pickupCooldowns = new HashMap<>();
    private long pickupCooldown = 1000;

    // Items.yml
    private final File itemsFile;
    private FileConfiguration itemsConfig;

    // Utils
    private final BanUtils utils = new BanUtils();

    public BanDatabase(final BanItem pl) {
        this.pl = pl;
        this.itemsFile = new File(pl.getDataFolder(), "items.yml");
    }

    // Getters
    public Map<UUID, Long> getPickupCooldowns() { return pickupCooldowns; }
    public CustomItems getCustomItems() { return customItems; }
    public Blacklisted getBlacklist() { return blacklist; }
    public Whitelisted getWhitelist() { return whitelist; }
    public BanUtils getUtils() { return utils; }

    public void load(final CommandSender sender) {
        // Loading custom items (items.yml)
        loadItems(sender);

        // Loading config (config.yml)
        pickupCooldown = pl.getConfig().getLong("pickup-message-cooldown", 1000);
        // Loading maps
        loadBlacklist(sender);
        loadWhitelist(sender);
    }

    // Loading items.yml
    private void loadItems(final CommandSender sender) {
        // Clearing map
        customItems.clear();

        // Checking/Creating file
        try {
            // Trying to save the custom one, else creating a new one
            if (!itemsFile.isFile()) pl.saveResource("items.yml", false);
            if (!itemsFile.isFile()) if (!itemsFile.createNewFile()) throw new Exception();
        } catch (Exception e) {
            sender.sendMessage("Unable to create 'items.yml' file.");
            return;
        }

        // Loading custom items
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        for (String key : itemsConfig.getKeys(false)) {
            try {
                final ItemStack itemStack = (ItemStack) itemsConfig.get(key);
                if (itemStack == null) continue;
                customItems.put(key, new BannedItem(itemStack));
            } catch (Exception e) {
                sender.sendMessage("Unvalid custom item '" + key + "' in items.yml.");
            }
        }
    }

    // Loading blacklist from config.yml
    private void loadBlacklist(final CommandSender sender) {
        // Clearing map
        blacklist.clear();

        // Loading blacklist
        final ConfigurationSection worldsCs = pl.getConfig().getConfigurationSection("blacklist");
        if (worldsCs == null) return;

        for (final String worldKey : worldsCs.getKeys(false)) { // Looping through worlds
            // Getting world(s)
            final List<World> worlds = utils.getWorldsFromString(worldKey);
            if (worlds == null || worlds.isEmpty()) {
                sender.sendMessage("Unknown world(s) '" + worldKey + "' set in blacklist of config.yml");
                continue;
            }

            // Checking the banned item
            final ConfigurationSection materialsCs = worldsCs.getConfigurationSection(worldKey);
            if (materialsCs == null) continue;
            for (final String materialKey : materialsCs.getKeys(false)) {
                // Getting the banned item
                final BannedItem bannedItem = utils.getBannedItemFromString(materialKey, customItems);
                if (bannedItem == null) {
                    sender.sendMessage("Unknown item '" + materialKey + "' set for world '" + worldKey + "' in blacklist of config.yml");
                    continue;
                }

                // Getting options
                final ConfigurationSection optionsCs = materialsCs.getConfigurationSection(materialKey);
                if(optionsCs == null) continue;
                final Map<BanOption, String> options = new HashMap<>();
                for (String optionKey : optionsCs.getKeys(false)) {
                    final String message = optionsCs.getString(optionKey);
                    final List<BanOption> banOptions = utils.getBanOptionsFromString(optionKey);

                    // Incorrect option(s)?
                    if (banOptions == null || banOptions.isEmpty()) {
                        sender.sendMessage("Invalid options '" + optionKey + "' for item '" + materialKey + "' set for world '" + worldKey + "' in blacklist of config.yml");
                        continue;
                    }

                    for (BanOption o : banOptions) options.put(o, pl.color(message));
                }
                if (options.isEmpty()) continue;

                // Adding into the map
                for (World w : worlds) blacklist.addNewBan(w.getName(), bannedItem, options);
            }
        }
    }

    // Loading whitelist from config.yml
    private void loadWhitelist(final CommandSender sender) {
        // Clearing map
        whitelist.clear();

        // Loading whitelist
        final ConfigurationSection worldsCs = pl.getConfig().getConfigurationSection("whitelist");
        if(worldsCs == null) return;

        for (final String worldKey : worldsCs.getKeys(false)) { // Looping through worlds
            // Checking the world
            final List<World> worlds = utils.getWorldsFromString(worldKey);
            if (worlds == null || worlds.isEmpty()) {
                sender.sendMessage("Unknown world(s) '" + worldKey + "' set in whitelist of config.yml");
                continue;
            }

            // Getting item info
            final ConfigurationSection itemsSection = worldsCs.getConfigurationSection(worldKey);
            if (itemsSection == null) continue;

            String message = null;
            for (final String itemKey : itemsSection.getKeys(false)) {
                // Blocked message?
                if (itemKey.equalsIgnoreCase("message")) {
                    message = itemsSection.getString(itemKey);
                    if (message != null) message = pl.color(message);
                    continue;
                }

                // Getting item
                final BannedItem bannedItem = utils.getBannedItemFromString(itemKey, customItems);
                if (bannedItem == null) {
                    sender.sendMessage("Unknown item '" + itemKey + "' set for world '" + worldKey + "' in whitelist of config.yml");
                    continue;
                }
                // Getting options for the item
                final String options = itemsSection.getString(itemKey);
                if (options == null) continue;
                final List<BanOption> banOptions = utils.getBanOptionsFromString(options);
                // Incorrect option(s)?
                if (banOptions == null || banOptions.isEmpty()) {
                    sender.sendMessage("Invalid options '" + options + "' for item '" + itemKey + "' set for world '" + worldKey + "' in whitelist of config.yml");
                    continue;
                }

                // Adding into the map
                for (World w : worlds) whitelist.addNewException(w.getName(), message, bannedItem, banOptions);
            }
        }
    }

    public Set<BanOption> getBlacklistOptions() {
        final Set<BanOption> options = new HashSet<>();

        for(Map<BannedItem, Map<BanOption, String>> values : blacklist.values()) {
            for (Map<BanOption, String> value : values.values()) {
                options.addAll(value.keySet());
            }
        }

        return options;
    }

    public boolean isWhitelistEnabled() {
        return whitelist.isEmpty();
    }


    public boolean isBanned(final Player p, final ItemStack item, final BanOption o) {
        final String w = p.getWorld().getName().toLowerCase();

        // Creating BannedItem object
        final BannedItem bannedItem = new BannedItem(item);
        final String customItemName = customItems.getName(bannedItem);

        // Checking permission bypass for normal items
        if (p.hasPermission("banitem.bypass." + w + "." + item.getType().name().toLowerCase())) return false;
        // Checking permission bypass for custom items?
        if (customItemName != null) if (p.hasPermission("banitem.bypass." + w + "." + customItemName)) return false;

        /* Checking blacklisted */
        final Map<BannedItem, Map<BanOption, String>> blacklisted = blacklist.get(w);
        if (blacklisted != null) {
            final Map<BanOption, String> options = blacklisted.get(bannedItem);
            if (options != null) {
                // Checking options
                // Creative?
                if (options.containsKey(BanOption.CREATIVE) && p.getGameMode() != GameMode.CREATIVE) return false;

                // Sending banned message, if exists
                if (options.containsKey(o)) {
                    sendMessage(p, o, options.get(o));
                    return true;
                }
            }
        }

        // Ignoring inventory for whitelist
        if(o == BanOption.INVENTORY) return false;

        /* Checking whitelisted */
        // Checking world
        final WhitelistedWorld whitelisted = whitelist.get(w);
        if (whitelisted == null) return false;

        // Checking banned item
        final Set<BanOption> options = whitelisted.getWhitelisted().get(bannedItem);
        if (options != null && options.contains(o)) return false;

        sendMessage(p, o, whitelisted.getMessage());
        return true;
    }

    private void sendMessage(final Player p, final BanOption o, final String m) {
        // No message set
        if(m == null || m.isEmpty()) return;

        // Checking pick up cooldown, to prevent spam
        if (o == BanOption.PICKUP) {
            final Long time = pickupCooldowns.get(p.getUniqueId());
            if (time == null) pickupCooldowns.put(p.getUniqueId(), System.currentTimeMillis());
            else {
                if (time + pickupCooldown > System.currentTimeMillis()) return;
                else pickupCooldowns.put(p.getUniqueId(), System.currentTimeMillis());
            }
        }
        p.sendMessage(m);
    }

    public void addCustomItem(final String customName, final ItemStack customItem) throws Exception {
        // Adding in map
        customItems.put(customName, new BannedItem(customItem));

        // Adding in file
        itemsConfig.set(customName, customItem);
        itemsConfig.save(itemsFile);
    }

    public void removeCustomItem(final String customName) throws Exception {
        // Removing from map
        customItems.remove(customName);

        // Removing from file
        itemsConfig.set(customName, null);
        itemsConfig.save(itemsFile);
    }

}
