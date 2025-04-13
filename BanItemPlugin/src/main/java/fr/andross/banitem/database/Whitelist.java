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
package fr.andross.banitem.database;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.andross.banitem.BanConfig;
import fr.andross.banitem.BanDatabase;
import fr.andross.banitem.BanItem;
import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.actions.BanData;
import fr.andross.banitem.actions.BanDataType;
import fr.andross.banitem.events.PlayerBanItemEvent;
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.utils.Chat;
import fr.andross.banitem.utils.PlaceholderApiCondition;
import fr.andross.banitem.utils.Utils;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.debug.DebugMessage;
import fr.andross.banitem.utils.hooks.IWorldGuardHook;
import fr.andross.banitem.utils.list.ListType;
import fr.andross.banitem.utils.list.Listable;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Map that contains whitelisted worlds.
 *
 * @author Andross
 * @version 3.3
 */
public class Whitelist extends HashMap<World, WhitelistedWorld> {
    /**
     * BanItem plugin instance.
     */
    private final BanItem plugin;

    /**
     * Constructor for a whitelist map.
     *
     * @param plugin       main instance
     * @param database the database instance
     * @param sender   {@link CommandSender} who to send the debug messages
     * @param section  {@link ConfigurationSection} which contains the blacklist node
     */
    public Whitelist(@NotNull final BanItem plugin,
                     @NotNull final BanDatabase database,
                     @NotNull final CommandSender sender,
                     @Nullable final ConfigurationSection section) {
        this.plugin = plugin;
        if (section == null) {
            return;
        }

        final BanConfig banConfig = plugin.getBanConfig();
        for (final String worldKey : section.getKeys(false)) { // Looping through worlds
            // Checking the world
            final List<World> worlds = Listable.getWorlds(worldKey, new Debug(banConfig, sender, new DebugMessage(null, banConfig.getConfigName()), new DebugMessage(null, "whitelist")));
            if (worlds.isEmpty()) {
                continue;
            }

            // Getting item info
            final ConfigurationSection itemsSection = section.getConfigurationSection(worldKey);
            if (itemsSection == null) {
                continue;
            }

            List<String> messages = null;
            List<BanAction> ignored = null;
            for (final String itemKey : itemsSection.getKeys(false)) {
                // Preparing debugger
                final Debug d = new Debug(banConfig, sender, new DebugMessage(null, banConfig.getConfigName()), new DebugMessage(null, "whitelist"), new DebugMessage(ListType.WORLD, worldKey), new DebugMessage(ListType.ITEM, itemKey));

                // Blocked message?
                if (itemKey.equalsIgnoreCase("message")) {
                    final List<String> message = Listable.getStringList(itemsSection.get(itemKey));
                    if (message.isEmpty()) {
                        continue;
                    }
                    messages = message.stream().filter(Objects::nonNull).map(Chat::color).collect(Collectors.toList());
                    continue;
                }

                // Ignored options?
                if (itemKey.equalsIgnoreCase("ignored")) {
                    final List<String> ignoredOptions = Listable.getStringList(itemsSection.get(itemKey));
                    if (ignoredOptions.isEmpty()) {
                        continue;
                    }
                    ignored = Listable.getList(ListType.ACTION, Listable.splitToList(ignoredOptions), d);
                    continue;
                }

                // Getting items
                final List<BannedItem> items = Listable.getItems(database, itemKey, d);
                if (items.isEmpty()) {
                    continue;
                }

                // Getting options for the item
                final Map<BanAction, BanActionData> actions = new EnumMap<>(BanAction.class);
                final ConfigurationSection actionsSection = itemsSection.getConfigurationSection(itemKey);
                if (actionsSection == null) {
                    final String optionsNames = itemsSection.getString(itemKey);
                    if (optionsNames == null) {
                        continue;
                    }
                    final List<BanAction> actionsList = Listable.getList(ListType.ACTION, optionsNames, d);
                    if (actionsList.isEmpty()) {
                        continue;
                    }
                    for (final BanAction action : actionsList) {
                        actions.put(action, new BanActionData());
                    }
                } else {
                    actions.putAll(plugin.getUtils().getBanActionsFromItemSection(worlds, actionsSection, d));
                }

                if (actions.isEmpty()) {
                    continue;
                }

                // Adding into the map
                for (final World w : worlds) {
                    for (final BannedItem item : items) {
                        addNewException(getOrCreateWhitelistedWorld(w, messages, ignored), item, actions);
                    }
                }
            }
        }
    }

    /**
     * This method will create a new whitelisted world, and add/replace it into the map.
     *
     * @param world    the bukkit world
     * @param messages list of "not allowed" messages
     * @param ignored  list of ignored actions
     * @return the new whitelisted world object
     */
    @NotNull
    public WhitelistedWorld createNewWhitelistedWorld(@NotNull final World world,
                                                      @Nullable final List<String> messages,
                                                      @Nullable final List<BanAction> ignored) {
        final WhitelistedWorld ww = new WhitelistedWorld(world, messages, ignored);
        put(world, ww);
        return ww;
    }

    /**
     * This method try to get an already existing whitelisted world, else create and put one.
     *
     * @param world    the bukkit world
     * @param messages list of "not allowed" messages
     * @param ignored  list of ignored actions
     * @return an existing whitelisted world object, otherwise a new one
     */
    public WhitelistedWorld getOrCreateWhitelistedWorld(@NotNull final World world,
                                                        @Nullable final List<String> messages,
                                                        @Nullable final List<BanAction> ignored) {
        return containsKey(world) ? get(world) : createNewWhitelistedWorld(world, messages, ignored);
    }

    /**
     * This will add a new exception <i>(allowed item)</i> into the WhitelistedWorld object.
     *
     * @param ww      whitelisted world, can be get with {@link Whitelist#getOrCreateWhitelistedWorld(World, List, List)}
     * @param item    the item
     * @param actions actions with their respective data
     */
    public void addNewException(@NotNull final WhitelistedWorld ww,
                                @NotNull final BannedItem item,
                                @NotNull final Map<BanAction, BanActionData> actions) {
        ww.addNewEntry(item, actions);
        put(ww.getWorld(), ww);
    }

    /**
     * Check if the item is whitelisted <i>(allowed)</i>.
     *
     * @param player      player involved
     * @param location    the effective location where the action occurs
     * @param item        the banned item
     * @param sendMessage send a message to the player if not allowed
     * @param action      the action
     * @param data        optional ban data
     * @return true if the item is whitelisted <i>(allowed)</i>, otherwise false
     */
    public boolean isWhitelisted(@NotNull final Player player,
                                 @Nullable final Location location,
                                 @NotNull final BannedItem item,
                                 final boolean sendMessage,
                                 @NotNull final BanAction action,
                                 @Nullable final BanData... data) {
        final WhitelistedWorld ww = get(player.getWorld());
        if (ww == null) {
            return true;
        }

        // Ignored action?
        if (ww.getIgnored().contains(action)) {
            return true;
        }

        /* Checking whitelist */
        final Map<BanAction, BanActionData> map = ww.get(item);
        if (map != null && !map.isEmpty() && map.containsKey(action)) {
            final BanActionData whitelisted = map.get(action);
            // Checking custom data
            if (Utils.isNullOrEmpty(data) || Arrays.stream(data).allMatch(whitelisted::contains)) {
                // Permission data?
                final String itemName = whitelisted.getMap().containsKey(BanDataType.CUSTOMNAME) ? String.valueOf(whitelisted.getMap().get(BanDataType.CUSTOMNAME)) : item.getType().name().toLowerCase(Locale.ROOT);
                if (whitelisted.getMap().containsKey(BanDataType.PERMISSION)) {
                    if (player.hasPermission((String) whitelisted.getMap().get(BanDataType.PERMISSION))) {
                        return true;
                    }
                } else {
                    // Bypass permission?
                    if (plugin.getUtils().hasPermission(player, itemName, action, data)) {
                        return true;
                    }
                }

                // Checking gamemode data?
                if (whitelisted.getMap().containsKey(BanDataType.GAMEMODE)) {
                    final Set<GameMode> set = whitelisted.getData(BanDataType.GAMEMODE);
                    if (set != null && !set.contains(player.getGameMode())) { // Gamemode not whitelisted
                        if (sendMessage) {
                            plugin.getUtils().sendMessage(player, itemName, action, whitelisted);
                        }
                        return false;
                    }
                }

                // Checking region data?
                if (whitelisted.getMap().containsKey(BanDataType.REGION)) {
                    final IWorldGuardHook hook = plugin.getHooks().getWorldGuardHook();
                    if (hook != null) {
                        final Set<ProtectedRegion> regions = whitelisted.getData(BanDataType.REGION);
                        if (regions != null && !regions.isEmpty()) {
                            final Set<ProtectedRegion> standingRegions = hook.getStandingRegions(location == null ? player.getLocation() : location);
                            if (regions.stream().noneMatch(standingRegions::contains)) {
                                if (sendMessage) {
                                    plugin.getUtils().sendMessage(player, itemName, action, whitelisted);
                                }
                                return false;
                            }
                        }
                    }
                }

                // Checking cooldown?
                if (whitelisted.getMap().containsKey(BanDataType.COOLDOWN)) {
                    final long cooldown = (long) whitelisted.getMap().get(BanDataType.COOLDOWN);
                    final Map<UUID, Long> cooldowns = whitelisted.getCooldowns();

                    // Not in cooldown? Adding!
                    if (!cooldowns.containsKey(player.getUniqueId())) {
                        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldown);
                        return true;
                    }

                    // Checking cooldown
                    final long playerCooldown = cooldowns.get(player.getUniqueId());

                    // Not in cooldown anymore?
                    if (playerCooldown < System.currentTimeMillis()) {
                        cooldowns.remove(player.getUniqueId()); // not in cooldown anymore, cleaning up'
                        return true;
                    }
                }

                // Placeholder API condition ?
                if (plugin.getHooks().isPlaceholderApiEnabled() &&
                        whitelisted.getMap().containsKey(BanDataType.PLACEHOLDERAPI_CONDITION)) {
                    final PlaceholderApiCondition placeholderApiCondition = (PlaceholderApiCondition) whitelisted.getMap().get(BanDataType.PLACEHOLDERAPI_CONDITION);
                    if (placeholderApiCondition.doesConditionMatch(player)) {
                        return true;
                    }
                }

                // Calling event?
                if (plugin.getBanConfig().getConfig().getBoolean("api.playerbanitemevent")) {
                    final PlayerBanItemEvent e = new PlayerBanItemEvent(player,
                            PlayerBanItemEvent.Type.WHITELIST,
                            item,
                            action,
                            whitelisted,
                            data);
                    plugin.getServer().getPluginManager().callEvent(e);
                    return !e.isCancelled();
                }

                // Run?
                if (whitelisted.getMap().containsKey(BanDataType.RUN)) {
                    final List<String> commands = whitelisted.getData(BanDataType.RUN);
                    if (commands != null) {
                        for (final String command : commands) {
                            String commandWithPlaceholderReplaced = command.replace("{player}", player.getName())
                                    .replace("{world}", player.getWorld().getName())
                                    .replace("{itemname}", itemName);

                            if (plugin.getHooks().isPlaceholderApiEnabled()) {
                                commandWithPlaceholderReplaced = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, commandWithPlaceholderReplaced);
                            }

                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandWithPlaceholderReplaced);
                        }
                    }
                }

                return true;
            }
        }

        if (sendMessage) {
            plugin.getUtils().sendMessage(player, action, ww.getMessages());
        }
        return false;
    }

    /**
     * This method is used to check if the item is whitelisted, not involving a player.
     * This method is mainly used to check dispensers <i>dispense</i> and hoppers <i>transfer</i>.
     *
     * @param world  bukkit world
     * @param item   the banned item
     * @param action the action
     * @param data   optional ban data
     * @return true if the item is whitelisted <i>(allowed)</i>, otherwise false
     */
    public boolean isWhitelisted(@NotNull final World world,
                                 @NotNull final BannedItem item,
                                 @NotNull final BanAction action,
                                 @Nullable final BanData... data) {
        final WhitelistedWorld ww = get(world);
        if (ww == null) {
            return true;
        }

        // Ignored action?
        if (ww.getIgnored().contains(action)) {
            return true;
        }

        /* Checking whitelist */
        // Checking by item (can include meta)?
        final Map<BanAction, BanActionData> map = ww.get(item);

        if (map != null && map.containsKey(action)) { // In whitelist
            final BanActionData whitelisted = map.get(action);
            return data == null || Arrays.stream(data).allMatch(whitelisted::contains);
        }
        return false;
    }

    /**
     * The total amount of items allowed, in all worlds.
     *
     * @return The total amount of items allowed, in all worlds
     */
    public int getTotalWhitelistedItems() {
        return values().stream().mapToInt(WhitelistedWorld::getTotalAmountOfItems).sum();
    }

}
