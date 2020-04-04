package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Utils.BanUtils;
import fr.andross.banitem.Utils.BannedItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

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
            message("&c[&e&lBanItem&c] &7/banitem &bci get &3<name>");
            message("&c[&e&lBanItem&c] &7/banitem &bci list");
            message("&c[&e&lBanItem&c] &7/banitem &bci remove &3<name>");
            return;
        }

        final String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "add": {
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
                final ItemStack customItem = BanUtils.v9OrMore ? ((Player)sender).getInventory().getItemInMainHand() : ((Player)sender).getInventory().getItemInHand();
                if (customItem.getType() == Material.AIR) {
                    message("&c[&e&lBanItem&c] &cYou must have a valid item in your hand.");
                    return;
                }

                // Checking if already exists
                if (pl.getBanDatabase().getCustomItems().containsKey(customName) && !(args.length > 3 && args[3].equalsIgnoreCase("force"))) {
                    message("&c[&e&lBanItem&c] &cA custom item named &e" + customName + "&c already exists. Add &2force&c argument to replace it.");
                    return;
                }

                // Adding custom item
                try {
                    pl.getBanDatabase().addCustomItem(customName, customItem);
                    message("&c[&e&lBanItem&c] &2Custom item &e" + customName + "&2 added.");
                } catch (Exception e) {
                    e.printStackTrace();
                    message("&c[&e&lBanItem&c] &cUnable to save custom item. Check the console for more information.");
                }
                return;
            }

            case "get": {
                // Console?
                if (!(sender instanceof Player)) {
                    message("Command IG only.");
                    return;
                }

                if (args.length < 3) { // Showing help
                    message("&c[&e&lBanItem&c] &7/banitem &bci get &3<name>");
                    return;
                }

                final BannedItem bi = pl.getBanDatabase().getCustomItems().get(args[2]);
                if (bi == null) {
                    message("&c[&e&lBanItem&c] &cUnknown custom item &e" + args[2] + "&c.");
                    return;
                }

                if (((Player) sender).getInventory().addItem(bi.toItemStack().clone()).size() > 0)
                    message("&c[&e&lBanItem&c] &cCan not get the custom item, your inventory is full.");
                else
                    message("&c[&e&lBanItem&c] &2Successfully recieved &a" + args[2] + "&2.");
                return;
            }

            case "remove": {
                if (args.length < 3) { // Showing help
                    message("&c[&e&lBanItem&c] &7/banitem &bci remove &3<name>");
                    return;
                }

                // Checking variables
                final String customName = args[2];

                // Checking if exists
                if (!pl.getBanDatabase().getCustomItems().containsKey(customName)) {
                    message("&c[&e&lBanItem&c] &cThere is no custom item named &e" + customName + "&c.");
                    return;
                }

                // Removing custom item
                try {
                    pl.getBanDatabase().removeCustomItem(customName);
                    message("&c[&e&lBanItem&c] &2Custom item &e" + customName + "&2 removed.");
                } catch (Exception e) {
                    e.printStackTrace();
                    message("&c[&e&lBanItem&c] &cUnable to remove custom item. Check the console for more information.");
                }
                return;
            }

            case "list": {
                final List<String> items = new ArrayList<>(pl.getBanDatabase().getCustomItems().keySet());
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

    @Nullable
    @Override
    public List<String> runTab() {
        final List<String> list = new ArrayList<>();

        if (args.length == 2) {
            list.add("add");
            list.add("get");
            list.add("list");
            list.add("remove");
            return StringUtil.copyPartialMatches(args[args.length - 1], list, new ArrayList<>());
        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("list")) return list;
            return StringUtil.copyPartialMatches(args[args.length - 1], pl.getBanDatabase().getCustomItems().keySet(), new ArrayList<>());
        } else if (args.length == 4 && args[1].equalsIgnoreCase("add")) {
            list.add("force");
            return list;
        }

        return list;
    }
}
