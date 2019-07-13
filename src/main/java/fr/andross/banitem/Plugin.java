package fr.andross.banitem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        final String noperm = bi.color(getConfig().getString("no-permission", "&cYou do not have permission."));

        if(!sender.hasPermission("banitem.command")) {
            sender.sendMessage(noperm);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) { // Reload command
            if (!sender.hasPermission("banitem.command.reload")) {
                sender.sendMessage(noperm);
                return true;
            }

            bi.load();
            sender.sendMessage(bi.color("&c[&e&lBanItem&c] &2Config reloaded."));
        } else if(args.length > 0 && args[0].equalsIgnoreCase("info")) { // Item info command
            if (!(sender instanceof Player)) {
                sender.sendMessage("Command IG only.");
                return true;
            }

            if (!sender.hasPermission("banitem.command.info")) {
                sender.sendMessage(noperm);
                return true;
            }

            final Player p = (Player) sender;
            final String m = bi.isV9OrMore() ? p.getInventory().getItemInMainHand().getType().name().toLowerCase() : p.getInventory().getItemInHand().getType().name().toLowerCase();
            sender.sendMessage(bi.color("&c[&e&lBanItem&c] &7Material name: &e" + m));
            sender.sendMessage(bi.color("&c[&e&lBanItem&c] &7Permission: &ebanitem.bypass." + p.getWorld().getName().toLowerCase() + "." + m));
        } else { // Help message
            sender.sendMessage(bi.color("&c[&e&lBanItem&c] &2Version: &ev" + getDescription().getVersion()));
            sender.sendMessage(bi.color("&c[&e&lBanItem&c] &7Use /banitem &3info&7 to get info about your item in hand."));
            sender.sendMessage(bi.color("&c[&e&lBanItem&c] &7Use /banitem &3reload&7 to reload the config."));
        }
        return true;
    }
}