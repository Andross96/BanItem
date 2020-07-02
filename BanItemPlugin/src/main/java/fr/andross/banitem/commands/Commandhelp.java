package fr.andross.banitem.commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.options.BanOption;
import fr.andross.banitem.utils.Listable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sub command help
 * @version 2.0
 * @author Andross
 */
public class Commandhelp extends BanCommand {
    private static final List<String> types = Arrays.asList("worlds", "options", "entities", "gamemodes", "inventories", "enchantments", "potions");

    public Commandhelp(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Permission?
        if (!sender.hasPermission("banitem.command.help")) {
            message(getNoPermMessage());
            return;
        }

        // No type entered?
        if (args.length < 2) {
            header("&6&lHelp");
            message("&b/bi help &3<type>");
            message("&7 >> Gives informations about");
            message("&7 >> the type entered.");

            return;
        }

        final String type = args[1].toLowerCase();
        final Listable listable = pl.getUtils();
        header("&6&lHelp");
        switch (type) {
            case "worlds": case "world": case "w": {
                message("&7The Bukkit world. List:");
                message("&7 >> " + Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.joining(",")));
                break;
            }

            case "options": case "option": case "o": {
                message("&7The ban options. They can be written");
                message("&7complexly with commas. List:");
                message("&7 >> " + listable.getOptions().stream().map(BanOption::getName).collect(Collectors.joining(",")));
                break;
            }

            case "entities": case "entity": case "e": {
                message("&7The entity type. They can be written");
                message("&7complexly with commas. List:");
                message("&7 >> " + listable.getEntities().stream().map(EntityType::name).map(String::toLowerCase).collect(Collectors.joining(",")));
                break;
            }

            case "gamemodes": case "gamemode": case "gm": case "g": {
                message("&7The gamemode type. They can be written");
                message("&7complexly with commas. List:");
                message("&7 >> " + listable.getGamemodes().stream().map(GameMode::name).map(String::toLowerCase).collect(Collectors.joining(",")));
                break;
            }

            case "inventories": case "inventory": case "inv": case "i": {
                message("&7The inventory type. They can be written");
                message("&7complexly with commas. List:");
                message("&7 >> " + listable.getInventories().stream().map(InventoryType::name).map(String::toLowerCase).collect(Collectors.joining(",")));
                break;
            }

            case "enchantments": case "enchantment": case "ench": {
                message("&7The enchantment type. They can be written");
                message("&7complexly with commas. List:");
                message("&7 >> " + listable.getEnchantments().stream().map(Enchantment::getName).map(String::toLowerCase).collect(Collectors.joining(",")));
                break;
            }

            case "potions": case "potion": case "pot": {
                message("&7The potion type. They can be written");
                message("&7complexly with commas. List:");
                message("&7 >> " + listable.getPotions().stream().map(PotionType::name).map(String::toLowerCase).collect(Collectors.joining(",")));
                break;
            }

            default: {
                message("&cUnknown help type.");
                message("&7 >> Type: " + String.join(",", types));
                break;
            }
        }
    }

    @Nullable
    @Override
    public List<String> runTab() {
        if (args.length == 2) return StringUtil.copyPartialMatches(args[1], types, new ArrayList<>());
        return new ArrayList<>();
    }
}
