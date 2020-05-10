package fr.andross.banitem.Utils.Debug;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Options.BanOption;
import fr.andross.banitem.Utils.Item.MetaType;
import fr.andross.banitem.Utils.Listable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A debug class, which can handle and display the nodes
 * Mainly used when loading the plugin, to display any error
 * @version 2.1.2
 * @author Andross
 */
public final class Debug implements Cloneable {
    private final BanItem pl;
    private final CommandSender sender;
    private List<DebugMessage> nodes = new ArrayList<>();

    public Debug(@NotNull final BanItem pl, @NotNull final CommandSender sender, final DebugMessage... dm) {
        this.pl = pl;
        this.sender = sender;
        add(dm);
    }

    /**
     * Add a node
     * @param type type of node
     * @param node message
     * @return this object
     */
    public Debug add(@Nullable final Listable.Type type, @NotNull final String node) {
        this.nodes.add(new DebugMessage(type, node));
        return this;
    }

    /**
     * Add a node
     * @param dm nodes with messages
     * @return this object
     */
    public Debug add(@NotNull final DebugMessage... dm) {
        this.nodes.addAll(Arrays.asList(dm));
        return this;
    }

    /**
     * List of nodes
     * @return list of nodes with their respectives messages
     */
    @NotNull
    public List<DebugMessage> getNodes() {
        return nodes;
    }

    /**
     * Set all nodes
     * @param nodes nodes with their respectives messages
     */
    public void setNodes(@NotNull final List<DebugMessage> nodes) {
        this.nodes = nodes;
    }

    /**
     * A simple message with the debug result
     * @return a simple message with the debug result
     */
    public String getSimpleDebug() {
        final StringBuilder sb = new StringBuilder(pl.getUtils().getPrefix() + "&7[");

        for (int i = 0; i < nodes.size(); i++) {
            final String node = nodes.get(i).getNode();
            if (i == nodes.size() - 1) sb.append("&7] ").append(node);
            else sb.append(node);
            if (i < nodes.size() - 2) sb.append(" >> ");
        }

        return pl.getUtils().color(sb.toString());
    }

    /**
     * A list of messages with nodes and the detailled debug message
     * @return a list of messages with nodes and a more detailled error message
     */
    public List<String> getBetterDebug() {
        final List<String> messages = new ArrayList<>();
        messages.add("&c------------------------");
        messages.add(pl.getUtils().getPrefix() + "&cError:");
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
                            messages.add("&7>> " + Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.joining(",")));
                            continue;
                        }
                        case OPTION: {
                            messages.add("&7This option is unknown. Valid options:");
                            messages.add("&7>> " + pl.getUtils().getOptions().stream().map(BanOption::getName).collect(Collectors.joining(",")));
                            continue;
                        }
                        case ITEM: {
                            messages.add("&7This material or custom item is unknown.");
                            messages.add("&7In game, you can use '/bi info' with your item in hand to get this info.");
                            continue;
                        }
                        case ENTITY: {
                            messages.add("&7This entity is unknown. Valid entities:");
                            messages.add("&7>> " + pl.getUtils().getEntities().stream().map(EntityType::name).map(String::toLowerCase).collect(Collectors.joining(",")));
                            continue;
                        }
                        case GAMEMODE: {
                            messages.add("&7This gamemode is unknown. Valid gamemodes:");
                            messages.add("&7>> " + pl.getUtils().getGamemodes().stream().map(GameMode::name).map(String::toLowerCase).collect(Collectors.joining(",")));
                            continue;
                        }
                        case INVENTORY: {
                            messages.add("&7This inventory type is unknown. Valid inventory types:");
                            messages.add("&7>> " + pl.getUtils().getInventories().stream().map(InventoryType::name).map(String::toLowerCase).collect(Collectors.joining(",")));
                            continue;
                        }
                        case METADATA: {
                            messages.add("&7This metadata is unknown. Valid metadatas:");
                            messages.add("&7>> " + pl.getUtils().getMetas().stream().map(MetaType::name).map(String::toLowerCase).collect(Collectors.joining(",")));
                            continue;
                        }
                        case METADATA_ENCHANTMENT: {
                            messages.add("&7This enchantment is unknown. The synthax is Enchantment:Level");
                            messages.add("&7Valid enchantments:");
                            messages.add("&7>> " + pl.getUtils().getEnchantments().stream().map(Enchantment::getName).map(String::toLowerCase).collect(Collectors.joining(",")));
                            continue;
                        }
                        case METADATA_POTION: {
                            messages.add("&7This potion is unknown. Valid potions:");
                            messages.add("&7>> " + pl.getUtils().getPotions().stream().map(PotionType::name).map(String::toLowerCase).collect(Collectors.joining(",")));
                            continue;
                        }
                    }
                }

            messages.add(sb.toString());
        }

        messages.add("&c------------------------");
        return messages.stream().map(pl.getUtils()::color).collect(Collectors.toList());
    }

    /**
     * Send this debug message to the sender
     */
    public void sendDebug() {
        if (!pl.getBanConfig().isBetterDebug()) sender.sendMessage(getSimpleDebug());
        else getBetterDebug().forEach(sender::sendMessage);
    }

    @Override
    public Debug clone() {
        try {
            final Debug d = (Debug) super.clone();
            d.setNodes(new ArrayList<>(nodes));
            return d;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }
}
