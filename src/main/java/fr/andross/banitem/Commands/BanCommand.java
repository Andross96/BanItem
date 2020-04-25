package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract sub command class
 * @version 2.0
 * @author Andross
 */
public abstract class BanCommand {
    private final String header;
    protected final BanItem pl;
    protected final CommandSender sender;
    protected final String[] args;

    public BanCommand(@NotNull final BanItem pl, @NotNull final CommandSender sender, @NotNull final String[] args) {
        header = pl.getUtils().getPrefix() + pl.getUtils().color("&7&m     &r &l[%s&r&l] &7&m     ");
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
        sender.sendMessage(pl.getUtils().getPrefix() + pl.getUtils().color(message));
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
