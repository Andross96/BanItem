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
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract sub command class
 * @version 2.4
 * @author Andross
 */
public abstract class BanCommand {
    private final String header;
    protected final BanItem pl;
    protected final CommandSender sender;
    protected final String[] args;

    public BanCommand(@NotNull final BanItem pl, @NotNull final CommandSender sender, @NotNull final String[] args) {
        header = pl.getBanConfig().getPrefix() + pl.getUtils().color("&7&m     &r &l[%s&r&l] &7&m     ");
        this.pl = pl;
        this.sender = sender;
        this.args = args;
    }

    /**
     * Sending a header separator
     */
    void header(@NotNull final String title) {
        sender.sendMessage(pl.getUtils().color(String.format(header, title)));
    }

    /**
     * Send a <i>(colored)</i> message to the sender
     * @param message message to send
     */
    void message(@NotNull final String message) {
        pl.getUtils().sendMessage(sender, message);
    }

    /**
     * @return the no permission message from config, otherwise giving the default one
     */
    String getNoPermMessage() {
        return pl.getUtils().color(pl.getBanConfig().getNoPermission());
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

    // Static running of commands
    private static final Map<String, Class<? extends BanCommand>> commands = new HashMap<>();

    static {
        // Loading sub commands
        commands.put("add", Commandadd.class);
        commands.put("check", Commandcheck.class);
        commands.put("customitem", Commandcustomitem.class);
        commands.put("ci", Commandcustomitem.class);
        commands.put("help", Commandhelp.class);
        commands.put("info", Commandinfo.class);
        commands.put("log", Commandlog.class);
        commands.put("reload", Commandreload.class);
        commands.put("rl", Commandreload.class);
    }

    /**
     * Used to run a subcommand
     * @param pl the {@link BanItem} plugin instance
     * @param command the subcommand entered
     * @param sender the command sender
     * @param args args used in the commands
     * @throws Exception if the sub command is unknown, or if any exception occurs
     */
    public static void runCommand(@NotNull final BanItem pl, @NotNull final String command, @NotNull final CommandSender sender, @NotNull final String[] args) throws Exception {
        commands.get(command.toLowerCase())
                .getDeclaredConstructor(BanItem.class, CommandSender.class, String[].class)
                .newInstance(pl, sender, args)
                .run();
    }

    /**
     * Used to get a list of tab completition for sub commands
     * @param pl the {@link BanItem} plugin instance
     * @param command the subcommand entered
     * @param sender the command sender
     * @param args args used in the commands
     * @return list of tab completition, if any
     * @throws Exception if the sub command is unknown, or if any exception occurs
     */
    @Nullable
    public static List<String> runTab(@NotNull final BanItem pl, @NotNull final String command, @NotNull final CommandSender sender, @NotNull final String[] args) throws Exception {
        return commands.get(command.toLowerCase())
                .getDeclaredConstructor(BanItem.class, CommandSender.class, String[].class)
                .newInstance(pl, sender, args)
                .runTab();
    }
}
