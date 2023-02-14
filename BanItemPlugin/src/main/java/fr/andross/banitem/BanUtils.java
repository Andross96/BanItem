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
package fr.andross.banitem;

import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.actions.BanData;
import fr.andross.banitem.actions.BanDataType;
import fr.andross.banitem.database.Blacklist;
import fr.andross.banitem.events.DeleteBannedItemEvent;
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.utils.Chat;
import fr.andross.banitem.utils.Utils;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.enchantments.EnchantmentWrapper;
import fr.andross.banitem.utils.list.ListType;
import fr.andross.banitem.utils.list.Listable;
import fr.andross.banitem.utils.scanners.WearScanner;
import fr.andross.banitem.utils.scanners.illegalstack.IllegalStackBlockType;
import fr.andross.banitem.utils.scanners.illegalstack.IllegalStackScanner;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * An utility class for the plugin
 * @version 3.4
 * @author Andross
 */
public final class BanUtils {
    private final BanItem pl;
    private final WearScanner wearScanner;
    private final IllegalStackScanner illegalStackScanner;
    private final Map<String, String> commandsAliases = new HashMap<>();
    private final Map<UUID, Long> messagesCooldown = new HashMap<>();
    private final Set<UUID> logging = new HashSet<>();

    BanUtils(final BanItem pl) {
        this.pl = pl;
        this.wearScanner = new WearScanner(pl, this);
        this.illegalStackScanner = new IllegalStackScanner(pl, this);
        commandsAliases.put("mi", "metaitem");
        commandsAliases.put("rl", "reload");
    }

    /**
     * Get a map of actions and actions data from a section
     * @param worlds list of worlds
     * @param section section
     * @param d debug
     * @return a map containing the ban actions and their respective data from the ConfigurationSection
     */
    @NotNull
    public Map<BanAction, BanActionData> getBanActionsFromItemSection(@NotNull final List<World> worlds, @Nullable final ConfigurationSection section, @NotNull final Debug d) {
        final Map<BanAction, BanActionData> actions = new HashMap<>();
        if (section == null) return actions;
        final List<BanAction> ignoredActions = new ArrayList<>();
        for (final String key : section.getKeys(false)) {
            for (String action : key.toUpperCase().trim().replaceAll("\\s+", "").split(",")) {
                final Debug newDebug = d.clone();
                try {
                    final BanActionData bo = getBanActionsForItem(worlds, section, key, newDebug.add(ListType.ACTION, key));
                    if (action.equals("*")) {
                        for (final BanAction banAction : BanAction.values()) actions.put(banAction, bo);
                        continue;
                    }
                    final boolean remove = action.startsWith("!");
                    if (remove) action = action.substring(1);
                    final BanAction banAction = BanAction.valueOf(action);
                    if (remove) ignoredActions.add(banAction); else actions.put(banAction, bo);
                } catch (final Exception e) {
                    newDebug.add(ListType.ACTION, "&cUnknown action &e&l" + action + "&c.").sendDebug();
                }
            }
        }
        // Removing ignored actions
        ignoredActions.forEach(actions::remove);

        return actions;
    }

    /**
     * Get ban actions data for a section
     * @param worlds list of worlds, used for regions
     * @param itemSection the action section
     * @param key the current data key
     * @param d debug
     * @return actions data from the section of the specific action
     */
    @NotNull
    public BanActionData getBanActionsForItem(@NotNull final List<World> worlds, @NotNull final ConfigurationSection itemSection, @NotNull final String key, @NotNull final Debug d) {
        final BanActionData banActionData = new BanActionData();
        final ConfigurationSection section = itemSection.getConfigurationSection(key);
        if (section == null) {
            final List<String> messages = Listable.getStringList(itemSection.get(key));
            if (!messages.isEmpty()) banActionData.getMap().put(BanDataType.MESSAGE, messages.stream().filter(Utils::isNotNullOrEmpty).map(Chat::color).collect(Collectors.toList()));
            return banActionData;
        }

        // Handling data
        for (final String actionData : section.getKeys(false)) {
            final BanDataType banDataType;
            try {
                banDataType = BanDataType.valueOf(actionData.toUpperCase().replace("-", "_"));
            } catch (final Exception e) {
                d.clone().add(ListType.ACTIONDATA, "&cUnknown action data &e&l" + actionData + "&c.").sendDebug();
                continue;
            }

            final Object o = section.get(actionData);
            if (o == null) continue; // should not happen, but, well..

            switch (banDataType) {
                case COOLDOWN:
                    banActionData.getMap().put(BanDataType.COOLDOWN, section.getLong("cooldown"));
                    break;

                case ENTITY: {
                    final List<EntityType> list = Listable.getList(ListType.ENTITY, o, d.add(ListType.ENTITY, actionData));
                    if (!list.isEmpty())
                        banActionData.getMap().put(BanDataType.ENTITY, EnumSet.copyOf(list));
                    break;
                }

                case ENCHANTMENT: {
                    final List<EnchantmentWrapper> list = Listable.getList(ListType.ENCHANTMENT, o, d.add(ListType.ENCHANTMENT, actionData));
                    if (!list.isEmpty())
                        banActionData.getMap().put(BanDataType.ENCHANTMENT, new HashSet<>(list));
                    break;
                }

                case GAMEMODE: {
                    final List<GameMode> list = Listable.getList(ListType.GAMEMODE, o, d.add(ListType.GAMEMODE, actionData));
                    if (!list.isEmpty())
                        banActionData.getMap().put(BanDataType.GAMEMODE, EnumSet.copyOf(list));
                    break;
                }

                case INVENTORY_FROM:
                case INVENTORY_TO: {
                    final List<InventoryType> list = Listable.getList(ListType.INVENTORY, o, d.add(ListType.INVENTORY, actionData));
                    if (!list.isEmpty())
                        banActionData.getMap().put(banDataType, EnumSet.copyOf(list));
                    break;
                }

                case LOG:
                    banActionData.getMap().put(BanDataType.LOG, o);
                    break;

                case MATERIAL: {
                    final List<BannedItem> list = Listable.getList(ListType.ITEM, o, d.add(ListType.ITEM, actionData));
                    if (!list.isEmpty())
                        banActionData.getMap().put(BanDataType.MATERIAL, list.stream().map(BannedItem::getType).collect(Collectors.toSet()));
                    break;
                }

                case MESSAGE: {
                    final List<String> messages = Listable.getStringList(o);
                    if (!messages.isEmpty())
                        banActionData.getMap().put(BanDataType.MESSAGE, messages.stream().filter(Utils::isNotNullOrEmpty).map(Chat::color).collect(Collectors.toList()));
                    break;
                }

                case PERMISSION: {
                    final String permission = String.valueOf(o);
                    if (permission != null)
                        banActionData.getMap().put(BanDataType.PERMISSION, permission.toLowerCase(Locale.ROOT));
                    break;
                }

                case REGION: {
                    if (!pl.getHooks().isWorldGuardEnabled()) {
                        d.clone().add(ListType.REGION, "&cUsed region metadata, but WorldGuard is not available.").sendDebug();
                        continue;
                    }
                    final List<com.sk89q.worldguard.protection.regions.ProtectedRegion> regions = Listable.getRegionsList(pl, o, d.add(ListType.REGION, actionData), worlds);
                    if (!regions.isEmpty())
                        banActionData.getMap().put(BanDataType.REGION, new HashSet<>(regions));
                    break;
                }

                case BANNABLE: {
                    // The moment it is in the config, we assume a player is meant to be banned
                    banActionData.getMap().put(BanDataType.BANNABLE, true);
                    break;
                }

                case RUN: {
                    final List<String> commands = Listable.getStringList(o);
                    if (!commands.isEmpty())
                        banActionData.getMap().put(BanDataType.RUN, commands);
                    break;
                }
            }
        }

        return banActionData;
    }

    /**
     * Method to check and delete banned item from the player opened inventories
     * @param player any player
     */
    public void deleteItemFromInventoryView(@NotNull final Player player) {
        // Op or all permissions?
        if (player.isOp() || player.hasPermission("banitem.bypass.*")) return;

        // Getting blacklist
        final Blacklist blacklist = pl.getBanDatabase().getBlacklist();
        if (!blacklist.containsKey(player.getWorld())) return; // nothing banned in this world

        // Checking!
        final Inventory[] invs;
        final InventoryView iv = player.getOpenInventory();
        final Inventory top = iv.getTopInventory();
        final Inventory bottom = iv.getBottomInventory();
        if (top.equals(bottom))
            invs = new Inventory[] { top };
        else
            invs = new Inventory[] { top, bottom };

        for (final Inventory inv : invs) {
            // Ignored inventory?
            final String name = Chat.uncolor(iv.getTitle());
            if (pl.getBanConfig().getIgnoredInventoryTitles().contains(name)) continue;

            for (int i = 0; i < inv.getSize(); i++) {
                final ItemStack item = inv.getItem(i);
                if (Utils.isNullOrAir(item)) continue;
                final BannedItem bannedItem = new BannedItem(item);
                if (pl.getApi().isBanned(player, player.getLocation(), bannedItem, BanAction.DELETE)) {
                    if (pl.getConfig().getBoolean("api.deletebanneditemevent")) {
                        final DeleteBannedItemEvent event = new DeleteBannedItemEvent(player, bannedItem);
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) continue;
                    }

                    inv.clear(i);
                }
            }
        }
    }

    /**
     * This method is used to send a ban message to player, if exists.
     * Mainly used for blacklist
     * @param player player involved in the action
     * @param itemName the item name involved
     * @param action the ban action <i>(used for log)</i>
     * @param data the ban data <i>(containing the messages)</i>
     */
    public void sendMessage(@NotNull final Player player, @NotNull final String itemName, @NotNull final BanAction action, @Nullable final BanActionData data) {
        if (data == null) return; // no message neither log

        // Checking action cooldown, to prevent spam
        if (action == BanAction.PICKUP || action == BanAction.HOLD || action == BanAction.SMITH) {
            // Adding in cooldown
            if (!messagesCooldown.containsKey(player.getUniqueId()))
                messagesCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            else {
                final long lastTime = messagesCooldown.get(player.getUniqueId());
                if (lastTime + 1000L > System.currentTimeMillis()) return; // not sending message again
                messagesCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }

        // Getting datas
        final List<String> message = data.getData(BanDataType.MESSAGE);
        final boolean log = data.getLog();

        // Logging?
        if (log && !logging.isEmpty()) {
            // Preparing message
            final String m = Chat.color(pl.getBanConfig().getPrefix() + // prefix
                        player.getName() + " " + // player name
                        "(" + player.getWorld().getName() + ") " + // world
                        "[" + itemName + "]: " + // item name
                        action.name()); // action

            // Sending log message
            logging.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(t -> t.sendMessage(m));
        }

        // No message set
        if (message == null) return;

        // Sending message & animation
        message.forEach(player::sendMessage);
        pl.getBanConfig().getAnimation().runAnimation(player);
    }

    /**
     * This method is used to send a ban message to player, if exists.
     * Mainly used for whitelist
     * @param player send the message to
     * @param action the ban action <i>(used for log)</i>
     * @param messages list of messages
     */
    public void sendMessage(@NotNull final Player player, @NotNull final BanAction action, @NotNull final List<String> messages) {
        if (messages.isEmpty()) return; // no message

        // Checking pick up cooldown, to prevent spam
        if (action == BanAction.PICKUP || action == BanAction.HOLD) {
            // Adding in cooldown
            if (!messagesCooldown.containsKey(player.getUniqueId()))
                messagesCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            else {
                final long lastTime = messagesCooldown.get(player.getUniqueId());
                if (lastTime + 1000L > System.currentTimeMillis()) return; // not sending message again
                messagesCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }

        // Sending message & animation
        messages.forEach(player::sendMessage);
        pl.getBanConfig().getAnimation().runAnimation(player);
    }

    /**
     * Method to check if the player has the bypass permission for either the item <i>(material name)</i> or custom name
     * @param player player to check
     * @param itemName name of the item
     * @param action action name
     * @param data additional data to check
     * @return true if the player has the permission to bypass the ban, otherwise false
     */
    public boolean hasPermission(@NotNull final Player player, @NotNull final String itemName, @NotNull final BanAction action, @Nullable final BanData... data) {
        final String world = player.getWorld().getName().toLowerCase();
        if (player.hasPermission("banitem.bypass.*")) return true;
        if (player.hasPermission("banitem.bypass." + world + ".*")) return true;
        if (player.hasPermission("banitem.bypass.allworlds.*")) return true;

        if (!Utils.isNullOrEmpty(data)) {
            if (player.hasPermission("banitem.bypass." + world + "." + itemName + "." + action.getName() + ".*")) return true;
            if (player.hasPermission("banitem.bypass.allworlds." + itemName + "." + action.getName() + ".*")) return true;
            if (player.hasPermission("banitem.bypass." + world + ".allitems." + action.getName() + ".*")) return true;
            if (player.hasPermission("banitem.bypass.allworlds.allitems." + action.getName() + ".*")) return true;
            for (final BanData bd : data) {
                final String dataName = String.valueOf(bd.getObject()).toLowerCase(Locale.ROOT);
                if (player.hasPermission("banitem.bypass." + world + "." + itemName + "." + action.getName() + "." + dataName)) return true;
                if (player.hasPermission("banitem.bypass.allworlds." + itemName + "." + action.getName() + "." + dataName)) return true;
                if (player.hasPermission("banitem.bypass." + world + ".allitems." + action.getName() + "." + dataName)) return true;
                if (player.hasPermission("banitem.bypass.allworlds.allitems." + action.getName() + "." + dataName)) return true;
            }
        } else {
            if (player.hasPermission("banitem.bypass." + world + "." + itemName + ".*")) return true;
            if (player.hasPermission("banitem.bypass.allworlds." + itemName + "." + ".*")) return true;
            if (player.hasPermission("banitem.bypass." + world + "." + itemName + "." + action.getName())) return true;
            if (player.hasPermission("banitem.bypass.allworlds." + itemName + "." + action.getName())) return true;

            if (player.hasPermission("banitem.bypass." + world + ".allitems.*")) return true;
            if (player.hasPermission("banitem.bypass.allworlds.allitems." + ".*")) return true;
            if (player.hasPermission("banitem.bypass." + world + ".allitems." + action.getName())) return true;
            if (player.hasPermission("banitem.bypass.allworlds.allitems." + action.getName())) return true;
        }

        return false;
    }

    /**
     * Get a friendly string of remaining time
     * @param time time in millis
     * @return a friendly string of remaining time
     */
    @NotNull
    public String getCooldownString(final long time) {
        if (time > 0 && time < 1000) return "0." + Integer.parseInt(Long.toString(time).substring(0, 1)) + "s";
        if (time <= 0) return "1s"; // soon

        final long days = TimeUnit.MILLISECONDS.toDays(time);
        final long hours = TimeUnit.MILLISECONDS.toHours(time) % 24;
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;

        final StringBuilder sb = new StringBuilder();
        if (days != 0) sb.append(days).append("d");
        if (hours != 0) sb.append(hours).append("h");
        if (minutes != 0) sb.append(minutes).append("m");
        if (seconds != 0) sb.append(seconds).append("s");

        return sb.length() == 0 ? "1s" : sb.toString();
    }

    /**
     * Used to check a player armor inventory
     * @param p player
     */
    public void checkPlayerArmors(final Player p) {
        final EntityEquipment ee = p.getEquipment();
        if (ee == null) return;

        final boolean primaryThread = Bukkit.isPrimaryThread();
        final ItemStack helmet = ee.getHelmet();
        if (!Utils.isNullOrAir(helmet) && pl.getApi().isBanned(p, p.getLocation(), helmet, primaryThread, BanAction.WEAR)) {
            if (!primaryThread) {
                Bukkit.getScheduler().runTask(pl, () -> checkPlayerArmors(p));
                return;
            }

            giveItemBack(p, helmet);
            ee.setHelmet(null);
        }

        final ItemStack chestplate = ee.getChestplate();
        if (!Utils.isNullOrAir(chestplate) && pl.getApi().isBanned(p, p.getLocation(), chestplate, primaryThread, BanAction.WEAR)) {
            if (!primaryThread) {
                Bukkit.getScheduler().runTask(pl, () -> checkPlayerArmors(p));
                return;
            }
            giveItemBack(p, chestplate);
            ee.setChestplate(null);
        }

        final ItemStack leggings = ee.getLeggings();
        if (!Utils.isNullOrAir(leggings) && pl.getApi().isBanned(p, p.getLocation(), leggings, primaryThread, BanAction.WEAR)) {
            if (!primaryThread) {
                Bukkit.getScheduler().runTask(pl, () -> checkPlayerArmors(p));
                return;
            }
            giveItemBack(p, leggings);
            ee.setLeggings(null);
        }

        final ItemStack boots = ee.getBoots();
        if (!Utils.isNullOrAir(boots) && pl.getApi().isBanned(p, p.getLocation(), boots, primaryThread, BanAction.WEAR)) {
            if (!primaryThread) {
                Bukkit.getScheduler().runTask(pl, () -> checkPlayerArmors(p));
                return;
            }
            giveItemBack(p, boots);
            ee.setBoots(null);
        }
    }

    /**
     * Used to check if a player has an illegal stacked item
     * @param p player
     */
    public void checkPlayerIllegalStacks(@NotNull final Player p) {
        final PlayerInventory inv = p.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            final ItemStack item = inv.getItem(i);
            if (Utils.isNullOrAir(item)) continue;

            // Have to check this item?
            final int maxStack = illegalStackScanner.getItems().containsKey(item.getType()) ?
                    illegalStackScanner.getItems().get(item.getType()).getAmount() : item.getMaxStackSize();
            if (maxStack <= 0) continue;

            if (item.getAmount() > maxStack) {
                // Illegal stack!
                if (!Bukkit.isPrimaryThread()) {
                    Bukkit.getScheduler().runTask(pl, () -> checkPlayerIllegalStacks(p));
                    return;
                }

                if (p.hasPermission("banitem.bypassillegalstack")) continue;

                // Blocking
                final IllegalStackBlockType blockType = illegalStackScanner.getItems().containsKey(item.getType()) ? illegalStackScanner.getItems().get(item.getType()).getBlockType() : illegalStackScanner.getDefaultBlockType();
                if (blockType == null) continue;

                switch (blockType) {
                    case DELETE: // totally remove the item
                        inv.setItem(i, null);
                        break;
                    case DELETEMORE: case SPLIT: // delete whats more or split it and give it back to player
                        final int amountMore = item.getAmount() - maxStack;
                        item.setAmount(item.getAmount() - amountMore);
                        inv.setItem(i, item);
                        if (blockType == IllegalStackBlockType.SPLIT) {
                            for (int j = 0; j < amountMore; j++) {
                                final ItemStack newItem = item.clone();
                                newItem.setAmount(1);
                                giveItemBack(p, newItem);
                            }
                        }
                        break;
                }

            }
        }
    }

    /**
     * Method to give an item back to the player, drop it if the inventory is full
     * @param p the player
     * @param item the item
     */
    public void giveItemBack(@NotNull final Player p, @NotNull final ItemStack item) {
        final int freeSlot = p.getInventory().firstEmpty();
        // No empty space, dropping it, else adding it into inventory
        if (freeSlot == -1) p.getWorld().dropItemNaturally(p.getLocation(), item);
        else p.getInventory().setItem(freeSlot, item);
    }


    /**
     * Sending a prefixed and colored (if player) message to sender
     * @param sender sender
     * @param message message
     */
    public void sendMessage(@NotNull final CommandSender sender, @Nullable final String message) {
        if (message == null) return;
        final String finalMessage = pl.getBanConfig().getPrefix() + Chat.color(message);
        final boolean colorInConsole = pl.getBanConfig().getConfig().getBoolean("debug.colors-console");
        sender.sendMessage((colorInConsole || sender instanceof Player ? finalMessage : Chat.uncolor(finalMessage)));
    }

    /**
     * Send a message if an update is available
     * This <b>must</b> be executed async
     */
    public void checkForUpdate() {
        try {
            final HttpsURLConnection c = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=67701").openConnection();
            c.setRequestMethod("GET");
            final String lastVersion = new BufferedReader(new InputStreamReader(c.getInputStream())).readLine();
            if (!lastVersion.equals(pl.getDescription().getVersion()))
                sendMessage(Bukkit.getConsoleSender(), "&aA newer version (&lv" + lastVersion + "&a) is available!");
        } catch (final IOException e) {
            sendMessage(Bukkit.getConsoleSender(), "&cUnable to communicate with the spigot api to check for newer versions.");
        }
    }

    /**
     * Get the WearScanner handler
     * @return the wear scanner handler
     */
    @NotNull
    public WearScanner getWearScanner() {
        return wearScanner;
    }

    /**
     * Get the IllegalStackScanner handler
     * @return the illegal stack scanner handler
     */
    @NotNull
    public IllegalStackScanner getIllegalStackScanner() {
        return illegalStackScanner;
    }

    /**
     * Get the sub commands aliases
     * @return the sub commands aliases
     */
    @NotNull
    public Map<String, String> getCommandsAliases() {
        return commandsAliases;
    }

    /**
     * Get the messages cooldown map
     * @return map containing the cooldowns for messages
     */
    @NotNull
    public Map<UUID, Long> getMessagesCooldown() {
        return messagesCooldown;
    }

    /**
     * This map contains the players who activated the log in game with <i>/banitem log</i>
     * Players which log mode is activated will receive the logs messages for the banned items, if set in config
     * @return set of players uuid
     */
    @NotNull
    public Set<UUID> getLogging() {
        return logging;
    }
}
