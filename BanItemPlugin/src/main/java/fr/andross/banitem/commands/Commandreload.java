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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Sub command reload.
 *
 * @author Andross
 * @version 3.1
 */
public class Commandreload extends BanCommand {

    /**
     * Constructor of the /banitem reload command.
     *
     * @param plugin The ban item plugin instance
     * @param sender The command sender
     * @param args   The command arguments used by the command sender
     */
    public Commandreload(final BanItem plugin, final CommandSender sender, final String[] args) {
        super(plugin, sender, args);
    }

    /**
     * Run the command.
     */
    @Override
    public void run() {
        // Permission?
        if (!sender.hasPermission("banitem.command.reload")) {
            sendMessage(getNoPermMessage());
            return;
        }

        sendHeaderMessage("&6&lReload");
        final File config = plugin.getBanConfig().getConfigFile();
        plugin.getApi().load(sender, config.getName().equals("config.yml") ? null : config);
    }

    /**
     * Run the tab completion of the command.
     *
     * @return the tab completion of the command.
     */
    @Nullable
    @Override
    public List<String> runTab() {
        return Collections.emptyList();
    }
}
