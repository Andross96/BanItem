package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import org.bukkit.command.CommandSender;

public class Commandreload extends BanCommand {

    public Commandreload(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Permission?
        if (!sender.hasPermission("banitem.command.reload")) {
            message(getNoPermMessage());
            return;
        }

        pl.load(sender);
    }

}
