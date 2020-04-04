package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Utils.BanUtils;
import fr.andross.banitem.Utils.BannedItem;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Commandinfo extends BanCommand {

    public Commandinfo(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Console?
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
        final ItemStack item = BanUtils.v9OrMore ? p.getInventory().getItemInMainHand() : p.getInventory().getItemInHand();
        if (item.getType() == Material.AIR) {
            message("&c[&e&lBanItem&c] &cYou must have a valid item in your hand.");
        } else {
            final String itemName = item.getType().name().toLowerCase();
            message("&c[&e&lBanItem&c] &7Material name: &e" + itemName);
            message("&c[&e&lBanItem&c] &7Permission: &ebanitem.bypass." + p.getWorld().getName().toLowerCase() + "." + itemName);

            final String customItemName = pl.getBanDatabase().getCustomItems().getName(new BannedItem(item));
            if (customItemName != null) message("&c[&e&lBanItem&c] &7Custom item name: &e" + customItemName);
        }
    }

    @Nullable
    @Override
    public List<String> runTab() {
        return new ArrayList<>();
    }
}
