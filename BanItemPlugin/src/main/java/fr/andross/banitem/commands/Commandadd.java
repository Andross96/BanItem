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
 * Sub command add.
 *
 * @author Andross
 * @version 3.1
 */
public class Commandadd extends BanCommand {

    /**
     * Constructor of the /banitem add command.
     *
     * @param plugin The ban item plugin instance
     * @param sender The command sender
     * @param args   The command arguments used by the command sender
     */
    public Commandadd(final BanItem plugin, final CommandSender sender, final String[] args) {
        super(plugin, sender, args);
    }

    /**
     * Run the command.
     */
    @Override
    public void run() {
        // Permission?
        if (!sender.hasPermission("banitem.command.add")) {
            sendMessage(getNoPermMessage());
            return;
        }

        // /bi add <actions> [-m materials] [-w worlds] [message]
        if (args.length < 2) {
            if (sender instanceof Player) {
                sendHeaderMessage("&6&lAdd");
                sendMessage("&7Usage:");
                sendMessage("&b/bi add &3<actions> [-m materials] [-w worlds] [message]");
                sendMessage("&7 >> Will ban the item (material).");
                sendMessage("&7 >> If no worlds or materials are entered");
                sendMessage("&7 >> it will ban the current material in your hand");
                sendMessage("&7 >> into the current world.");
                sendMessage("&2&oExample: /bi add place,break This item is banned.");
                sendMessage("&7&oPlayers will not be able to place nor break the item");
                sendMessage("&7&ointo your current world.");
                sendMessage("&2&oExample2: /bi add place -m stone -w world");
                sendMessage("&7&oPlayers will not be able to place stone into world.");
            } else {
                sendHeaderMessage("&6&lAdd");
                sendMessage("&7Usage: &b/bi add &3<actions> <-m materials> <-w worlds> [message]");
                sendMessage("&7 >> Will ban the item (material) into the set world.");
                sendMessage("&2&oExample: /bi add place -m stone -w world This item is banned.");
                sendMessage("&7&oPlayers will not be able to place stone");
                sendMessage("&o&ointo world.");
            }
            return;
        }

        // Parsing the actions
        final List<String> actionsNames = Listable.splitToList(args[1]);
        final List<BanAction> actions = Listable.getList(ListType.ACTION, actionsNames, null);
        if (actions.isEmpty()) {
            sendHeaderMessage("&6&lAdd");
            sendMessage("&cInvalid actions entered: &e" + args[1]);
            sendMessage("&7Valid actions: &o" + Arrays.stream(BanAction.values()).map(BanAction::getName).collect(Collectors.joining(",", "", "&7.")));
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
                    sendHeaderMessage("&6&lAdd");
                    sendMessage("&cInvalid material(s) syntax. Must be &e-m material1,material2...");
                    return;
                }

                final String material = args[i + 1];
                materials.addAll(Listable.getMaterials(material, null));
                if (material.isEmpty()) {
                    sendHeaderMessage("&6&lAdd");
                    sendMessage("&cInvalid material(s) entered: &e" + material);
                    if (sender instanceof Player) {
                        sendMessage("&7You can use &e/bi info&7 to get the material of");
                        sendMessage("&7the item currently in your hand.");
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
                    sendHeaderMessage("&6&lAdd");
                    sendMessage("&cInvalid world(s) syntax. Must be &e-w worldName,worldName2...");
                    return;
                }

                final String world = args[i + 1];
                worlds.addAll(Listable.getWorlds(world, null));
                if (worlds.isEmpty()) {
                    sendHeaderMessage("&6&lAdd");
                    sendMessage("&cInvalid world(s) entered: &e" + world);
                    sendMessage("&7Valid worlds: &o" + Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.joining(",", "", "&7.")));
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
                sendHeaderMessage("Add");
                sendMessage("You must enter the material(s) which will be banned.");
                sendMessage("Example: /bi add place -m stone -w world");
                return;
            }
            materials.add(Utils.getItemInHand((Player) sender).getType());
        }

        // Checking worlds
        if (worlds.isEmpty()) {
            // Console?
            if (!(sender instanceof Player)) {
                sendHeaderMessage("Add");
                sendMessage("You must enter the world(s) in which the ban will be applied.");
                sendMessage("Example: /bi add place -m stone -w world");
                return;
            }
            worlds.add(((Player) sender).getWorld());
        }

        // Adding!
        final World[] worldsArray = worlds.toArray(new World[0]);
        if (plugin.getApi().addToBlacklist(materials.stream().map(BannedItem::new).collect(Collectors.toList()), actionsData, worldsArray)) {
            sendHeaderMessage("&6&lAdd");
            final String materialsName = materials.size() > 10 ? "these materials" : materials.stream().map(Material::name).collect(Collectors.joining(","));
            final String worldsName = Bukkit.getWorlds().size() == worlds.size() ? "&2ALL" : worlds.stream().map(World::getName).collect(Collectors.joining(","));
            sendMessage("&aSuccessfully banned &e" + materialsName.toLowerCase(Locale.ROOT) + " &afor world &2" + worldsName + "&a.");
            if (sender instanceof Player) {
                sendMessage("&7&oPlease note that you probably have the bypass");
                sendMessage("&7&opermission, so the ban may not apply to you.");
            }
            plugin.getListener().load(sender);
            return;
        }

        sendMessage("&cAn error occurred while banning the item.");
        sendMessage("&cCheck the console for more information.");
    }

    /**
     * Run the tab completion of the command.
     *
     * @return the tab completion of the command.
     */
    @Override
    public List<String> runTab() {
        if (args.length == 2)
            return StringUtil.copyPartialMatches(args[1], Arrays.stream(BanAction.values()).map(BanAction::getName).collect(Collectors.toList()), new ArrayList<>());
        return Arrays.asList("-w", "-m", "message");
    }
}
