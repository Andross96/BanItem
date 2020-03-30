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
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class BanDatabase {
    private final Blacklist blacklist;
    private final Whitelist whitelist;
    private final CustomItems customItems;
    private final BanAnimation animations;
    private final Map<UUID, Long> pickupCooldowns = new HashMap<>();
    private final long pickupCooldown;

    public BanDatabase(@NotNull final CommandSender sender) {
        this.customItems = new CustomItems(sender);
        this.blacklist = new Blacklist(sender, customItems);
        this.whitelist = new Whitelist(sender, customItems);
        this.animations = new BanAnimation(sender);
        pickupCooldown = BanItem.getInstance().getConfig().getLong("pickup-message-cooldown");
    }

    @NotNull
    public Set<BanOption> getBlacklistOptions() {
        final Set<BanOption> options = new HashSet<>();
        for(final Map<Material, Map<BanOption, String>> values : blacklist.values())
            for (final Map<BanOption, String> value : values.values())
                options.addAll(value.keySet());
        return options;
    }

    public boolean isWhitelistEnabled() {
        return !whitelist.isEmpty();
    }

    public boolean isBanned(@NotNull final Player p, @NotNull final ItemStack item, @NotNull final BanOption option) {
        // Variables
        final String w = p.getWorld().getName();
        final String wLower = w.toLowerCase();
        final String itemName = item.getType().name().toLowerCase();
        final String optionName = option.name().toLowerCase();
        final BannedItem bannedItem = new BannedItem(item);
        final String customItemName = customItems.getName(bannedItem);

        // Checking permission bypass
        if (BanUtils.hasPermission(p, wLower, itemName, customItemName, optionName)) return false;

        /* Checking blacklisted */
        final Map<BanOption, String> blacklisted = blacklist.getBanOptions(w, bannedItem);
        // Adding custom item options
        bl: if (!blacklisted.isEmpty()) {
            // Creative only?
            if (blacklisted.containsKey(BanOption.CREATIVE) && p.getGameMode() != GameMode.CREATIVE) break bl;
            // Delete?
            if (blacklisted.containsKey(BanOption.DELETE)) Bukkit.getScheduler().runTask(BanItem.getInstance(), () -> BanUtils.deleteItemFromInventory(p, p.getInventory()));
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

    public boolean isBanned(@NotNull final String world, @NotNull final ItemStack item, @NotNull final BanOption option) {
        final BannedItem bannedItem = new BannedItem(item);

        /* Checking blacklisted */
        final Map<BanOption, String> blacklisted = blacklist.getBanOptions(world, bannedItem);
        if (!blacklisted.isEmpty() && blacklisted.containsKey(option)) return true;

        /* Checking whitelisted */
        final WhitelistWorld ww = whitelist.get(world);
        if (ww == null || ww.isIgnored(option)) return false;
        final Set<BanOption> options = ww.getBanOptions(bannedItem);
        return options == null || !options.contains(option);
    }

    private void sendMessage(@NotNull final Player p, @NotNull final BanOption o, @Nullable final String m) {
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

    public void removeCustomItem(@NotNull final String customName) throws Exception {
        // Removing from map
        customItems.remove(customName);

        // Removing from file
        customItems.getItemsConfig().set(customName, null);
        customItems.getItemsConfig().save(customItems.getItemsFile());
    }

    @NotNull
    public Map<UUID, Long> getPickupCooldowns() {
        return pickupCooldowns;
    }

    @NotNull
    public CustomItems getCustomItems() {
        return customItems;
    }

    @NotNull
    public Blacklist getBlacklist() {
        return blacklist;
    }

    @NotNull
    public Whitelist getWhitelist() {
        return whitelist;
    }

}
