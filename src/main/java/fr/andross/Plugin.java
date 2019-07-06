package fr.andross;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {
    private BanItem bi;

    @Override
    public void onEnable() {
        // Loading plugin
        bi = new BanItem(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final String noperm = color(getConfig().getString("no-permission", "&cYou do not have permission."));

        if(!sender.hasPermission("banitem.command")) {
            sender.sendMessage(noperm);
            return true;
        }

        // Reload command
        if(args.length > 0 && args[0].equalsIgnoreCase("reload")){
            if(!sender.hasPermission("banitem.command.reload")) {
                sender.sendMessage(noperm);
                return true;
            }

            bi.load();
            sender.sendMessage(color("&c[&e&lBanItem&c] &2Config reloaded."));
            return true;
        }

        // Help message
        sender.sendMessage(color("&c[&e&lBanItem&c] &2Version: &ev" + getDescription().getVersion()));
        sender.sendMessage(color("&c[&e&lBanItem&c] &7Use /banitem &3reload&7 to reload the config."));
        return true;
    }

    public String color(final String text) { return ChatColor.translateAlternateColorCodes('&', text); }
}