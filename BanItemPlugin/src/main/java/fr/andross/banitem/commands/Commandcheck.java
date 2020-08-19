/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2020 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import fr.andross.banitem.database.Blacklist;
import fr.andross.banitem.database.ItemMap;
import fr.andross.banitem.options.BanDataType;
import fr.andross.banitem.options.BanOption;
import fr.andross.banitem.options.BanOptionData;
import fr.andross.banitem.utils.item.BannedItem;
import fr.andross.banitem.utils.item.BannedItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * Sub command check
 * @version 2.4
 * @author Andross
 */
public class Commandcheck extends BanCommand {

    public Commandcheck(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Permission?
        if (!sender.hasPermission("banitem.command.check")) {
            message(getNoPermMessage());
            return;
        }

        // Checking...
        final boolean delete = args.length > 1 && args[1].equalsIgnoreCase("delete");
        final Set<String> players = new HashSet<>();
        final Blacklist blacklist = pl.getBanDatabase().getBlacklist();
        for (final Player p : pl.getServer().getOnlinePlayers()) {
            final ItemMap map = blacklist.get(p.getWorld());
            if (map == null) continue; // nothing banned in this world

            // Checking player inventory
            final PlayerInventory inv = p.getInventory();
            item: for (int i = 0; i < inv.getSize(); i++) {
                final ItemStack item = inv.getItem(i);
                if (pl.getUtils().isNullOrAir(item)) continue;

                final Map<BanOption, BanOptionData> data = map.get(new BannedItem(item));
                if (data == null || data.isEmpty()) continue;

                // Checking metadata
                for (final BanOption option : data.keySet()) {
                    final BanOptionData optionData = data.get(option);
                    if (optionData.containsKey(BanDataType.METADATA)) {
                        final BannedItemMeta meta = optionData.getMetadata();
                        if (meta != null && !meta.matches(item)) continue item;
                    }
                }

                // Blacklisted!
                if (delete) inv.clear(i);
                players.add(p.getName());
            }
        }

        // Showing list
        if (players.isEmpty()) {
            header("&6&lCheck");
            message("&7No player with blacklisted item in inventory found.");
            return;
        }

        final StringJoiner joiner = new StringJoiner(",", "", "&7.");
        for (final String player : players) joiner.add(ChatColor.GOLD + player + ChatColor.GRAY);
        header("&6&lCheck");
        message("&7Found &2" + players.size() + "&7 player(s):");
        message(joiner.toString());
        if (delete) message("&7&oSuccessfully removed banned items from &e&o" + players.size() + "&7&o players.");
    }

    @Override
    public List<String> runTab() {
        final List<String> list = new ArrayList<>();
        if (args.length == 2) list.add("delete");
        return list;
    }
}
