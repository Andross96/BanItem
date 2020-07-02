package fr.andross.banitem.commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.options.BanDataType;
import fr.andross.banitem.options.BanOption;
import fr.andross.banitem.options.BanOptionData;
import fr.andross.banitem.utils.BanVersion;
import fr.andross.banitem.utils.Listable;
import fr.andross.banitem.utils.item.BannedItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sub command add
 * @version 2.0
 * @author Andross
 */
public class Commandadd extends BanCommand {

    public Commandadd(final BanItem pl, final CommandSender sender, final String[] args) {
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
        if (!sender.hasPermission("banitem.command.add")) {
            message(getNoPermMessage());
            return;
        }

        // /banitem add <options> [message]
        if (args.length < 2) {
            header("&6&lAdd");
            message("&7Usage: &b/bi add &3<options> [message]");
            message("&7 >> Will ban the item (type)");
            message("&7 >> in your hand.");
            sender.sendMessage(pl.getBanConfig().getPrefix() + pl.getUtils().color("&7 >> &oExample: /bi add place,break ") + "&cThis item is banned.");
            return;
        }

        // Getting player item in hand
        final Player p = (Player) sender;
        final ItemStack item = BanVersion.v9OrMore ? p.getInventory().getItemInMainHand() : p.getInventory().getItemInHand();

        // Parsing the options
        final List<String> optionsNames = pl.getUtils().getSplittedList(args[1]);
        final List<BanOption> options = pl.getUtils().getList(Listable.Type.OPTION, optionsNames, null, null);
        if (options.isEmpty()) {
            header("&6&lAdd");
            message("&cInvalid options set: &e" + args[1]);
            message("&7Valid options: &o" + Arrays.stream(BanOption.values()).map(BanOption::getName).collect(Collectors.joining(",")));
            return;
        }

        // Have a message?
        final List<String> messages = new ArrayList<>();
        if (args.length > 2) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++){
                if (i > 2) sb.append(" ");
                sb.append(args[i]);
            }
            if (sb.length() > 0) messages.add(pl.getUtils().color(sb.toString()));
        }

        final BanOptionData optionData = new BanOptionData();
        if (!messages.isEmpty()) optionData.put(BanDataType.MESSAGE, messages);
        final Map<BanOption, BanOptionData> optionsData = new HashMap<>();
        for (final BanOption bo : options) optionsData.put(bo, optionData);

        pl.getApi().addToBlacklist(new BannedItem(item, false), optionsData, p.getWorld());
        pl.getListener().load(sender);
        header("&6&lAdd");
        message("&2The item '&a" + item.getType().name().toLowerCase() + "&2' is now successfully banned for world &a" + p.getWorld().getName() + "&2.");
        message("&7&oNote that you surely have the bypass permission, so the ban may not apply to you!");
    }

    @Override
    public List<String> runTab() {
        final List<String> list = new ArrayList<>();
        if (args.length == 2) list.add("options");
        if (args.length > 2) list.add("message");
        return list;
    }
}
