package fr.andross.banitem;

import fr.andross.banitem.Maps.Blacklist;
import fr.andross.banitem.Maps.CustomItems;
import fr.andross.banitem.Maps.Whitelist;
import fr.andross.banitem.Maps.WhitelistWorld;
import fr.andross.banitem.Utils.BanAnimation;
import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BanUtils;
import fr.andross.banitem.Utils.BannedItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class BanDatabase {
    private final BanItem pl;
    private final Blacklist blacklist;
    private final Whitelist whitelist;
    private final CustomItems customItems;
    private final BanAnimation animations;
    private final Map<UUID, Long> pickupCooldowns = new HashMap<>();
    private final long pickupCooldown;

    public BanDatabase(final BanItem pl, final CommandSender sender) {
        this.pl = pl;
        this.customItems = new CustomItems(pl, sender);
        this.blacklist = new Blacklist(pl, sender, customItems);
        this.whitelist = new Whitelist(pl, sender, customItems);
        this.animations = new BanAnimation(pl, sender);
        pickupCooldown = pl.getConfig().getLong("pickup-message-cooldown", 1000);
    }

    public Set<BanOption> getBlacklistOptions() {
        final Set<BanOption> options = new HashSet<>();
        for(Map<Material, Map<BanOption, String>> values : blacklist.values()) {
            for (Map<BanOption, String> value : values.values()) {
                options.addAll(value.keySet());
            }
        }
        return options;
    }

    public boolean isWhitelistEnabled() {
        return whitelist.isEmpty();
    }

    public boolean isBanned(final Player p, final ItemStack item, final BanOption option) {
        // Variables
        final String w = p.getWorld().getName().toLowerCase();
        final String itemName = item.getType().name().toLowerCase();
        final String optionName = option.name().toLowerCase();
        final BannedItem bannedItem = new BannedItem(item);
        final String customItemName = customItems.getName(bannedItem);

        // Checking permission bypass
        if (p.hasPermission("banitem.bypass." + w + "." + itemName + "." + optionName)) return false;
        if (customItemName != null) if (p.hasPermission("banitem.bypass." + w + "." + customItemName + "." + optionName)) return false;

        /* Checking blacklisted */
        final Map<BanOption, String> blacklisted = blacklist.getBanOptions(w, bannedItem);
        // Adding custom item options
        bl: if (!blacklisted.isEmpty()) {
            // Creative only?
            if (blacklisted.containsKey(BanOption.CREATIVE) && p.getGameMode() != GameMode.CREATIVE) break bl;
            // Delete?
            if (blacklisted.containsKey(BanOption.DELETE)) Bukkit.getScheduler().runTask(pl, () -> {
                BanUtils.deleteItemFromInventory(pl, p.getWorld().getName(), p.getInventory());
                final String message = blacklisted.get(BanOption.DELETE);
                if (message != null && !message.isEmpty()) p.sendMessage(pl.color(message));
            });
            // Sending banned message, if exists
            if (blacklisted.containsKey(option)) {
                sendMessage(p, option, blacklisted.get(option));
                return true;
            }
        }

        /* Checking whitelisted */
        // Checking world
        final WhitelistWorld ww = whitelist.get(w);
        if (ww == null) return false;
        // Is ignored?
        if (ww.isIgnored(option)) return false;
        // Checking banned item
        final Set<BanOption> options = ww.getBanOptions(bannedItem);
        if (options != null && options.contains(option)) return false;

        sendMessage(p, option, ww.getMessage());
        return true;
    }

    public boolean isDispenseBanned(final String world, final ItemStack item) {
        final BannedItem bannedItem = new BannedItem(item);

        /* Checking blacklisted */
        final Map<BanOption, String> blacklisted = blacklist.getBanOptions(world, bannedItem);
        if (!blacklisted.isEmpty() && blacklisted.containsKey(BanOption.DISPENSE)) return true;

        /* Checking whitelisted */
        final WhitelistWorld ww = whitelist.get(world);
        if (ww == null || ww.isIgnored(BanOption.DISPENSE)) return false;
        final Set<BanOption> options = ww.getBanOptions(bannedItem);
        return options == null || !options.contains(BanOption.DISPENSE);
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

        // Sending message
        p.sendMessage(m);
        animations.runAnimation(p);
    }

    public void addCustomItem(@NotNull final String customName, @NotNull final ItemStack customItem) throws Exception {
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
