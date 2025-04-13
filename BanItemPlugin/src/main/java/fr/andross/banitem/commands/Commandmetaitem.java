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
 *
 * @author Andross
 * @version 3.1.1
 */
public class Commandmetaitem extends BanCommand {

    public Commandmetaitem(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Checking permission
        if (!sender.hasPermission("banitem.command.metaitem")) {
            sendMessage(getNoPermMessage());
            return;
        }

        if (args.length < 2) { // Showing help
            sendHeaderMessage("&6&lMetaItems - Help");
            sendMessage("&7Usages:");

            sendMessage("&b/bi mi add &3<name> [actions] [message]");
            sendMessage("&7 >> Will save and ban the item (including meta)");
            sendMessage("&7 >> currently in your hand");
            sendMessage("&7 >> in your current world.");

            sendMessage("&b/bi mi get &3<name>");
            sendMessage("&7 >> Will give you the item (with meta)");
            sendMessage("&7 >> in your inventory.");

            sendMessage("&b/bi mi list");
            sendMessage("&7 >> Displays a list of meta");
            sendMessage("&7 >> item names saved.");

            sendMessage("&b/bi mi remove &3<name>");
            sendMessage("&7 >> Will remove & unban the metaitem");

            sendMessage("&cMetaItems match exactly the current item. If for example the durability changes, the " +
                    "item will not be recognized anymore. You probably want the custom items feature.");
            return;
        }

        final String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "add": { // /bi mi add <name> <actions> [message]
                // Not player?
                if (!(sender instanceof Player)) {
                    sendMessage("Command IG only.");
                    return;
                }

                if (args.length < 3) { // Showing help
                    sendHeaderMessage("&6&lMetaItems - Add");
                    sendMessage("&b/bi mi add &3<name> [actions] [message]");
                    sendMessage("&7 >> Will save and ban the item (with meta)");
                    sendMessage("&7 >> currently in your hand.");
                    sendMessage("&7 >> Actions can be multiple separated with commas.");
                    return;
                }

                // Getting player item in hand
                final Player player = (Player) sender;
                final ItemStack item = Utils.getItemInHand(player);

                // Only saving the meta item?
                final String name = args[2];
                if (args.length < 4) {
                    sendHeaderMessage("&6&lMetaItems - Add");
                    try {
                        plugin.getApi().addMetaItem(name, item);
                        sendMessage("&aMeta item successfully saved with name &2" + name + "&a.");
                    } catch (final Exception e) {
                        plugin.getLogger().warning("Error during saving meta item:" + e.getMessage());
                        sendMessage("&cError during saving. Check the console for more info.");
                    }
                    return;
                }

                final List<String> actionsNames = Listable.splitToList(args[3]);
                final List<BanAction> actions = Listable.getList(ListType.ACTION, actionsNames, null);
                if (actions.isEmpty()) {
                    sendHeaderMessage("&6&lMetaItems - Add");
                    sendMessage("&cInvalid actions entered: &e" + args[3]);
                    sendMessage("&7 >> Valid actions: &o" + Arrays.stream(BanAction.values()).map(BanAction::getName).collect(Collectors.joining(",", "", "&7.")));
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
                    if (sb.length() > 0) {
                        actionData.getMap().put(BanDataType.MESSAGE, Collections.singletonList(Chat.color(sb.toString())));
                    }
                }

                final Map<BanAction, BanActionData> actionsData = new EnumMap<>(BanAction.class);
                actions.forEach(ba -> actionsData.put(ba, actionData));

                // Adding meta item name
                try {
                    plugin.getBanDatabase().addMetaItem(name, item);
                } catch (final Exception e) {
                    plugin.getLogger().warning("Error during saving meta item:" + e.getMessage());
                    sendMessage("&cUnable to save metaitems.yml file.");
                    sendMessage("&cCheck the console for more information.");
                    return;
                }

                // Adding in blacklist
                plugin.getApi().addToBlacklist(new BannedItem(item), actionsData, player.getWorld());
                plugin.getListener().load(sender);
                sendMessage("&aThis item &e" + name + "&a is now successfully banned for world &2" + player.getWorld().getName() + "&a.");
                sendMessage("&7&oPlease note that you probably have the bypass");
                sendMessage("&7&opermission, so the ban may not apply to you.");
                return;
            }

            case "get": {
                // Not player?
                if (!(sender instanceof Player)) {
                    sendMessage("Command IG only.");
                    return;
                }

                if (args.length < 3) { // Showing help
                    sendHeaderMessage("&6&lMetaItems - Get");
                    sendMessage("&b/bi mi get &3<name>");
                    sendMessage("&7 >> Will give you the item (with meta)");
                    sendMessage("&7 >> in your inventory.");
                    return;
                }

                final BannedItem bi = plugin.getBanDatabase().getMetaItems().get(args[2]);
                if (bi == null) {
                    sendHeaderMessage("&6&lMetaItems - Get");
                    sendMessage("&cUnknown meta item &e" + args[2] + "&c.");
                    return;
                }

                sendHeaderMessage("&6&lMetaItems - Get");
                if (!((Player) sender).getInventory().addItem(bi.toItemStack()).isEmpty()) {
                    sendMessage("&cCan not get the meta item, your inventory is full.");
                } else {
                    sendMessage("&aSuccessfully received &e" + args[2] + "&a.");
                }
                return;
            }

            case "remove": {
                if (args.length < 3) { // Showing help
                    sendHeaderMessage("&6&lMetaItems - Remove");
                    sendMessage("&b/bi mi remove &3<name>");
                    sendMessage("&7 >> Will remove & unban the item");
                    sendMessage("&7 >> with this meta name.");
                    return;
                }

                // Checking variables
                final String metaName = args[2];

                // Checking if exists
                if (!plugin.getBanDatabase().getMetaItems().containsKey(metaName)) {
                    sendHeaderMessage("&6&lMetaItems - Remove");
                    sendMessage("&cThere is no meta item named &e" + metaName + "&c.");
                    return;
                }

                // Removing meta item
                sendHeaderMessage("&6&lMetaItems - Remove");
                try {
                    plugin.getBanDatabase().removeMetaItem(metaName);
                    plugin.getListener().load(sender);
                    sendMessage("&aMeta item &e" + metaName + "&a removed.");
                } catch (Exception e) {
                    plugin.getLogger().warning("Error during saving meta item:" + e.getMessage());
                    sendMessage("&cUnable to save metaitems.yml file.");
                    sendMessage("&cCheck the console for more information.");
                }
                return;
            }

            case "list": {
                final List<String> items = new ArrayList<>(plugin.getBanDatabase().getMetaItems().keySet());

                if (items.isEmpty()) {
                    sendHeaderMessage("&6&lMetaItems - List");
                    sendMessage("&7There is no meta item added yet.");
                    return;
                }

                sendHeaderMessage("&6&lMetaItems - List");
                sendMessage("&aMeta items: " + (items.stream().map(s -> ChatColor.GOLD + s + ChatColor.GRAY).collect(Collectors.joining(",", "", "&7."))));
            }
        }
    }

    @Nullable
    @Override
    public List<String> runTab() {
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], Arrays.asList("add", "get", "list", "remove"), new ArrayList<>());
        }

        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("list")) return Collections.emptyList();
            return StringUtil.copyPartialMatches(args[2], plugin.getBanDatabase().getMetaItems().keySet(), new ArrayList<>());
        }

        if (args.length == 4 && args[1].equalsIgnoreCase("add")) {
            return Collections.singletonList("action");
        }

        if (args.length > 4 && args[1].equalsIgnoreCase("add")) {
            return Collections.singletonList("message");
        }

        return Collections.emptyList();
    }
}
