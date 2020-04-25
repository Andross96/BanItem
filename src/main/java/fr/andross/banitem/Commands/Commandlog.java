package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sub command log
 * @version 2.0
 * @author Andross
 */
public class Commandlog extends BanCommand {

    public Commandlog(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Not player?
        if (!(sender instanceof Player)) {
            message("Command IG only.");
            return;
        }

        // Permission?
        if (!sender.hasPermission("banitem.command.log")) {
            message(getNoPermMessage());
            return;
        }

        // Toggling log
        final UUID uuid = ((Player) sender).getUniqueId();
        header("&6&lLog");
        if (pl.getUtils().getLogging().contains(uuid)) {
            pl.getUtils().getLogging().remove(uuid);
            message("&7[LOG]: &c&lOFF");
        } else {
            pl.getUtils().getLogging().add(uuid);
            message("&7[LOG]: &a&lON");
        }
    }

    @Override
    public List<String> runTab() {
        return new ArrayList<>();
    }
}
