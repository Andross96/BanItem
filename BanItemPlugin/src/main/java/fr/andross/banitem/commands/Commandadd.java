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
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.actions.BanDataType;
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.utils.Chat;
import fr.andross.banitem.utils.Utils;
import fr.andross.banitem.utils.list.ListType;
import fr.andross.banitem.utils.list.Listable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sub command add
 * @version 3.1
 * @author Andross
 */
public class Commandadd extends BanCommand {

    public Commandadd(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Permission?
        if (!sender.hasPermission("banitem.command.add")) {
            message(getNoPermMessage());
            return;
        }

        // /bi add <actions> [-m materials] [-w worlds] [message]
        if (args.length < 2) {
            if (sender instanceof Player) {
                header("&6&lAdd");
                message("&7Usage:");
                message("&b/bi add &3<actions> [-m materials] [-w worlds] [message]");
                message("&7 >> Will ban the item (material).");
                message("&7 >> If no worlds or materials are entered");
                message("&7 >> it will ban the current material in your hand");
                message("&7 >> into the current world.");
                message("&2&oExample: /bi add place,break This item is banned.");
                message("&7&oPlayers will not be able to place nor break the item");
                message("&7&ointo your current world.");
                message("&2&oExample2: /bi add place -m stone -w world");
                message("&7&oPlayers will not be able to place stone into world.");
            } else {
                header("&6&lAdd");
                message("&7Usage: &b/bi add &3<actions> <-m materials> <-w worlds> [message]");
                message("&7 >> Will ban the item (material) into the set world.");
                message("&2&oExample: /bi add place -m stone -w world This item is banned.");
                message("&7&oPlayers will not be able to place stone");
                message("&o&ointo world.");
            }
            return;
        }

        // Parsing the actions
        final List<String> actionsNames = Listable.getSplittedList(args[1]);
        final List<BanAction> actions = Listable.getList(ListType.ACTION, actionsNames, null);
        if (actions.isEmpty()) {
            header("&6&lAdd");
            message("&cInvalid actions entered: &e" + args[1]);
            message("&7Valid actions: &o" + Arrays.stream(BanAction.values()).map(BanAction::getName).collect(Collectors.joining(",", "", "&7.")));
            return;
        }

        final BanActionData actionData = new BanActionData();
        final List<Material> materials = new ArrayList<>();
        final List<World> worlds = new ArrayList<>();
        int startMessage = 2;

        // Set materials?
        for (int i = 0; i < args.length; i++) {
            if (args[i].toLowerCase(Locale.ROOT).startsWith("-m")) {
                if (args.length <= i + 1) {
                    header("&6&lAdd");
                    message("&cInvalid material(s) synthax. Must be &e-m material1,material2...");
                    return;
                }

                final String material = args[i + 1];
                materials.addAll(Listable.getMaterials(material, null));
                if (material.isEmpty()) {
                    header("&6&lAdd");
                    message("&cInvalid material(s) entered: &e" + material);
                    if (sender instanceof Player) {
                        message("&7You can use &e/bi info&7 to get the material of");
                        message("&7the item currently in your hand.");
                    }
                    return;
                }
                startMessage += 2;
                break;
            }
        }

        // Set worlds?
        for (int i = 0; i < args.length; i++) {
            if (args[i].toLowerCase(Locale.ROOT).startsWith("-w")) {
                if (args.length <= i + 1) {
                    header("&6&lAdd");
                    message("&cInvalid world(s) synthax. Must be &e-w worldName,worldName2...");
                    return;
                }

                final String world = args[i + 1];
                worlds.addAll(Listable.getWorlds(world, null));
                if (worlds.isEmpty()) {
                    header("&6&lAdd");
                    message("&cInvalid world(s) entered: &e" + world);
                    message("&7Valid worlds: &o" + Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.joining(",", "", "&7.")));
                    return;
                }
                startMessage += 2;
                break;
            }
        }

        // Have a message?
        if (args.length - 1 >= startMessage) {
            final StringBuilder sb = new StringBuilder();
            for (int i = startMessage; i < args.length; i++) {
                if (i > startMessage) sb.append(" ");
                sb.append(args[i]);
            }
            if (sb.length() > 0)
                actionData.getMap().put(BanDataType.MESSAGE, Collections.singletonList(Chat.color(sb.toString())));
        }

        final Map<BanAction, BanActionData> actionsData = new EnumMap<>(BanAction.class);
        actions.forEach(ba -> actionsData.put(ba, actionData));

        // Checking materials
        if (materials.isEmpty()) {
            // Console?
            if (!(sender instanceof Player)) {
                header("Add");
                message("You must enter the material(s) which will be banned.");
                message("Example: /bi add place -m stone -w world");
                return;
            }
            materials.add(Utils.getItemInHand((Player) sender).getType());
        }

        // Checking worlds
        if (worlds.isEmpty()) {
            // Console?
            if (!(sender instanceof Player)) {
                header("Add");
                message("You must enter the world(s) in which the ban will be applied.");
                message("Example: /bi add place -m stone -w world");
                return;
            }
            worlds.add(((Player) sender).getWorld());
        }

        // Adding!
        final World[] worldsArray = worlds.toArray(new World[0]);
        if (pl.getApi().addToBlacklist(materials.stream().map(BannedItem::new).collect(Collectors.toList()), actionsData, worldsArray)) {
            header("&6&lAdd");
            final String materialsName = materials.size() > 10 ? "these materials" : materials.stream().map(Material::name).collect(Collectors.joining(","));
            final String worldsName = Bukkit.getWorlds().size() == worlds.size() ? "&2ALL" : worlds.stream().map(World::getName).collect(Collectors.joining(","));
            message("&aSuccessfully banned &e" + materialsName.toLowerCase(Locale.ROOT) + " &afor world &2" + worldsName + "&a.");
            if (sender instanceof Player) {
                message("&7&oPlease note that you probably have the bypass");
                message("&7&opermission, so the ban may not apply to you.");
            }
            pl.getListener().load(sender);
            return;
        }

        message("&cAn error occured while banning the item.");
        message("&cCheck the console for more information.");
    }

    @Override
    public List<String> runTab() {
        if (args.length == 2) return StringUtil.copyPartialMatches(args[1], Arrays.stream(BanAction.values()).map(BanAction::getName).collect(Collectors.toList()), new ArrayList<>());
        return Arrays.asList("-w", "-m", "message");
    }
}
