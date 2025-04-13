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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Sub command log
 *
 * @author Andross
 * @version 3.1
 */
public class Commandlog extends BanCommand {

    public Commandlog(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Not player?
        if (!(sender instanceof Player)) {
            sendMessage("Command IG only.");
            return;
        }

        // Permission?
        if (!sender.hasPermission("banitem.command.log")) {
            sendMessage(getNoPermMessage());
            return;
        }

        // Toggling log
        final UUID uuid = ((Player) sender).getUniqueId();
        sendHeaderMessage("&6&lLog");
        if (plugin.getUtils().getLogging().contains(uuid)) {
            plugin.getUtils().getLogging().remove(uuid);
            sendMessage("&7[LOG]: &c&lOFF");
        } else {
            plugin.getUtils().getLogging().add(uuid);
            sendMessage("&7[LOG]: &a&lON");
        }
    }

    @Override
    public List<String> runTab() {
        return Collections.emptyList();
    }
}
