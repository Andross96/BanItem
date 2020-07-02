package fr.andross.banitem.commands;

import fr.andross.banitem.BanItem;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command reload
 * @version 2.0
 * @author Andross
 */
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

        header("&6&lReload");
        pl.getApi().load(sender, null);
    }

    @Nullable
    @Override
    public List<String> runTab() {
        return new ArrayList<>();
    }
}
