package fr.andross.banitem.Commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Database.Blacklist;
import fr.andross.banitem.Database.ItemMap;
import fr.andross.banitem.Options.BanDataType;
import fr.andross.banitem.Options.BanOption;
import fr.andross.banitem.Options.BanOptionData;
import fr.andross.banitem.Utils.Item.BannedItem;
import fr.andross.banitem.Utils.Item.BannedItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * Sub command check
 * @version 2.1
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
        final Set<String> players = new HashSet<>();
        final Blacklist blacklist = pl.getBanDatabase().getBlacklist();
        for (final Player p : pl.getServer().getOnlinePlayers()) {
            final ItemMap map = blacklist.get(p.getWorld());
            if (map == null) continue; // nothing banned in this world

            // Checking player inventory
            final PlayerInventory inv = p.getInventory();
            item: for (int i = 0; i < inv.getSize(); i++) {
                final ItemStack item = inv.getItem(i);
                if (pl.getUtils().isNullOrAir(item)) continue;

                final Map<BanOption, BanOptionData> data = map.get(new BannedItem(item));
                if (data == null || data.isEmpty()) continue;

                // Checking metadata
                for (final BanOption option : data.keySet()) {
                    final BanOptionData optionData = data.get(option);
                    if (optionData.containsKey(BanDataType.METADATA)) {
                        final BannedItemMeta meta = optionData.getMetadata();
                        if (meta != null && !meta.matches(item)) continue item;
                    }
                }

                // Blacklisted!
                if (delete) inv.clear(i);
                players.add(p.getName());
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
