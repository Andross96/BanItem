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
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.utils.Utils;
import fr.andross.banitem.utils.list.Listable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Sub command remove.
 *
 * @author Andross
 * @version 3.1
 */
public class Commandremove extends BanCommand {

    /**
     * Constructor of the /banitem remove command.
     *
     * @param plugin The ban item plugin instance
     * @param sender The command sender
     * @param args   The command arguments used by the command sender
     */
    public Commandremove(final BanItem plugin, final CommandSender sender, final String[] args) {
        super(plugin, sender, args);
    }

    /**
     * Run the command.
     */
    @Override
    public void run() {
        // Permission?
        if (!sender.hasPermission("banitem.command.remove")) {
            sendMessage(getNoPermMessage());
            return;
        }

        final List<Material> materials = new ArrayList<>();
        final List<World> worlds = new ArrayList<>();

        // Set materials?
        for (int i = 0; i < args.length; i++) {
            if (args[i].toLowerCase(Locale.ROOT).startsWith("-m")) {
                if (args.length <= i + 1) {
                    sendHeaderMessage("&6&lRemove");
                    sendMessage("&cInvalid material(s) syntax. Must be &e-m material1,material2...");
                    return;
                }

                final String material = args[i + 1];
                materials.addAll(Listable.getMaterials(material, null));
                if (material.isEmpty()) {
                    sendHeaderMessage("&6&lRemove");
                    sendMessage("&cInvalid material(s) entered: &e" + material);
                    return;
                }
                break;
            }
        }

        // Set worlds?
        for (int i = 0; i < args.length; i++) {
            if (args[i].toLowerCase(Locale.ROOT).startsWith("-w")) {
                if (args.length <= i + 1) {
                    sendHeaderMessage("&6&lRemove");
                    sendMessage("&cInvalid world(s) syntax. Must be &e-w worldName,worldName2...");
                    return;
                }

                final String world = args[i + 1];
                worlds.addAll(Listable.getWorlds(world, null));
                if (worlds.isEmpty()) {
                    sendHeaderMessage("&6&lRemove");
                    sendMessage("&cInvalid world(s) entered: &e" + world);
                    sendMessage("&7Valid worlds: &o" + Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.joining(",", "", "&7.")));
                    return;
                }
                break;
            }
        }

        // Checking materials
        if (materials.isEmpty()) {
            // Console?
            if (!(sender instanceof Player)) {
                sendHeaderMessage("Remove");
                sendMessage("You must enter the material(s) which will be unbanned.");
                sendMessage("Example: /bi remove -m stone -w world");
                return;
            }
            materials.add(Utils.getItemInHand((Player) sender).getType());
        }

        // Checking worlds
        if (worlds.isEmpty()) {
            // Console?
            if (!(sender instanceof Player)) {
                sendHeaderMessage("Remove");
                sendMessage("You must enter the world(s) in which the ban will be unbanned.");
                sendMessage("Example: /bi remove -m stone -w world");
                return;
            }
            worlds.add(((Player) sender).getWorld());
        }

        final World[] worldsArray = worlds.toArray(new World[0]);
        plugin.getApi().removeFromBlacklist(materials.stream().map(BannedItem::new).collect(Collectors.toList()), worldsArray);
        sendHeaderMessage("&6&lRemove");
        if (materials.size() == 1) {
            sendMessage("&aThe item is successfully unbanned.");
        } else {
            sendMessage("&aThe items are successfully unbanned.");
        }
    }

    /**
     * Run the tab completion of the command.
     *
     * @return the tab completion of the command.
     */
    @Override
    public List<String> runTab() {
        return Collections.emptyList();
    }
}
