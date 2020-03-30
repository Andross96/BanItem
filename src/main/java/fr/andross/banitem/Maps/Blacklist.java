package fr.andross.banitem.Maps;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BanUtils;
import fr.andross.banitem.Utils.BannedItem;
import fr.andross.banitem.Utils.Chat;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blacklist extends HashMap<String, Map<Material, Map<BanOption, String>>> {
    private final Map<String, Map<BannedItem, Map<BanOption, String>>> customItems = new HashMap<>();

    public Blacklist(@NotNull final CommandSender sender, @NotNull final CustomItems customItems) {
        final BanItem pl = BanItem.getInstance();
        // Loading blacklist
        final ConfigurationSection worldsCs = pl.getConfig().getConfigurationSection("blacklist");
        if (worldsCs == null) return;

        for (final String worldKey : worldsCs.getKeys(false)) { // Looping through worlds
            // Getting world(s)
            final List<World> worlds = BanUtils.getWorldsFromString(sender, true, worldKey);
            if (worlds.isEmpty()) continue;

            // Checking the banned item
            final ConfigurationSection materialsCs = worldsCs.getConfigurationSection(worldKey);
            if (materialsCs == null) continue;
            for (final String materialKey : materialsCs.getKeys(false)) {
                // Getting the banned item
                final Material m = Material.matchMaterial(materialKey);
                BannedItem bi = null;
                if (m == null) {
                    bi = customItems.get(materialKey);
                    if (bi == null) {
                        sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &cUnknown item &e" + materialKey + "&c set for world &e" + worldKey + "&c in blacklist of config.yml"));
                        continue;
                    }
                }

                // Getting options
                final ConfigurationSection optionsCs = materialsCs.getConfigurationSection(materialKey);
                if (optionsCs == null) continue;
                final Map<BanOption, String> options = new HashMap<>();
                for (final String optionKey : optionsCs.getKeys(false)) {
                    final String message = optionsCs.getString(optionKey);
                    final List<BanOption> banOptions = BanUtils.getBanOptionsFromString(sender, true, optionKey, materialKey, worldKey);
                    if (banOptions.isEmpty()) continue;
                    for (final BanOption o : banOptions) options.put(o, message == null ? null : Chat.color(message));
                }
                if (options.isEmpty()) continue;

                // Adding into the map
                if (bi != null) for (final World w : worlds) addNewBan(w.getName(), bi, options);
                else for (final World w : worlds) addNewBan(w.getName(), m, options);
            }
        }
    }

    public void addNewBan(@NotNull final String w, @NotNull final Material m, @Nullable final Map<BanOption, String> o) {
        Map<Material, Map<BanOption, String>> newmap = get(w);
        if(newmap == null) newmap = new HashMap<>();
        newmap.put(m, o);
        put(w, newmap);
    }

    public void addNewBan(@NotNull final String w, @NotNull final BannedItem bi, @Nullable final Map<BanOption, String> o) {
        Map<BannedItem, Map<BanOption, String>> newmap = customItems.get(w);
        if(newmap == null) newmap = new HashMap<>();
        newmap.put(bi, o);
        customItems.put(w, newmap);
    }

    public Map<BanOption, String> getBanOptions(@NotNull final String w, @NotNull final BannedItem bi) {
        final Map<BanOption, String> map = new HashMap<>();
        // Normal items
        if (containsKey(w)) {
            final Map<Material, Map<BanOption, String>> normalMap = get(w);
            if (normalMap.containsKey(bi.getType())) map.putAll(normalMap.get(bi.getType()));
        }
        // Custom items
        if (customItems.containsKey(w)) {
            final Map<BannedItem, Map<BanOption, String>> customMap = customItems.get(w);
            if (customMap.containsKey(bi)) map.putAll(customMap.get(bi));
        }
        return map;
    }

    public Map<BanOption, String> getBanOptions(@NotNull final String w, @NotNull final ItemStack item) {
        return getBanOptions(w, new BannedItem(item));
    }

    public Map<BannedItem, Map<BanOption, String>> getCustomItems(final String w) {
        return customItems.get(w);
    }

    public int getTotal() {
        int i = 0;
        for (Map<Material, Map<BanOption, String>> map : values()) i += map.size();
        for (Map<BannedItem, Map<BanOption, String>> map : customItems.values()) i += map.keySet().size();
        return i;
    }

}
