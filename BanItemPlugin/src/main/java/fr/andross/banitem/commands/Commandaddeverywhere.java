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
import fr.andross.banitem.utils.statics.Chat;
import fr.andross.banitem.utils.statics.Utils;
import fr.andross.banitem.utils.statics.list.ListType;
import fr.andross.banitem.utils.statics.list.Listable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sub command addeverywhere
 * @version 3.0
 * @author Andross
 */
public class Commandaddeverywhere extends BanCommand {

    public Commandaddeverywhere(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Not player?
        if (!(sender instanceof Player)) {
            message("Command IG only.");
            return;
        }

        // Permission?
        if (!sender.hasPermission("banitem.command.addeverywhere")) {
            message(getNoPermMessage());
            return;
        }

        // /banitem addeverywhere <actions> [message]
        if (args.length < 2) {
            header("&6&lAdd");
            message("&7Usage: &b/bi addeverywhere &3<actions> [message]");
            message("&7 >> Will ban the item (material)");
            message("&7 >> actually in your hand");
            message("&7 >> in all worlds.");
            sender.sendMessage(pl.getBanConfig().getPrefix() + Chat.color("&2&oExample: /bi add place,break") + " &cThis item is banned.");
            message("&2&oPlayers will not be able to place nor break the item.");
            return;
        }

        // Getting player item in hand
        final Player p = (Player) sender;
        final ItemStack item = Utils.getItemInHand(p);

        // Parsing the actions
        final List<String> actionsNames = Listable.getSplittedList(args[1]);
        final List<BanAction> actions = Listable.getList(ListType.ACTION, actionsNames, null);
        if (actions.isEmpty()) {
            header("&6&lAdd");
            message("&cInvalid actions entered: &e" + args[1]);
            message("&7Valid actions: &o" + Arrays.stream(BanAction.values()).map(BanAction::getName).collect(Collectors.joining(",", "", "&7.")));
            return;
        }

        final BanActionData actionData = new BanActionData();

        // Have a message?
        if (args.length > 2) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (i > 2) sb.append(" ");
                sb.append(args[i]);
            }
            if (sb.length() > 0) actionData.getMap().put(BanDataType.MESSAGE, Collections.singletonList(Chat.color(sb.toString())));
        }

        final Map<BanAction, BanActionData> actionsData = new EnumMap<>(BanAction.class);
        actions.forEach(ba -> actionsData.put(ba, actionData));

        pl.getApi().addToBlacklist(new BannedItem(item.getType()), actionsData);
        pl.getListener().load(sender);
        header("&6&lAdd");
        message("&aSuccessfully banned &e" + item.getType().name().toLowerCase() + " &ain all worlds.");
        message("&7&oPlease note that you probably have the bypass");
        message("&7&opermission, so the ban may not apply to you.");
    }

    @Override
    public List<String> runTab() {
        if (args.length == 2) return StringUtil.copyPartialMatches(args[1], Stream.of(BanAction.values()).map(BanAction::getName).collect(Collectors.toList()), new ArrayList<>());
        if (args.length > 2) return Collections.singletonList("message");
        return Collections.emptyList();
    }
}
