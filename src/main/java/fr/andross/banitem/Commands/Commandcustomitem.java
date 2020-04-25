package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.BanListener;
import fr.andross.banitem.Options.BanDataType;
import fr.andross.banitem.Options.BanOption;
import fr.andross.banitem.Options.BanOptionData;
import fr.andross.banitem.Utils.Ban.BanVersion;
import fr.andross.banitem.Utils.Ban.BannedItem;
import fr.andross.banitem.Utils.General.Listable;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sub command custom item
 * @version 2.0
 * @author Andross
 */
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
            header("&6&lCustomItems - Help");
            message("&7Usages:");

            message("&b/bi ci add &3<name> [option] [message]");
            message("&7 >> Will ban the item (with meta)");
            message("&7 >> in your hand.");

            message("&b/bi ci get &3<name>");
            message("&7 >> Will get the item (with meta)");
            message("&7 >> in your inventory.");

            message("&b/bi ci list");
            message("&7 >> Displays a list of custom");
            message("&7 >> item names.");

            message("&b/bi ci remove &3<name>");
            message("&7 >> Will remove & unban the item");
            message("&7 >> with this custom name.");
            return;
        }

        final String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "add": { // /bi ci add <name> <option> [message]
                // Not player?
                if (!(sender instanceof Player)) {
                    message("Command IG only.");
                    return;
                }

                if (args.length < 3) { // Showing help
                    header("&6&lCustomItems - Add");
                    message("&b/bi ci add &3<name> [option] [message]");
                    message("&7 >> Will ban the item (with meta)");
                    message("&7 >> in your hand.");
                    message("&7 >> Options can be multiple with commas.");
                    return;
                }

                // Getting player item in hand
                final Player p = (Player) sender;
                final ItemStack item = BanVersion.v9OrMore ? p.getInventory().getItemInMainHand() : p.getInventory().getItemInHand();

                // Only saving the custom item?
                final String name = args[2];
                if (args.length < 4) {
                    header("&6&lCustomItems - Add");
                    try {
                        pl.getApi().addCustomItem(name, item);
                        message("&2Custom item successfully saved with name &a" + name + "&2.");
                    } catch (final Exception e) {
                        e.printStackTrace();
                        message("&cError during saving. Check the console for more info.");
                    }
                    return;
                }

                final List<String> optionsNames = pl.getUtils().getSplittedList(args[3]);
                final List<BanOption> options = pl.getUtils().getList(Listable.Type.OPTION, optionsNames, null);
                if (options.isEmpty()) {
                    header("&6&lCustomItems - Add");
                    message("&cInvalid options set: &e" + args[3]);
                    message("&7 >> Valid options: &o" + Arrays.stream(BanOption.values()).map(BanOption::getName).collect(Collectors.joining(",")));
                    return;
                }

                // Have a message?
                final List<String> messages = new ArrayList<>();
                if (args.length > 4) {
                    final StringBuilder sb = new StringBuilder();
                    for (int i = 4; i < args.length; i++){
                        if (i > 4) sb.append(" ");
                        sb.append(args[i]);
                    }
                    if (sb.length() > 0) messages.add(pl.getUtils().color(sb.toString()));
                }

                final BanOptionData optionData = new BanOptionData();
                if (!messages.isEmpty()) optionData.put(BanDataType.MESSAGE, messages);
                final Map<BanOption, BanOptionData> optionsData = new HashMap<>();
                for (final BanOption bo : options) optionsData.put(bo, optionData);

                // Adding custom item name
                try {
                    pl.getBanDatabase().addCustomItem(name, item);
                } catch (final Exception e) {
                    e.printStackTrace();
                    message("&cUnable to save items.yml file");
                    return;
                }

                // Adding in blacklist
                pl.getApi().addToBlacklist(new BannedItem(item, true), optionsData, p.getWorld());
                BanListener.loadListeners();
                message("&2This item '&a" + name + "&2' is now successfully banned for world &a" + p.getWorld().getName() + "&2.");
                message("&7&oNote that you surely have the bypass permission, so the ban may not apply to you!");
                return;
            }

            case "get": {
                // Not player?
                if (!(sender instanceof Player)) {
                    message("Command IG only.");
                    return;
                }

                if (args.length < 3) { // Showing help
                    header("&6&lCustomItems - Get");
                    message("&b/bi ci get &3<name>");
                    message("&7 >> Will get the item (with meta)");
                    message("&7 >> in your inventory.");
                    return;
                }

                final BannedItem bi = pl.getBanDatabase().getCustomItems().get(args[2]);
                if (bi == null) {
                    header("&6&lCustomItems - Get");
                    message("&cUnknown custom item &e" + args[2] + "&c.");
                    return;
                }

                header("&6&lCustomItems - Get");
                if (((Player) sender).getInventory().addItem(bi.toItemStack().clone()).size() > 0)
                    message("&cCan not get the custom item, your inventory is full.");
                else
                    message("&2Successfully received &a" + args[2] + "&2.");
                return;
            }

            case "remove": {
                if (args.length < 3) { // Showing help
                    header("&6&lCustomItems - Remove");
                    message("&b/bi ci remove &3<name>");
                    message("&7 >> Will remove & unban the item");
                    message("&7 >> with this custom name.");
                    return;
                }

                // Checking variables
                final String customName = args[2];

                // Checking if exists
                if (!pl.getBanDatabase().getCustomItems().containsKey(customName)) {
                    header("&6&lCustomItems - Remove");
                    message("&cThere is no custom item named &e" + customName + "&c.");
                    return;
                }

                // Removing custom item
                header("&6&lCustomItems - Remove");
                try {
                    pl.getBanDatabase().removeCustomItem(customName);
                    BanListener.loadListeners();
                    message("&2Custom item &e" + customName + "&2 removed.");
                } catch (Exception e) {
                    e.printStackTrace();
                    message("&cUnable to remove custom item. Check the console for more information.");
                }
                return;
            }

            case "list": {
                final List<String> items = new ArrayList<>(pl.getBanDatabase().getCustomItems().keySet());

                if (items.isEmpty()) {
                    header("&6&lCustomItems - List");
                    message("&7There is no custom item added yet.");
                    return;
                }

                header("&6&lCustomItems - List");
                message("&2Custom items: " + (items.stream().map(s -> ChatColor.GOLD + s + ChatColor.GRAY).collect(Collectors.joining(",")) + "&7."));
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
            list.add("option");
            return list;
        } else if (args.length > 4 && args[1].equalsIgnoreCase("add")) {
            list.add("message");
            return list;
        }

        return list;
    }
}
