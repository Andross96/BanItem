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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class Whitelist extends HashMap<String, WhitelistWorld> {

    public Whitelist(@NotNull final CommandSender sender, @NotNull final CustomItems customItems) {
        final BanItem pl = BanItem.getInstance();
        // Loading whitelist
        final ConfigurationSection worldsCs = pl.getConfig().getConfigurationSection("whitelist");
        if (worldsCs == null) return;

        for (final String worldKey : worldsCs.getKeys(false)) { // Looping through worlds
            // Checking the world
            final List<World> worlds = BanUtils.getWorldsFromString(sender, false, worldKey);
            if (worlds.isEmpty()) continue;

            // Getting item info
            final ConfigurationSection itemsSection = worldsCs.getConfigurationSection(worldKey);
            if (itemsSection == null) continue;

            String message = null;
            List<BanOption> ignored = null;
            for (final String itemKey : itemsSection.getKeys(false)) {
                // Blocked message?
                if (itemKey.equalsIgnoreCase("message")) {
                    message = itemsSection.getString(itemKey);
                    if (message != null) message = Chat.color(message);
                    continue;
                }

                // Ignored options?
                if (itemKey.equalsIgnoreCase("ignored")) {
                    final String ignoredOptions = itemsSection.getString(itemKey);
                    if (ignoredOptions == null) continue;
                    ignored = BanUtils.getBanOptionsFromString(sender, false, ignoredOptions, itemKey, worldKey);
                    continue;
                }

                // Getting item
                final Material m = Material.matchMaterial(itemKey);
                BannedItem bi = null;
                if (m == null) {
                    bi = customItems.get(itemKey);
                    if (bi == null) {
                        sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &cUnknown item &e" + itemKey + "&c set for world &e" + worldKey + "&c in whitelist of config.yml"));
                        continue;
                    }
                }

                // Getting options for the item
                final String options = itemsSection.getString(itemKey);
                if (options == null) continue;
                final List<BanOption> banOptions = BanUtils.getBanOptionsFromString(sender, false, options, itemKey, worldKey);
                if (banOptions.isEmpty()) continue;

                // Adding into the map
                if (bi != null) for (final World w : worlds) addNewException(w.getName(), message == null ? null : Chat.color(message), ignored, bi, banOptions);
                else for (final World w : worlds) addNewException(w.getName(), message == null ? null : Chat.color(message), ignored, m, banOptions);
            }
        }
    }

    public void addNewException(@NotNull final String world, @Nullable final String message, @Nullable final List<BanOption> ignored, @NotNull final Material m, @NotNull final List<BanOption> o) {
        WhitelistWorld wlw = get(world);
        if (wlw == null) wlw = new WhitelistWorld(message, ignored);
        wlw.addNewEntry(m, o);
        put(world, wlw);
    }

    public void addNewException(@NotNull final String world, @Nullable final String message, @Nullable final List<BanOption> ignored, @NotNull final BannedItem i, @NotNull final List<BanOption> o) {
        WhitelistWorld wlw = get(world);
        if (wlw == null) wlw = new WhitelistWorld(message, ignored);
        wlw.addNewEntry(i, o);
        put(world, wlw);
    }

    public int getTotal() {
        int i = 0;
        for (WhitelistWorld ww : values()) i += ww.count();
        return i;
    }

}
