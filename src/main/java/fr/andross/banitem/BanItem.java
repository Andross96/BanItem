package fr.andross.banitem;

import fr.andross.banitem.Commands.BanCommand;
import fr.andross.banitem.Maps.Blacklist;
import fr.andross.banitem.Maps.Whitelist;
import fr.andross.banitem.Utils.BanDatabase;
import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BanUtils;
import fr.andross.banitem.Utils.BannedItem;
import fr.andross.banitem.Maps.WhitelistWorld;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class BanItem extends JavaPlugin implements BanItemAPI {
    private BanDatabase db;
    private boolean v12OrMore, v9OrMore;
    private final BanUtils utils = new BanUtils();

    @Override
    public void onEnable() {
        // Checking Bukkit version
        v12OrMore = getServer().getBukkitVersion().matches("(1\\.12)(.*)|(1\\.13)(.*)|(1\\.14)(.*)");
        v9OrMore = v12OrMore || getServer().getBukkitVersion().matches("(1\\.9)(.*)|(1\\.10)(.*)|(1\\.11)(.*)");

        // Loading plugin after worlds
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> { if(isEnabled()) load(getServer().getConsoleSender()); });
    }

    private void load(final CommandSender sender) {
        // (re)Loading config
        saveDefaultConfig();
        reloadConfig();

        // (re)Loading database
        db = new BanDatabase(this, sender);

        // (re)Loading listeners
        utils.reloadListeners(this);

        sender.sendMessage(color("&c[&e&lBanItem&c] &2Successfully loaded &e" + db.getBlacklist().getTotal() + "&2 blacklisted & &e" + db.getWhitelist().getTotal() + "&2 whitelisted item(s)."));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            String commandName = args[0].toLowerCase();
            if (commandName.equals("ci")) commandName = "customitem";
            BanCommand bc = (BanCommand) Class.forName("fr.andross.banitem.Commands.Command" + commandName).getDeclaredConstructor(this.getClass(), CommandSender.class, String[].class).newInstance(this, sender, args);
            bc.run();
        } catch (Exception e) {
            // Permission?
            if (!sender.hasPermission("banitem.command.help")) {
                sender.sendMessage(color(getConfig().getString("no-permission", "&cYou do not have permission.")));
                return true;
            }

            // Help messages
            sender.sendMessage(color("&c[&e&lBanItem&c] &7Use /banitem &3check&7 [delete] to check if any player has a blacklisted item."));
            sender.sendMessage(color("&c[&e&lBanItem&c] &7Use /banitem &3customitem&7 to add/remove/list custom items."));
            sender.sendMessage(color("&c[&e&lBanItem&c] &7Use /banitem &3info&7 to get info about your item in hand."));
            sender.sendMessage(color("&c[&e&lBanItem&c] &7Use /banitem &3reload&7 to reload the config."));
            sender.sendMessage(color("&c[&e&lBanItem&c] &2Version: &ev" + getDescription().getVersion()));
        }
        return true;
    }

    @NotNull
    public String color(final String text) { return ChatColor.translateAlternateColorCodes('&', text); }
    public boolean isv9OrMore() { return v9OrMore; }
    public boolean isv12OrMore() { return v12OrMore; }

    // API \\
    @NotNull
    public BanDatabase getDatabase() { return db; }

    @NotNull
    public fr.andross.banitem.Maps.CustomItems getCustomItems() { return db.getCustomItems(); }

    @NotNull
    public Blacklist getBlacklist() { return db.getBlacklist(); }

    @NotNull
    public Whitelist getWhitelist() { return db.getWhitelist(); }

    @NotNull
    public fr.andross.banitem.Utils.BanUtils getUtils() { return utils; }

    public void reload(CommandSender sender) { load(sender); }

    @Nullable
    public Map<BanOption, String> getBlacklist(@NotNull ItemStack item, @NotNull String world) {
        final Map<BannedItem, Map<BanOption, String>> map = db.getBlacklist().get(world);
        if (map == null) return null;
        return map.get(new BannedItem(item));
    }

    @Nullable
    public WhitelistWorld getWhitelist(@NotNull String world) {
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
            db.getWhitelist().addNewException(w, null, null, bannedItem, options);
            getConfig().set("whitelist." + w + "." + item.getType().name().toLowerCase(), option);
        }
        saveConfig();
    }

    public void removeFromWhitelist(@NotNull ItemStack item, @NotNull String... worlds) {
        final BannedItem bannedItem = new BannedItem(item);

        for (String w : worlds) {
            final WhitelistWorld ww = db.getWhitelist().get(w);
            if (ww == null) continue;
            ww.getWhitelist().remove(bannedItem);

            getConfig().set("whitelist." + w + "." + item.getType().name().toLowerCase(), null);
        }
        saveConfig();
    }

    public BanOption getBanOption(@NotNull String option) {
        try {
            return BanOption.valueOf(option.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}