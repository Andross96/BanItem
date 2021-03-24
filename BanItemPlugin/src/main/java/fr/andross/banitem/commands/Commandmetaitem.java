/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2021 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your action) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.andross.banitem.commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.actions.BanDataType;
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.utils.Chat;
import fr.andross.banitem.utils.Utils;
import fr.andross.banitem.utils.list.ListType;
import fr.andross.banitem.utils.list.Listable;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sub command meta item
 * @version 3.1
 * @author Andross
 */
public class Commandmetaitem extends BanCommand {

    public Commandmetaitem(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Checking permission
        if (!sender.hasPermission("banitem.command.metaitem")) {
            message(getNoPermMessage());
            return;
        }

        if (args.length < 2) { // Showing help
            header("&6&lMetaItems - Help");
            message("&7Usages:");

            message("&b/bi mi add &3<name> [actions] [message]");
            message("&7 >> Will save and ban the item (including meta)");
            message("&7 >> currently in your hand");
            message("&7 >> in your current world.");

            message("&b/bi mi get &3<name>");
            message("&7 >> Will give you the item (with meta)");
            message("&7 >> in your inventory.");

            message("&b/bi mi list");
            message("&7 >> Displays a list of meta");
            message("&7 >> item names saved.");

            message("&b/bi mi remove &3<name>");
            message("&7 >> Will remove & unban the metaitem");
            return;
        }

        final String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "add": { // /bi mi add <name> <actions> [message]
                // Not player?
                if (!(sender instanceof Player)) {
                    message("Command IG only.");
                    return;
                }

                if (args.length < 3) { // Showing help
                    header("&6&lMetaItems - Add");
                    message("&b/bi mi add &3<name> [actions] [message]");
                    message("&7 >> Will save and ban the item (with meta)");
                    message("&7 >> currently in your hand.");
                    message("&7 >> Actions can be multiple separated with commas.");
                    return;
                }

                // Getting player item in hand
                final Player p = (Player) sender;
                final ItemStack item = Utils.getItemInHand(p);

                // Only saving the custom item?
                final String name = args[2];
                if (args.length < 4) {
                    header("&6&lMetaItems - Add");
                    try {
                        pl.getApi().addMetaItem(name, item);
                        message("&aMeta item successfully saved with name &2" + name + "&a.");
                    } catch (final Exception e) {
                        e.printStackTrace();
                        message("&cError during saving. Check the console for more info.");
                    }
                    return;
                }

                final List<String> actionsNames = Listable.getSplittedList(args[3]);
                final List<BanAction> actions = Listable.getList(ListType.ACTION, actionsNames, null);
                if (actions.isEmpty()) {
                    header("&6&lMetaItems - Add");
                    message("&cInvalid actions entered: &e" + args[3]);
                    message("&7 >> Valid actions: &o" + Arrays.stream(BanAction.values()).map(BanAction::getName).collect(Collectors.joining(",", "", "&7.")));
                    return;
                }

                // Have a message?
                final BanActionData actionData = new BanActionData();

                // Have a message?
                if (args.length > 4) {
                    final StringBuilder sb = new StringBuilder();
                    for (int i = 4; i < args.length; i++) {
                        if (i > 4) sb.append(" ");
                        sb.append(args[i]);
                    }
                    if (sb.length() > 0) actionData.getMap().put(BanDataType.MESSAGE, Collections.singletonList(Chat.color(sb.toString())));
                }

                final Map<BanAction, BanActionData> actionsData = new EnumMap<>(BanAction.class);
                actions.forEach(ba -> actionsData.put(ba, actionData));

                // Adding custom item name
                try {
                    pl.getBanDatabase().addMetaItem(name, item);
                } catch (final Exception e) {
                    e.printStackTrace();
                    message("&cUnable to save metaitems.yml file.");
                    message("&cCheck the console for more information.");
                    return;
                }

                // Adding in blacklist
                pl.getApi().addToBlacklist(new BannedItem(item), actionsData, p.getWorld());
                pl.getListener().load(sender);
                message("&aThis item &e" + name + "&a is now successfully banned for world &2" + p.getWorld().getName() + "&a.");
                message("&7&oPlease note that you probably have the bypass");
                message("&7&opermission, so the ban may not apply to you.");
                return;
            }

            case "get": {
                // Not player?
                if (!(sender instanceof Player)) {
                    message("Command IG only.");
                    return;
                }

                if (args.length < 3) { // Showing help
                    header("&6&lMetaItems - Get");
                    message("&b/bi mi get &3<name>");
                    message("&7 >> Will give you the item (with meta)");
                    message("&7 >> in your inventory.");
                    return;
                }

                final BannedItem bi = pl.getBanDatabase().getCustomItems().get(args[2]);
                if (bi == null) {
                    header("&6&lMetaItems - Get");
                    message("&cUnknown meta item &e" + args[2] + "&c.");
                    return;
                }

                header("&6&lMetaItems - Get");
                if (((Player) sender).getInventory().addItem(bi.toItemStack()).size() > 0)
                    message("&cCan not get the meta item, your inventory is full.");
                else
                    message("&aSuccessfully received &e" + args[2] + "&a.");
                return;
            }

            case "remove": {
                if (args.length < 3) { // Showing help
                    header("&6&lMetaItems - Remove");
                    message("&b/bi mi remove &3<name>");
                    message("&7 >> Will remove & unban the item");
                    message("&7 >> with this meta name.");
                    return;
                }

                // Checking variables
                final String customName = args[2];

                // Checking if exists
                if (!pl.getBanDatabase().getCustomItems().containsKey(customName)) {
                    header("&6&lMetaItems - Remove");
                    message("&cThere is no custom item named &e" + customName + "&c.");
                    return;
                }

                // Removing custom item
                header("&6&lMetaItems - Remove");
                try {
                    pl.getBanDatabase().removeMetaItem(customName);
                    pl.getListener().load(sender);
                    message("&aMeta item &e" + customName + "&a removed.");
                } catch (Exception e) {
                    e.printStackTrace();
                    message("&cUnable to save metaitems.yml file.");
                    message("&cCheck the console for more information.");
                }
                return;
            }

            case "list": {
                final List<String> items = new ArrayList<>(pl.getBanDatabase().getMetaItems().keySet());

                if (items.isEmpty()) {
                    header("&6&lMetaItems - List");
                    message("&7There is no custom item added yet.");
                    return;
                }

                header("&6&lMetaItems - List");
                message("&aMeta items: " + (items.stream().map(s -> ChatColor.GOLD + s + ChatColor.GRAY).collect(Collectors.joining(",", "", "&7."))));
            }
        }
    }

    @Nullable
    @Override
    public List<String> runTab() {
        if (args.length == 2)
            return StringUtil.copyPartialMatches(args[1], Arrays.asList("add", "get", "list", "remove"), new ArrayList<>());
        else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("list")) return Collections.emptyList();
            return StringUtil.copyPartialMatches(args[2], pl.getBanDatabase().getMetaItems().keySet(), new ArrayList<>());
        } else if (args.length == 4 && args[1].equalsIgnoreCase("add"))
            return Collections.singletonList("action");
        else if (args.length > 4 && args[1].equalsIgnoreCase("add"))
            return Collections.singletonList("message");
        return Collections.emptyList();
    }
}
