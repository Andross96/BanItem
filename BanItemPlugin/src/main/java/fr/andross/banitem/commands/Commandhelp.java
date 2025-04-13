/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2021 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your action) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.andross.banitem.commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.actions.BanAction;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sub command help.
 *
 * @author Andross
 * @version 3.1
 */
public class Commandhelp extends BanCommand {
    private static final List<String> HELP_TYPES = Arrays.asList("worlds", "actions", "entities", "gamemodes", "inventories", "enchantments", "potions");

    /**
     * Constructor of the /banitem help command.
     *
     * @param plugin The ban item plugin instance
     * @param sender The command sender
     * @param args   The command arguments used by the command sender
     */
    public Commandhelp(final BanItem plugin, final CommandSender sender, final String[] args) {
        super(plugin, sender, args);
    }

    /**
     * Run the command.
     */
    @Override
    public void run() {
        // Permission?
        if (!sender.hasPermission("banitem.command.help")) {
            sendMessage(getNoPermMessage());
            return;
        }

        // No type entered?
        if (args.length < 2) {
            sendHeaderMessage("&6&lHelp");
            sendMessage("&b/bi help &3<type>");
            sendMessage("&7 >> Gives information about");
            sendMessage("&7 >> the type entered.");
            sendMessage("&7Types: &o" + HELP_TYPES.stream().collect(Collectors.joining(",", "", "&7.")));
            return;
        }

        final String type = args[1].toLowerCase();
        sendHeaderMessage("&6&lHelp");
        switch (type) {
            case "worlds":
            case "world":
            case "w": {
                sendMessage("&7List of Bukkit worlds loaded:");
                sendMessage("&7 >> " + Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.joining(",", "", "&7.")));
                break;
            }

            case "actions":
            case "action":
            case "o": {
                sendMessage("&7List of bannable actions:");
                sendMessage("&7 >> " + Arrays.stream(BanAction.values()).map(BanAction::getName).collect(Collectors.joining(",", "", "&7.")));
                break;
            }

            case "entities":
            case "entity":
            case "e": {
                sendMessage("&7List of entity types:");
                sendMessage("&7 >> " + Arrays.stream(EntityType.values()).map(EntityType::name).map(String::toLowerCase).collect(Collectors.joining(",", "", "&7.")));
                break;
            }

            case "gamemodes":
            case "gamemode":
            case "gm":
            case "g": {
                sendMessage("&7List of available gamemodes:");
                sendMessage("&7 >> " + Arrays.stream(GameMode.values()).map(GameMode::name).map(String::toLowerCase).collect(Collectors.joining(",", "", "&7.")));
                break;
            }

            case "inventories":
            case "inventory":
            case "inv":
            case "i": {
                sendMessage("&7List of inventory type:");
                sendMessage("&7 >> " + Arrays.stream(InventoryType.values()).map(InventoryType::name).map(String::toLowerCase).collect(Collectors.joining(",", "", "&7.")));
                break;
            }

            case "enchantments":
            case "enchantment":
            case "ench": {
                sendMessage("&7List of (bukkit) enchantments:");
                sendMessage("&7 >> " + Arrays.stream(Enchantment.values()).map(Enchantment::getName).map(String::toLowerCase).collect(Collectors.joining(",", "", "&7.")));
                break;
            }

            case "potions":
            case "potion":
            case "pot": {
                sendMessage("&7List of potions:");
                sendMessage("&7 >> " + Arrays.stream(PotionEffectType.values()).map(PotionEffectType::getName).map(String::toLowerCase).collect(Collectors.joining(",", "", "&7.")));
                break;
            }

            default: {
                sendMessage("&cUnknown help type.");
                sendMessage("&7 >> Type: " + String.join(",", HELP_TYPES));
                break;
            }
        }
    }

    /**
     * Run the tab completion of the command.
     *
     * @return the tab completion of the command.
     */
    @Nullable
    @Override
    public List<String> runTab() {
        return args.length == 2 ? StringUtil.copyPartialMatches(args[1], HELP_TYPES, new ArrayList<>()) : Collections.emptyList();
    }
}
