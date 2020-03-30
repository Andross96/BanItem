package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Utils.Chat;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        sender.sendMessage(Chat.color(message));
    }

    String getNoPermMessage() {
        return pl.getConfig().getString("no-permission", "&cYou do not have permission.");
    }

    public abstract void run();

    @Nullable
    public abstract List<String> runTab();
}
