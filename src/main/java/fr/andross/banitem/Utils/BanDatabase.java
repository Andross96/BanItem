package fr.andross.banitem.Utils;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Maps.Blacklist;
import fr.andross.banitem.Maps.CustomItems;
import fr.andross.banitem.Maps.Whitelist;
import fr.andross.banitem.Maps.WhitelistWorld;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class BanDatabase {
    private final Blacklist blacklist;
    private final Whitelist whitelist;
    private final CustomItems customItems;
    private final Map<UUID, Long> pickupCooldowns = new HashMap<>();
    private final long pickupCooldown;

    public BanDatabase(final BanItem pl, final CommandSender sender) {
        this.customItems = new CustomItems(pl, sender);
        this.blacklist = new Blacklist(pl, sender, customItems);
        this.whitelist = new Whitelist(pl, sender, customItems);
        pickupCooldown = pl.getConfig().getLong("pickup-message-cooldown", 1000);
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

        /* Checking whitelisted */
        // Checking world
        final WhitelistWorld ww = whitelist.get(w);
        if (ww == null) return false;

        // Is ignored?
        if (ww.isIgnored(o)) return false;

        // Checking banned item
        final Set<BanOption> options = ww.getWhitelist().get(bannedItem);
        if (options != null && options.contains(o)) return false;

        sendMessage(p, o, ww.getMessage());
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
        customItems.getItemsConfig().set(customName, customItem);
        customItems.getItemsConfig().save(customItems.getItemsFile());
    }

    public void removeCustomItem(final String customName) throws Exception {
        // Removing from map
        customItems.remove(customName);

        // Removing from file
        customItems.getItemsConfig().set(customName, null);
        customItems.getItemsConfig().save(customItems.getItemsFile());
    }

    public Map<UUID, Long> getPickupCooldowns() { return pickupCooldowns; }
    public CustomItems getCustomItems() { return customItems; }
    public Blacklist getBlacklist() { return blacklist; }
    public Whitelist getWhitelist() { return whitelist; }

}
