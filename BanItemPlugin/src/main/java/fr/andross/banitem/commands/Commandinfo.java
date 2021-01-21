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
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.utils.statics.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Sub command info
 * @version 3.0
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
        final ItemStack item = Utils.getItemInHand(p);

        header("&6&lInfo");
        final String materialName = item.getType().name().toLowerCase();
        message("&7Material name: &e" + materialName);
        final String metaItemName = pl.getBanDatabase().getMetaItems().getKey(new BannedItem(item));
        if (metaItemName != null) message("&7Meta item name: &e" + metaItemName);
        message("&7Permission example:");
        message(" &7>> &ebanitem.bypass." + p.getWorld().getName().toLowerCase() + "." + (metaItemName != null ? metaItemName.toLowerCase() : materialName) + ".action.*");
    }

    @Nullable
    @Override
    public List<String> runTab() {
        return Collections.emptyList();
    }
}
