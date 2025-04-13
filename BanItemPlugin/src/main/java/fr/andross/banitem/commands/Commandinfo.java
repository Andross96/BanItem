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
import fr.andross.banitem.items.CustomBannedItem;
import fr.andross.banitem.utils.MinecraftVersion;
import fr.andross.banitem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Sub command info
 *
 * @author Andross
 * @version 3.2
 */
public class Commandinfo extends BanCommand {
    private static final String CHECK = "\u2713";
    private static final String UNCHECK = "\u2A09";

    public Commandinfo(final BanItem pl, final CommandSender sender, final String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        // Not player?
        if (!(sender instanceof Player)) {
            sendMessage("Command IG only.");
            return;
        }

        // Permission?
        if (!sender.hasPermission("banitem.command.info")) {
            sendMessage(getNoPermMessage());
            return;
        }

        // Preparing variables
        final Player player = (Player) sender;
        final ItemStack item = Utils.getItemInHand(player);

        // Debug item?
        if (args.length > 1 && args[1].equalsIgnoreCase("debug")) {
            sendHeaderMessage("&6&lInfo - Debug item");

            final StringBuilder materialBuilder = new StringBuilder("&7Material name: ");
            materialBuilder.append("&e").append(item.getType().name().toLowerCase(Locale.ROOT));
            if (!MinecraftVersion.v13OrMore)
                materialBuilder.append(" &o(ID: ").append(item.getType().getId()).append(")");
            sendMessage(materialBuilder.toString());

            sendMessage("&7Durability: &e" + item.getDurability() + "/" + item.getType().getMaxDurability());
            sendMessage("&7ItemMeta: " + (item.hasItemMeta() ? "&a" + CHECK : "&c" + UNCHECK));
            if (item.hasItemMeta()) {
                final ItemMeta itemMeta = item.getItemMeta();
                if (itemMeta != null) {
                    itemMeta.serialize().forEach((key, value) -> sendMessage("  &7" + key + ": &e" + value));
                }
            }

            if (plugin.getServer().getPluginManager().isPluginEnabled("NBTAPI")) {
                sendMessage("&7NBTAPI: &e" + new de.tr7zw.nbtapi.NBTItem(item));
            } else {
                sendMessage("&7NBTAPI: &c" + UNCHECK);
            }

            sendMessage("&7Bukkit version: " + Bukkit.getVersion());
            return;
        }

        // Displaying item info
        sendHeaderMessage("&6&lInfo");

        // Displaying material type
        final String materialName = item.getType().name().toLowerCase();
        sendMessage("&7Material name: &e" + materialName);

        // Displaying if it's a meta item
        final BannedItem bannedItem = new BannedItem(item);
        final String metaItemName = plugin.getBanDatabase().getMetaItems().getKey(bannedItem);
        if (metaItemName != null) {
            sendMessage("&7Meta item name: &e" + metaItemName);
        }

        // Displaying matching custom items
        final List<CustomBannedItem> customItems = plugin.getBanDatabase().getCustomItems().values()
                .stream()
                .filter(ci -> ci.matches(bannedItem))
                .collect(Collectors.toList());
        if (!customItems.isEmpty()) {
            sendMessage("&7Matching custom items: &e" +
                    customItems.stream()
                            .map(CustomBannedItem::getName)
                            .collect(Collectors.joining(",")));
        }

        sendMessage("&7Permission example:");
        sendMessage(" &7>> &ebanitem.bypass." + player.getWorld().getName().toLowerCase() + "." + (metaItemName != null ? metaItemName.toLowerCase() : materialName) + ".action.*");
    }

    @Nullable
    @Override
    public List<String> runTab() {
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], Collections.singletonList("debug"), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
