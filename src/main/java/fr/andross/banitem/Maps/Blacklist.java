package fr.andross.banitem.Maps;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BanUtils;
import fr.andross.banitem.Utils.BannedItem;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blacklist extends HashMap<String, Map<BannedItem, Map<BanOption, String>>> {

    public Blacklist(final BanItem pl, final CommandSender sender, final CustomItems customItems) {
        final BanUtils utils = pl.getUtils();

        // Loading blacklist
        final ConfigurationSection worldsCs = pl.getConfig().getConfigurationSection("blacklist");
        if (worldsCs == null) return;

        for (final String worldKey : worldsCs.getKeys(false)) { // Looping through worlds
            // Getting world(s)
            final List<World> worlds = utils.getWorldsFromString(worldKey);
            if (worlds == null || worlds.isEmpty()) {
                sender.sendMessage(pl.color("&c[&e&lBanItem&c] &cUnknown world(s) &e" + worldKey + "&c set in blacklist of config.yml"));
                continue;
            }

            // Checking the banned item
            final ConfigurationSection materialsCs = worldsCs.getConfigurationSection(worldKey);
            if (materialsCs == null) continue;
            for (final String materialKey : materialsCs.getKeys(false)) {
                // Getting the banned item
                final BannedItem bannedItem = utils.getBannedItemFromString(materialKey, customItems);
                if (bannedItem == null) {
                    sender.sendMessage(pl.color("&c[&e&lBanItem&c] &cUnknown item &e" + materialKey + "&c set for world &e" + worldKey + "&c in blacklist of config.yml"));
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
                        sender.sendMessage(pl.color("&c[&e&lBanItem&c] &cInvalid options &e" + optionKey + "&c for item &e" + materialKey + "&c set for world &e" + worldKey + "&c in blacklist of config.yml"));
                        continue;
                    }

                    for (BanOption o : banOptions) options.put(o, pl.color(message));
                }
                if (options.isEmpty()) continue;

                // Adding into the map
                for (World w : worlds) addNewBan(w.getName(), bannedItem, options);
            }
        }
    }

    public void addNewBan(final String w, final BannedItem i, final Map<BanOption, String> o) {
        Map<BannedItem, Map<BanOption, String>> newmap = get(w);
        if(newmap == null) newmap = new HashMap<>();
        newmap.put(i, o);
        put(w, newmap);
    }

    public int getTotal() {
        int i = 0;
        for (Map<BannedItem, Map<BanOption, String>> map : values()) i += map.keySet().size();
        return i;
    }

}
