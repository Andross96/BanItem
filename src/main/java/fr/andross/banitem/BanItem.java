package fr.andross.banitem;

import fr.andross.banitem.Commands.BanCommand;
import fr.andross.banitem.Utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BanItem extends JavaPlugin {
    private static BanItem instance;
    private BanDatabase db;
    private BanItemAPI api;
    private boolean v12OrMore, v9OrMore, v8OrMore;

    @Override
    public void onEnable() {
        instance = this;
        // Checking Bukkit version
        v12OrMore = Bukkit.getBukkitVersion().matches("(1\\.12)(.*)|(1\\.13)(.*)|(1\\.14)(.*)|(1\\.15)(.*)");
        v9OrMore = v12OrMore || Bukkit.getBukkitVersion().matches("(1\\.9)(.*)|(1\\.10)(.*)|(1\\.11)(.*)");
        v8OrMore = v9OrMore || Bukkit.getBukkitVersion().matches("(1\\.8)(.*)");

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

    public void load(@NotNull final CommandSender sender) {
        // (re)Loading config
        saveDefaultConfig();
        reloadConfig();

        // (re)Loading database
        db = new BanDatabase(sender);

        // (re)Loading listeners
        BanListener.reloadListeners(this);

        // Result
        sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &2Successfully loaded &e" + db.getBlacklist().getTotal() + "&2 blacklisted & &e" + db.getWhitelist().getTotal() + "&2 whitelisted item(s)."));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            String commandName = args[0].toLowerCase();
            if (commandName.equals("ci")) commandName = "customitem";
            final BanCommand bc = (BanCommand) Class.forName("fr.andross.banitem.Commands.Command" + commandName).getDeclaredConstructor(this.getClass(), CommandSender.class, String[].class).newInstance(this, sender, args);
            bc.run();
        } catch (Exception e) {
            // Permission?
            if (!sender.hasPermission("banitem.command.help")) {
                final String message = getConfig().getString("no-permission");
                if (message != null) sender.sendMessage(Chat.color(message));
                return true;
            }

            // Help messages
            sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &7Use /banitem &3check&7 [delete] to check if any player has a blacklisted item."));
            sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &7Use /banitem &3customitem&7 to add/remove/list custom items."));
            sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &7Use /banitem &3info&7 to get info about your item in hand."));
            sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &7Use /banitem &3reload&7 to reload the config."));
            sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &2Version: &ev" + getDescription().getVersion()));
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String alias, @NotNull final String[] args) {
        final List<String> list = new ArrayList<>();

        if (!sender.hasPermission("banitem.command.help")) return list;

        if (args.length == 1) {
            list.add("check");
            list.add("customitem");
            list.add("info");
            list.add("reload");
            return StringUtil.copyPartialMatches(args[args.length - 1], list, new ArrayList<>());
        }

        try {
            String commandName = args[0].toLowerCase();
            if (commandName.equals("ci")) commandName = "customitem";
            final BanCommand bc = (BanCommand) Class.forName("fr.andross.banitem.Commands.Command" + commandName).getDeclaredConstructor(this.getClass(), CommandSender.class, String[].class).newInstance(this, sender, args);
            return bc.runTab();
        } catch (final Exception e) {
            return list;
        }
    }

    @NotNull
    public static BanItem getInstance() {
        return instance;
    }

    @NotNull
    public BanDatabase getBanDatabase() { return db; }

    @NotNull
    public BanItemAPI getApi() { return api; }

    public boolean isv9OrMore() { return v9OrMore; }

    public boolean isv12OrMore() { return v12OrMore; }

    public boolean isv8OrMore() {
        return v8OrMore;
    }
}