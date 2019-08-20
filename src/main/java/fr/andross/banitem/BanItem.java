package fr.andross.banitem;

import fr.andross.banitem.Utils.BanDatabase;
import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BannedItem;
import fr.andross.banitem.Utils.WhitelistedWorld;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BanItem extends JavaPlugin implements BanItemAPI {
    private BanDatabase db;
    private boolean v12OrMore, v9OrMore;

    @Override
    public void onEnable() {
        // Checking Bukkit version
        v12OrMore = getServer().getBukkitVersion().matches("(1\\.12)(.*)|(1\\.13)(.*)|(1\\.14)(.*)");
        v9OrMore = v12OrMore || getServer().getBukkitVersion().matches("(1\\.9)(.*)|(1\\.10)(.*)|(1\\.11)(.*)");

        // Loading plugin
        load(getServer().getConsoleSender());
    }

    private void load(final CommandSender sender) {
        // (re)Loading config
        saveDefaultConfig();
        reloadConfig();

        // (re)Loading database
        db = new BanDatabase(this);
        db.load(sender);
        // Checking maps
        final Set<BanOption> blacklist = db.getBlacklistOptions();
        final boolean whitelist = db.isWhitelistEnabled();

        // (re)Loading listeners
        HandlerList.unregisterAll(this);
        final Listener l = new Listener() { };
        final EventPriority ep = EventPriority.HIGHEST;

        // Registering listeners
        // Adding listeners if option is setted
        if (whitelist || blacklist.contains(BanOption.PLACE) || blacklist.contains(BanOption.BREAK) || blacklist.contains(BanOption.USE) || blacklist.contains(BanOption.INTERACT)) {
            // >=1.9: It has EquipmentSlot
            // <1.9: it doesn't
            getServer().getPluginManager().registerEvent(PlayerInteractEvent.class, l, ep, (li, e) -> {
                final PlayerInteractEvent event = (PlayerInteractEvent) e;

                // Checking placing a banned item?
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.isBlockInHand()) {
                    if (db.isBanned(event.getPlayer(), event.getItem(), BanOption.PLACE)) {
                        event.setCancelled(true);
                        return;
                    }
                }

                // Checking breaking a banned item?
                if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
                    // Breaking?
                    if (db.isBanned(event.getPlayer(), new ItemStack(event.getClickedBlock().getType()), BanOption.BREAK)) {
                        event.setCancelled(true);
                        return;
                    }
                }

                // Checking interacting with a banned item?
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
                    if (db.isBanned(event.getPlayer(), new ItemStack(event.getClickedBlock().getType()), BanOption.INTERACT)) {
                        event.setCancelled(true);
                        return;
                    }
                }

                // Checking using an item?
                if (event.getItem() != null && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    if (db.isBanned(event.getPlayer(), event.getItem(), BanOption.USE)) event.setCancelled(true);
                }
            }, this);
        }

        if (blacklist.contains(BanOption.INVENTORY)) {
            getServer().getPluginManager().registerEvent(InventoryClickEvent.class, l, ep, (li, e) -> {
                final InventoryClickEvent event = (InventoryClickEvent) e;
                final ItemStack item = event.getCurrentItem();
                if(item == null) return;
                if (db.isBanned((Player)event.getWhoClicked(), item, BanOption.INVENTORY)) event.setCancelled(true);
            }, this);
        }
        if (blacklist.contains(BanOption.DROP) || whitelist) {
            getServer().getPluginManager().registerEvent(PlayerDropItemEvent.class, l, ep, (li, e) -> {
                final PlayerDropItemEvent event = (PlayerDropItemEvent) e;
                if (db.isBanned(event.getPlayer(), event.getItemDrop().getItemStack(), BanOption.DROP))
                    event.setCancelled(true);
            }, this);
        }

        if (blacklist.contains(BanOption.PICKUP) || whitelist) {
            // Pickup cooldown map clearing
            getServer().getPluginManager().registerEvent(PlayerQuitEvent.class, l, ep, (li, e) -> { db.getPickupCooldowns().remove(((PlayerQuitEvent) e).getPlayer().getUniqueId());
            }, this);

            // >=1.12: EntityPickupItemEvent
            // <1.12: PlayerPickupItemEvent
            final EventExecutor ee;
            final Class<? extends Event> c;
            if (v12OrMore) {
                c = org.bukkit.event.entity.EntityPickupItemEvent.class;
                ee = (li, e) -> {
                    final org.bukkit.event.entity.EntityPickupItemEvent event = (org.bukkit.event.entity.EntityPickupItemEvent) e;
                    if (!(event.getEntity() instanceof Player)) return;
                    if (db.isBanned((Player) event.getEntity(), event.getItem().getItemStack(), BanOption.PICKUP)) event.setCancelled(true);
                };
            } else {
                c = org.bukkit.event.player.PlayerPickupItemEvent.class;
                ee = (li, e) -> {
                    final org.bukkit.event.player.PlayerPickupItemEvent event = (org.bukkit.event.player.PlayerPickupItemEvent) e;
                    if (db.isBanned(event.getPlayer(), event.getItem().getItemStack(), BanOption.PICKUP)) event.setCancelled(true);
                };
            }
            getServer().getPluginManager().registerEvent(c, l, ep, ee, this);
        }

        sender.sendMessage(color("&c[&e&lBanItem&c] &2Successfully loaded &e" + db.getBlacklist().getTotal() + "&2 blacklisted & &e" + db.getWhitelist().getTotal() + "&2 whitelisted item(s)."));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final String noperm = color(getConfig().getString("no-permission", "&cYou do not have permission."));

        // Reload command?
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("banitem.command.reload")) {
                sender.sendMessage(noperm);
                return true;
            }

            load(sender);
            return true;
        }

        // Iteminfo command?
        if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
            // Console?
            if (!(sender instanceof Player)) {
                sender.sendMessage("Command IG only.");
                return true;
            }

            // Permission?
            if (!sender.hasPermission("banitem.command.info")) {
                sender.sendMessage(noperm);
                return true;
            }

            // Showing item info
            final Player p = (Player) sender;
            final String m = v9OrMore ? p.getInventory().getItemInMainHand().getType().name().toLowerCase() : p.getInventory().getItemInHand().getType().name().toLowerCase();
            sender.sendMessage(color("&c[&e&lBanItem&c] &7Material name: &e" + m));
            sender.sendMessage(color("&c[&e&lBanItem&c] &7Permission: &ebanitem.bypass." + p.getWorld().getName().toLowerCase() + "." + m));
            return true;
        }

        // Customitem command?
        if(args.length > 0 && args[0].matches("(?i)customitem|ci")) {
            // Checking permission
            if (!sender.hasPermission("banitem.command.customitem")) {
                sender.sendMessage(noperm);
                return true;
            }

            if (args.length < 2) { // Showing help
                sender.sendMessage(color("&c[&e&lBanItem&c] &7Usage:"));
                sender.sendMessage(color("&c[&e&lBanItem&c] &7/banitem &bci add &3<name> &3[force]"));
                sender.sendMessage(color("&c[&e&lBanItem&c] &7/banitem &bci remove &3<name>"));
                sender.sendMessage(color("&c[&e&lBanItem&c] &7/banitem &bci list"));
                return true;
            }

            // Adding custom item?
            if (args[1].equalsIgnoreCase("add")) {
                // Console?
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Command IG only.");
                    return true;
                }

                if (args.length < 3) { // Showing help
                    sender.sendMessage(color("&c[&e&lBanItem&c] &7/banitem &bci add &3<name> &3[force]"));
                    return true;
                }

                // Checking variables
                final String customName = args[2];
                final ItemStack customItem = v9OrMore ? ((Player)sender).getInventory().getItemInMainHand() : ((Player)sender).getInventory().getItemInHand();
                if (customItem.getType() == Material.AIR) {
                    sender.sendMessage(color("&c[&e&lBanItem&c] &cYou must have a valid item in your hand."));
                    return true;
                }

                // Checking if already exists
                if (db.getCustomItems().containsKey(customName) && !(args.length > 3 && args[3].equalsIgnoreCase("force"))) {
                    sender.sendMessage(color("&c[&e&lBanItem&c] &cA custom item named &e" + customName + "&c already exists. Add &2force&c argument to replace it."));
                    return true;
                }

                // Adding custom item
                try {
                    db.addCustomItem(customName, customItem);
                    sender.sendMessage(color("&c[&e&lBanItem&c] &2Custom item &e" + customName + "&2 added."));
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(color("&c[&e&lBanItem&c] &cUnable to save custom item. Check the console for more information."));
                }
                return true;
            }

            // Removing custom item?
            if (args[1].equalsIgnoreCase("remove")) {
                if (args.length < 3) { // Showing help
                    sender.sendMessage(color("&c[&e&lBanItem&c] &7/banitem &bci remove &3<name>"));
                    return true;
                }

                // Checking variables
                final String customName = args[2];

                // Checking if exists
                if (!db.getCustomItems().containsKey(customName)) {
                    sender.sendMessage(color("&c[&e&lBanItem&c] &cThere is no custom item named &e" + customName + "&c."));
                    return true;
                }

                // Removing custom item
                try {
                    db.removeCustomItem(customName);
                    sender.sendMessage(color("&c[&e&lBanItem&c] &2Custom item &e" + customName + "&2 removed."));
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(color("&c[&e&lBanItem&c] &cUnable to remove custom item. Check the console for more information."));
                }
                return true;
            }

            // Listing custom item?
            if (args[1].equalsIgnoreCase("list")) {
                final List<String> items = new ArrayList<>(db.getCustomItems().keySet());
                if (items.isEmpty()) {
                    sender.sendMessage(color("&c[&e&lBanItem&c] &7There is no custom item created yet."));
                    return true;
                }

                final StringBuilder list = new StringBuilder();
                for(String s : items) list.append(ChatColor.GOLD).append(s).append(ChatColor.GRAY).append(", ");
                sender.sendMessage(color("&c[&e&lBanItem&c] &2Custom items: " + list.toString().substring(0, list.toString().length() - 2) + "&7."));
                return true;
            }

            return true;
        }

        if (!sender.hasPermission("banitem.command.help")) {
            sender.sendMessage(noperm);
            return true;
        }

        // Help message
        sender.sendMessage(color("&c[&e&lBanItem&c] &2Version: &ev" + getDescription().getVersion()));
        sender.sendMessage(color("&c[&e&lBanItem&c] &7Use /banitem &3info&7 to get info about your item in hand."));
        sender.sendMessage(color("&c[&e&lBanItem&c] &7Use /banitem &3customitem&7 to add/remove/list custom items."));
        sender.sendMessage(color("&c[&e&lBanItem&c] &7Use /banitem &3reload&7 to reload the config."));
        return true;
    }

    @NotNull
    public String color(final String text) { return ChatColor.translateAlternateColorCodes('&', text); }

    // API \\
    @NotNull
    public BanDatabase getDatabase() { return db; }

    @NotNull
    public fr.andross.banitem.Maps.CustomItems getCustomItems() { return db.getCustomItems(); }

    @NotNull
    public fr.andross.banitem.Maps.Blacklisted getBlacklist() { return db.getBlacklist(); }

    @NotNull
    public fr.andross.banitem.Maps.Whitelisted getWhitelist() { return db.getWhitelist(); }

    @NotNull
    public fr.andross.banitem.Utils.BanUtils getUtils() { return db.getUtils(); }

    public void reload(CommandSender sender) { load(sender); }

    @Nullable
    public Map<BanOption, String> getBlacklisted(@NotNull ItemStack item, @NotNull String world) {
        final Map<BannedItem, Map<BanOption, String>> map = db.getBlacklist().get(world);
        if (map == null) return null;
        return map.get(new BannedItem(item));
    }

    @Nullable
    public WhitelistedWorld getWhitelisted(@NotNull String world) {
        return db.getWhitelist().get(world);
    }

    public void addCustomItem(@NotNull String name, @NotNull ItemStack item) throws Exception {
        db.addCustomItem(name, item);
    }

    public void removeCustomItem(@NotNull String name) throws Exception {
        db.removeCustomItem(name);
    }

    public boolean containsCustomItem(@NotNull ItemStack item) {
        return db.getCustomItems().getName(new BannedItem(item)) != null;
    }

    public boolean containsCustomItem(@NotNull String name) {
        return db.getCustomItems().containsKey(name);
    }

    public void addToBlacklist(@NotNull ItemStack item, @NotNull Map<BanOption, String> options, @NotNull String... worlds) {
        final BannedItem bannedItem = new BannedItem(item);

        for (String w : worlds) {
            db.getBlacklist().addNewBan(w, bannedItem, options);
            for (Map.Entry<BanOption, String> entry : options.entrySet()) {
                getConfig().set("blacklist." + w + "." + item.getType().name().toLowerCase() + "." + entry.getKey().name().toLowerCase(), entry.getValue());
            }
        }
        saveConfig();
    }

    public void removeFromBlacklist(@NotNull ItemStack item, @NotNull String... worlds) {
        final BannedItem bannedItem = new BannedItem(item);

        for (String w : worlds) {
            final Map<BannedItem, Map<BanOption, String>> map = db.getBlacklist().get(w);
            map.remove(bannedItem);
            db.getBlacklist().put(w, map);
            getConfig().set("blacklist." + w + "." + item.getType().name().toLowerCase(), null);
        }
        saveConfig();
    }

    public void addToWhitelist(@NotNull ItemStack item, @NotNull List<BanOption> options, @NotNull String... worlds) {
        final BannedItem bannedItem = new BannedItem(item);
        final StringBuilder list = new StringBuilder();
        for (BanOption o : options) list.append(o.name().toLowerCase()).append(",");
        String finalList = list.toString();
        final String option = finalList.substring(0, finalList.length() - 1);

        for (String w : worlds) {
            db.getWhitelist().addNewException(w, null, bannedItem, options);
            getConfig().set("whitelist." + w + "." + item.getType().name().toLowerCase(), option);
        }
        saveConfig();
    }

    public void removeFromWhitelist(@NotNull ItemStack item, @NotNull String... worlds) {
        final BannedItem bannedItem = new BannedItem(item);

        for (String w : worlds) {
            final WhitelistedWorld wlw = db.getWhitelist().get(w);
            if (wlw == null) continue;
            wlw.getWhitelisted().remove(bannedItem);

            getConfig().set("whitelist." + w + "." + item.getType().name().toLowerCase(), null);
        }
        saveConfig();
    }



}