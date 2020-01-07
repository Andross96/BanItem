package fr.andross.banitem;

import fr.andross.banitem.Commands.BanCommand;
import fr.andross.banitem.Utils.BanUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class BanItem extends JavaPlugin {
    private BanDatabase db;
    private BanItemAPI api;
    private boolean v12OrMore, v9OrMore;

    @Override
    public void onEnable() {
        // Checking Bukkit version
        v12OrMore = Bukkit.getBukkitVersion().matches("(1\\.12)(.*)|(1\\.13)(.*)|(1\\.14)(.*)|(1\\.15)(.*)");
        v9OrMore = v12OrMore || Bukkit.getBukkitVersion().matches("(1\\.9)(.*)|(1\\.10)(.*)|(1\\.11)(.*)");

        // Loading API
        this.api = new BanItemAPI(this);

        // Loading plugin after worlds
        Bukkit.getScheduler().runTaskLater(this, () -> { if(isEnabled()) load(Bukkit.getConsoleSender()); }, 20L);
    }

    @Override
    public void onDisable() {
        // Cleaning up'
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
    }

    public void load(final CommandSender sender) {
        // (re)Loading config
        saveDefaultConfig();
        reloadConfig();

        // (re)Loading database
        db = new BanDatabase(this, sender);

        // (re)Loading listeners
        BanUtils.reloadListeners(this);

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

    public boolean isv9OrMore() { return v9OrMore; }

    public boolean isv12OrMore() { return v12OrMore; }

    @NotNull
    public String color(final String text) { return ChatColor.translateAlternateColorCodes('&', text); }

    @NotNull
    public BanDatabase getDb() { return db; }

    @NotNull
    public BanItemAPI getApi() { return api; }
}