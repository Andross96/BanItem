package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import org.bukkit.command.CommandSender;

public abstract class BanCommand {
    protected final BanItem pl;
    protected final CommandSender sender;
    protected final String[] args;

    public BanCommand(final BanItem pl, final CommandSender sender, final String[] args) {
        this.pl = pl;
        this.sender = sender;
        this.args = args;
    }

    void message(final String message) {
        sender.sendMessage(pl.color(message));
    }

    String getNoPermMessage() {
        return pl.getConfig().getString("no-permission", "&cYou do not have permission.");
    }

    public abstract void run();
}
