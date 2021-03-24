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
 * Abstract sub command class
 * @version 3.1
 * @author Andross
 */
public abstract class BanCommand {
    private final String header = Chat.color("&7&m     &r &l[%s&r&l] &7&m     ");
    protected final BanItem pl;
    protected final CommandSender sender;
    protected final String[] args;

    public BanCommand(@NotNull final BanItem pl, @NotNull final CommandSender sender, @NotNull final String[] args) {
        this.pl = pl;
        this.sender = sender;
        this.args = args;
    }

    /**
     * Sending a header separator
     */
    protected void header(@NotNull final String title) {
        pl.getUtils().sendMessage(sender, String.format(header, title));
    }

    /**
     * Send a <i>(colored)</i> message to the sender
     * @param message message to send
     */
    protected void message(@Nullable final String message) {
        pl.getUtils().sendMessage(sender, message);
    }

    /**
     * @return the no permission message from config
     */
    @Nullable
    protected String getNoPermMessage() {
        return pl.getBanConfig().getConfig().getString("no-permission");
    }

    /**
     * Running a subcommand
     */
    public abstract void run();

    /**
     * Running a subcommand tab
     * @return list of tab completition
     */
    @Nullable
    public abstract List<String> runTab();
}
