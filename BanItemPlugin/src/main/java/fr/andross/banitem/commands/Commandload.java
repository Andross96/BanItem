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

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Sub command load
 * @version 3.1
 * @author Andross
 */
public class Commandload extends BanCommand {

    public Commandload(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Permission?
        if (!sender.hasPermission("banitem.command.load")) {
            message(getNoPermMessage());
            return;
        }

        // Args?
        if (args.length < 2) {
            header("&6&lLoad");
            message("&7Usage: &b/bi load &3<filename>");
            message("&7Load the configuration file with name <filename>");
            return;
        }

        final File f = new File(pl.getDataFolder(), args[1] + ".yml");
        if (!f.exists()) {
            header("&6&lLoad");
            message("&cThere is no configuration file named &e" + args[1] + ".yml&c.");
            return;
        }

        pl.getApi().load(sender, f);
    }

    @Override
    public List<String> runTab() {
        return Collections.emptyList();
    }
}
