package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BannedItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

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
        final Set<String> players = new HashSet<>();
        for (String world : pl.getBlacklist().keySet()) {
            final Map<BannedItem, Map<BanOption, String>> map = pl.getBlacklist().get(world);

            for (final BannedItem bannedItem : map.keySet()) {
                final ItemStack item = bannedItem.toItemStack();
                for (final Player p : pl.getServer().getOnlinePlayers()) {
                    if (!p.getWorld().getName().equalsIgnoreCase(world)) continue;
                    if (!p.getInventory().containsAtLeast(item, 1)) continue;
                    players.add(p.getName());
                    if (!delete) continue;

                    // Deleting
                    final PlayerInventory inv = p.getInventory();
                    int size = inv.getSize();
                    for (int slot = 0; slot < size; slot++) {
                        final ItemStack is = inv.getItem(slot);
                        if (is == null) continue;
                        if (is.isSimilar(item)) inv.clear(slot);
                    }
                }
            }
        }

        // Showing list
        if (players.isEmpty()) {
            message("&c[&e&lBanItem&c] &eFound &20&e player with blacklisted item(s) in inventory.");
            return;
        }

        final StringBuilder list = new StringBuilder();
        for (String player : players) list.append(ChatColor.GOLD).append(player).append(ChatColor.GRAY).append(", ");
        message("&c[&e&lBanItem&c] &eFound &2" + players.size() + "&e player(s) with blacklisted item(s) in inventory: ");
        message(list.toString().substring(0, list.toString().length() - 2) + "&7.");
        if (delete) message("&c[&e&lBanItem&c] &7&oSuccessfully removed.");
    }
}
