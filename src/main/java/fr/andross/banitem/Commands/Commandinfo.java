package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Utils.Ban.BannedItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command info
 * @version 2.0.1
 * @author Andross
 */
public class Commandinfo extends BanCommand {

    public Commandinfo(final BanItem pl, final CommandSender sender, final String[] args) {
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
        if (!sender.hasPermission("banitem.command.info")) {
            message(getNoPermMessage());
            return;
        }

        // Showing item info
        final Player p = (Player) sender;
        final ItemStack item = pl.getUtils().getItemInHand(p);

        header("&6&lInfo");
        final String itemName = item.getType().name().toLowerCase();
        final String customItemName = pl.getBanDatabase().getCustomItems().getName(new BannedItem(item));
        message("&7Material name: &e" + itemName);
        if (customItemName != null) message("&7Custom item name: &e" + customItemName);
        message("&7Permission: ");
        message(" &7>> &ebanitem.bypass." + p.getWorld().getName().toLowerCase() + "." + (customItemName != null ? customItemName.toLowerCase() : itemName) + ".option.*");
    }

    @Nullable
    @Override
    public List<String> runTab() {
        return new ArrayList<>();
    }
}
