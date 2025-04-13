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
package fr.andross.banitem.utils.debug;

import fr.andross.banitem.BanConfig;
import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.items.meta.MetaType;
import fr.andross.banitem.utils.Chat;
import fr.andross.banitem.utils.enchantments.EnchantmentHelper;
import fr.andross.banitem.utils.list.ListType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A debug class, which can handle and display the nodes.
 * Mainly used when loading the plugin, to debug errors.
 *
 * @author Andross
 * @version 3.1
 */
public final class Debug implements Cloneable {
    private final BanConfig banConfig;
    private final CommandSender sender;
    private List<DebugMessage> nodes = new ArrayList<>();

    public Debug(@NotNull final BanConfig banConfig,
                 @NotNull final CommandSender sender,
                 final DebugMessage... dm) {
        this.banConfig = banConfig;
        this.sender = sender;
        if (dm != null) add(dm);
    }

    /**
     * Add a debug node.
     *
     * @param type type of node
     * @param node message
     * @return this object
     */
    public Debug add(@Nullable final ListType type, @NotNull final String node) {
        this.nodes.add(new DebugMessage(type, node));
        return this;
    }

    /**
     * Add a debug node.
     *
     * @param node message
     * @return this object
     */
    public Debug add(@NotNull final String node) {
        this.nodes.add(new DebugMessage(node));
        return this;
    }

    /**
     * Add a debug node.
     *
     * @param dm nodes with messages
     * @return this object
     */
    public Debug add(@NotNull final DebugMessage... dm) {
        this.nodes.addAll(Arrays.asList(dm));
        return this;
    }

    /**
     * List of debug nodes.
     *
     * @return list of nodes with their respective messages
     */
    @NotNull
    public List<DebugMessage> getNodes() {
        return nodes;
    }

    /**
     * Set all debug nodes.
     *
     * @param nodes nodes with their respective messages
     */
    public void setNodes(@NotNull final List<DebugMessage> nodes) {
        this.nodes = nodes;
    }

    /**
     * A simple message with the debug result.
     *
     * @return a simple message with the debug result
     */
    public String getSimpleDebug() {
        final StringBuilder sb = new StringBuilder(banConfig.getPrefix() + "&7[");

        for (int i = 0; i < nodes.size(); i++) {
            final String node = nodes.get(i).getNode();
            if (i == nodes.size() - 1) sb.append("&7] ").append(node);
            else sb.append(node);
            if (i < nodes.size() - 2) sb.append(" >> ");
        }

        return Chat.color(sb.toString());
    }

    /**
     * A list of messages with nodes and the detailed debug message.
     *
     * @return a list of messages with nodes and a more detailed error message
     */
    public List<String> getBetterDebug() {
        final List<String> messages = new ArrayList<>();
        messages.add("&c------------------------");
        messages.add(banConfig.getPrefix() + "&cError:");
        int i = 0;
        for (final DebugMessage dm : nodes) {
            i++;
            final StringBuilder sb = new StringBuilder();
            if (i != nodes.size()) for (int j = 0; j < i; j++) sb.append(" ");
            sb.append("&7&o>> ");
            sb.append(i == nodes.size() - 1 ? "&6" : "&2");
            sb.append(dm.getNode());
            // Commenting last node
            if (i == nodes.size())
                if (dm.getType() != null) {
                    messages.add(sb.toString());
                    switch (dm.getType()) {
                        case WORLD: {
                            messages.add("&7This world is unknown. Valid worlds:");
                            messages.add("&7>> " + Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.joining(",", "", ".")));
                            continue;
                        }
                        case ACTION: {
                            messages.add("&7This action is unknown. Valid actions:");
                            messages.add("&7>> " + Arrays.stream(BanAction.values()).map(BanAction::getName).collect(Collectors.joining(",", "", ".")));
                            continue;
                        }
                        case ACTIONDATA: {
                            messages.add("&7This action data is unknown.");
                            continue;
                        }
                        case ITEM: {
                            messages.add("&7This item is unknown.");
                            messages.add("&7In game, you can use '/bi info' with your item in hand to get this info.");
                            continue;
                        }
                        case ENTITY: {
                            messages.add("&7This entity is unknown. Valid entities:");
                            messages.add("&7>> " + Arrays.stream(EntityType.values()).map(EntityType::name).map(String::toLowerCase).collect(Collectors.joining(",", "", ".")));
                            continue;
                        }
                        case GAMEMODE: {
                            messages.add("&7This gamemode is unknown. Valid gamemodes:");
                            messages.add("&7>> " + Arrays.stream(GameMode.values()).map(GameMode::name).map(String::toLowerCase).collect(Collectors.joining(",", "", ".")));
                            continue;
                        }
                        case INVENTORY: {
                            messages.add("&7This inventory type is unknown. Valid inventory types:");
                            messages.add("&7>> " + Arrays.stream(InventoryType.values()).map(InventoryType::name).map(String::toLowerCase).collect(Collectors.joining(",", "", ".")));
                            continue;
                        }
                        case METATYPE: {
                            messages.add("&7This meta type is unknown. Valid meta type:");
                            messages.add("&7>> " + Arrays.stream(MetaType.values()).map(MetaType::name).map(String::toLowerCase).collect(Collectors.joining(",", "", ".")));
                            continue;
                        }
                        case ENCHANTMENT: {
                            messages.add("&7This enchantment is unknown. Valid enchantments:");
                            messages.add("&7>> " + EnchantmentHelper.getEnchantmentsNames().stream().collect(Collectors.joining(",", "", ".")));
                            continue;
                        }
                        case REGION: {
                            messages.add("&7This region is unknown.");
                            continue;
                        }
                    }
                }

            messages.add(sb.toString());
        }

        messages.add("&c------------------------");
        return messages.stream().map(Chat::color).collect(Collectors.toList());
    }

    /**
     * Send this debug message to the sender.
     */
    public void sendDebug() {
        if (!banConfig.getConfig().getBoolean("debug.errors")) {
            sender.sendMessage(sender instanceof Player ? getSimpleDebug() : Chat.stripColors(getSimpleDebug()));
        } else {
            if (sender instanceof Player) {
                getBetterDebug().forEach(sender::sendMessage);
            } else {
                getBetterDebug().stream().map(Chat::stripColors).forEach(sender::sendMessage);
            }
        }
    }

    @Override
    public Debug clone() {
        try {
            final Debug d = (Debug) super.clone();
            d.setNodes(new ArrayList<>(nodes));
            return d;
        } catch (final CloneNotSupportedException e) {
            throw new Error(e);
        }
    }
}
