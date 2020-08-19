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
import fr.andross.banitem.utils.item.BannedItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command info
 * @version 2.4
 * @author Andross
 */
public class Commandinfo extends BanCommand {

    public Commandinfo(final BanItem pl, final CommandSender sender, final String[] args) {
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
        if (!sender.hasPermission("banitem.command.info")) {
            message(getNoPermMessage());
            return;
        }

        // Showing item info
        final Player p = (Player) sender;
        final ItemStack item = pl.getUtils().getItemInHand(p);

        header("&6&lInfo");
        final String itemName = item.getType().name().toLowerCase();
        final String customItemName = pl.getBanDatabase().getCustomItems().getName(new BannedItem(item));
        message("&7Material name: &e" + itemName);
        if (customItemName != null) message("&7Custom item name: &e" + customItemName);
        message("&7Permission: ");
        message(" &7>> &ebanitem.bypass." + p.getWorld().getName().toLowerCase() + "." + (customItemName != null ? customItemName.toLowerCase() : itemName) + ".option.*");
    }

    @Nullable
    @Override
    public List<String> runTab() {
        return new ArrayList<>();
    }
}
