package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Commandcustomitem extends BanCommand {

    public Commandcustomitem(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Checking permission
        if (!sender.hasPermission("banitem.command.customitem")) {
            message(getNoPermMessage());
            return;
        }

        if (args.length < 2) { // Showing help
            message("&c[&e&lBanItem&c] &7Usage:");
            message("&c[&e&lBanItem&c] &7/banitem &bci add &3<name> &3[force]");
            message("&c[&e&lBanItem&c] &7/banitem &bci remove &3<name>");
            message("&c[&e&lBanItem&c] &7/banitem &bci list");
            return;
        }

        // Adding custom item?
        if (args[1].equalsIgnoreCase("add")) {
            // Console?
            if (!(sender instanceof Player)) {
                message("Command IG only.");
                return;
            }

            if (args.length < 3) { // Showing help
                message("&c[&e&lBanItem&c] &7/banitem &bci add &3<name> &3[force]");
                return;
            }

            // Checking variables
            final String customName = args[2];
            final ItemStack customItem = pl.isv9OrMore() ? ((Player)sender).getInventory().getItemInMainHand() : ((Player)sender).getInventory().getItemInHand();
            if (customItem.getType() == Material.AIR) {
                message("&c[&e&lBanItem&c] &cYou must have a valid item in your hand.");
                return;
            }

            // Checking if already exists
            if (pl.getDb().getCustomItems().containsKey(customName) && !(args.length > 3 && args[3].equalsIgnoreCase("force"))) {
                message("&c[&e&lBanItem&c] &cA custom item named &e" + customName + "&c already exists. Add &2force&c argument to replace it.");
                return;
            }

            // Adding custom item
            try {
                pl.getDb().addCustomItem(customName, customItem);
                message("&c[&e&lBanItem&c] &2Custom item &e" + customName + "&2 added.");
            } catch (Exception e) {
                e.printStackTrace();
                message("&c[&e&lBanItem&c] &cUnable to save custom item. Check the console for more information.");
            }
            return;
        }

        // Removing custom item?
        if (args[1].equalsIgnoreCase("remove")) {
            if (args.length < 3) { // Showing help
                message("&c[&e&lBanItem&c] &7/banitem &bci remove &3<name>");
                return;
            }

            // Checking variables
            final String customName = args[2];

            // Checking if exists
            if (!pl.getDb().getCustomItems().containsKey(customName)) {
                message("&c[&e&lBanItem&c] &cThere is no custom item named &e" + customName + "&c.");
                return;
            }

            // Removing custom item
            try {
                pl.getDb().removeCustomItem(customName);
                message("&c[&e&lBanItem&c] &2Custom item &e" + customName + "&2 removed.");
            } catch (Exception e) {
                e.printStackTrace();
                message("&c[&e&lBanItem&c] &cUnable to remove custom item. Check the console for more information.");
            }
            return;
        }

        // Listing custom item?
        if (args[1].equalsIgnoreCase("list")) {
            final List<String> items = new ArrayList<>(pl.getDb().getCustomItems().keySet());
            if (items.isEmpty()) {
                message("&c[&e&lBanItem&c] &7There is no custom item created yet.");
                return;
            }

            final StringBuilder list = new StringBuilder();
            for(String s : items) list.append(ChatColor.GOLD).append(s).append(ChatColor.GRAY).append(", ");
            message("&c[&e&lBanItem&c] &2Custom items: " + list.toString().substring(0, list.toString().length() - 2) + "&7.");
        }
    }

}
