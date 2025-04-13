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
import fr.andross.banitem.utils.Chat;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Abstract sub command class.
 *
 * @author Andross
 * @version 3.2
 */
public abstract class BanCommand {
    /**
     * Header used into the message sent to the command sender.
     */
    private static final String HEADER = Chat.color("&7&m     &r &l[%s&r&l] &7&m     ");

    /**
     * BanItem plugin instance.
     */
    protected final BanItem plugin;

    /**
     * Command sender.
     */
    protected final CommandSender sender;

    /**
     * Arguments used by the command sender into the command.
     */
    protected final String[] args;

    /**
     * Create a BanCommand object which handles in game BanItem commands.
     *
     * @param plugin ban item plugin instance
     * @param sender the command sender
     * @param args the arguments for the command used by the sender
     */
    public BanCommand(@NotNull final BanItem plugin,
                      @NotNull final CommandSender sender,
                      @NotNull final String[] args) {
        this.plugin = plugin;
        this.sender = sender;
        this.args = args;
    }

    /**
     * Sending a header separator.
     *
     * @param title the title of the header
     */
    protected void sendHeaderMessage(@NotNull final String title) {
        plugin.getUtils().sendMessage(sender, String.format(HEADER, title));
    }

    /**
     * Send a <i>(colored)</i> message to the sender.
     *
     * @param message message to send
     */
    protected void sendMessage(@Nullable final String message) {
        plugin.getUtils().sendMessage(sender, message);
    }

    /**
     * Get the "no-permission" message from config
     *
     * @return the "no-permission" message from config
     */
    @Nullable
    protected String getNoPermMessage() {
        return plugin.getBanConfig().getConfig().getString("no-permission");
    }

    /**
     * Running a subcommand.
     */
    public abstract void run();

    /**
     * Running a subcommand tab.
     *
     * @return list of tab completion
     */
    @Nullable
    public abstract List<String> runTab();
}
