package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Options.BanOption;
import fr.andross.banitem.Options.BanOptionData;
import fr.andross.banitem.Utils.Ban.BannedItem;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Sub command check
 * @version 2.0
 * @author Andross
 */
public class Commandcheck extends BanCommand {

    public Commandcheck(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Permission?
        if (!sender.hasPermission("banitem.command.check")) {
            message(getNoPermMessage());
            return;
        }

        // Checking...
        final boolean delete = args.length > 1 && args[1].equalsIgnoreCase("delete");
        final List<String> players = new ArrayList<>();
        for (final Player p : pl.getServer().getOnlinePlayers()) {
            final Map<BannedItem, Map<BanOption, BanOptionData>> map = pl.getBanDatabase().getBlacklist().get(p.getWorld());
            if (map == null) continue; // nothing banned in this world
            for (final BannedItem bannedItem : map.keySet()) {
                // Has banned item?
                final ItemStack itemStack = bannedItem.toItemStack();
                if (!p.getInventory().containsAtLeast(itemStack, 1)) continue;
                players.add(p.getName());

                // Removing?
                if (delete) {
                    final PlayerInventory inv = p.getInventory();
                    int size = inv.getSize();
                    for (int slot = 0; slot < size; slot++) {
                        final ItemStack is = inv.getItem(slot);
                        if (pl.getUtils().isNullOrAir(is)) continue;
                        final BannedItem bannedItemStack = new BannedItem(is);
                        if (bannedItem.equals(bannedItemStack)) inv.clear(slot);
                    }
                }
            }
        }

        // Showing list
        if (players.isEmpty()) {
            header("&6&lCheck");
            message("&7No player with blacklisted item in inventory found.");
            return;
        }

        final StringJoiner joiner = new StringJoiner(",", "", "&7.");
        for (final String player : players) joiner.add(ChatColor.GOLD + player + ChatColor.GRAY);
        header("&6&lCheck");
        message("&7Found &2" + players.size() + "&7 player(s):");
        message(joiner.toString());
        if (delete) message("&7&oSuccessfully removed banned items from &e&o" + players.size() + "&7&o players.");
    }

    @Override
    public List<String> runTab() {
        final List<String> list = new ArrayList<>();
        if (args.length == 2) list.add("delete");
        return list;
    }
}
