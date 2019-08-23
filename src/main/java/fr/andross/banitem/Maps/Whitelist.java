package fr.andross.banitem.Maps;
import fr.andross.banitem.BanItem;
import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BanUtils;
import fr.andross.banitem.Utils.BannedItem;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class Whitelist extends HashMap<String, WhitelistWorld> {

    public Whitelist(final BanItem pl, final CommandSender sender, final CustomItems customItems) {
        final BanUtils utils = pl.getUtils();

        // Loading whitelist
        final ConfigurationSection worldsCs = pl.getConfig().getConfigurationSection("whitelist");
        if(worldsCs == null) return;

        for (final String worldKey : worldsCs.getKeys(false)) { // Looping through worlds
            // Checking the world
            final List<World> worlds = utils.getWorldsFromString(worldKey);
            if (worlds == null || worlds.isEmpty()) {
                sender.sendMessage(pl.color("&c[&e&lBanItem&c] &cUnknown world(s) &e" + worldKey + "&c set in whitelist of config.yml"));
                continue;
            }

            // Getting item info
            final ConfigurationSection itemsSection = worldsCs.getConfigurationSection(worldKey);
            if (itemsSection == null) continue;

            String message = null;
            List<BanOption> ignored = null;
            for (final String itemKey : itemsSection.getKeys(false)) {
                // Blocked message?
                if (itemKey.equalsIgnoreCase("message")) {
                    message = itemsSection.getString(itemKey);
                    if (message != null) message = pl.color(message);
                    continue;
                }

                // Ignored options?
                if (itemKey.equalsIgnoreCase("ignored")) {
                    final String ignoredOptions = itemsSection.getString(itemKey);
                    if (ignoredOptions == null) continue;
                    ignored = utils.getBanOptionsFromString(ignoredOptions);
                    if (ignored == null) sender.sendMessage(pl.color("&c[&e&lBanItem&c] &cUnknown ignored options &e" + itemKey + "&c set for world &e" + worldKey + "&c in whitelist of config.yml"));
                    continue;
                }

                // Getting item
                final BannedItem bannedItem = utils.getBannedItemFromString(itemKey, customItems);
                if (bannedItem == null) {
                    sender.sendMessage(pl.color("&c[&e&lBanItem&c] &cUnknown item &e" + itemKey + "&c set for world &e" + worldKey + "&c in whitelist of config.yml"));
                    continue;
                }
                // Getting options for the item
                final String options = itemsSection.getString(itemKey);
                if (options == null) continue;
                final List<BanOption> banOptions = utils.getBanOptionsFromString(options);
                // Incorrect option(s)?
                if (banOptions == null || banOptions.isEmpty()) {
                    sender.sendMessage(pl.color("&c[&e&lBanItem&c] &cInvalid options &e" + options + "&c for item &e" + itemKey + "&c set for world &e" + worldKey + "&c in whitelist of config.yml"));
                    continue;
                }

                // Adding into the map
                for (World w : worlds) addNewException(w.getName(), message, ignored, bannedItem, banOptions);
            }
        }
    }

    public void addNewException(final String world, final String message, final List<BanOption> ignored, final BannedItem i, final List<BanOption> o) {
        WhitelistWorld wlw = get(world);
        if (wlw == null) wlw = new WhitelistWorld(message, ignored);
        wlw.addNewEntry(i, o);
        put(world, wlw);
    }

    public int getTotal() {
        int i = 0;
        for (WhitelistWorld ww : values()) i += ww.getWhitelist().keySet().size();
        return i;
    }

}
