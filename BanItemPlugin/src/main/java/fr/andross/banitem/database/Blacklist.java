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

import fr.andross.banitem.BanConfig;
import fr.andross.banitem.BanDatabase;
import fr.andross.banitem.BanItem;
import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.actions.BanData;
import fr.andross.banitem.actions.BanDataType;
import fr.andross.banitem.database.items.Items;
import fr.andross.banitem.events.PlayerBanItemEvent;
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.items.CustomBannedItem;
import fr.andross.banitem.items.ICustomName;
import fr.andross.banitem.utils.Utils;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.debug.DebugMessage;
import fr.andross.banitem.utils.hooks.IWorldGuardHook;
import fr.andross.banitem.utils.list.ListType;
import fr.andross.banitem.utils.list.Listable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Map that contains the blacklisted items
 * @version 3.3
 * @author Andross
 */
public final class Blacklist extends HashMap<World, Items> {
    private final BanItem pl;

    /**
     * Constructor for a blacklist map
     * @param pl the main instance
     * @param database the database instance
     * @param sender {@link CommandSender} to send the debug messages to
     * @param section {@link ConfigurationSection} which contains the blacklist node
     */
    public Blacklist(@NotNull final BanItem pl, @NotNull final BanDatabase database, @NotNull final CommandSender sender, @Nullable final ConfigurationSection section) {
        this.pl = pl;
        if (section == null) return;

        // Loading blacklist
        final BanConfig banConfig = pl.getBanConfig();
        for (final String worldKey : section.getKeys(false)) { // Looping through worlds
            // Getting world(s)
            final List<World> worlds = Listable.getWorlds(worldKey, new Debug(banConfig, sender, new DebugMessage(banConfig.getConfigName()), new DebugMessage("blacklist"), new DebugMessage(ListType.WORLD, worldKey)));
            if (worlds.isEmpty()) continue;

            // Getting items(s)
            final ConfigurationSection itemsCs = section.getConfigurationSection(worldKey);
            if (itemsCs == null) continue; // should not happen, but, well..
            for (final String itemKey : itemsCs.getKeys(false)) {
                // Preparing debugger
                final Debug d = new Debug(banConfig, sender, new DebugMessage(banConfig.getConfigName()), new DebugMessage("blacklist"), new DebugMessage(ListType.WORLD, worldKey), new DebugMessage(ListType.ITEM, itemKey));

                // Getting Item(s)
                final List<BannedItem> items = Listable.getItems(database, itemKey, d);
                if (items.isEmpty()) continue;

                // Getting Actions & Actions data
                final ConfigurationSection actionCs = itemsCs.getConfigurationSection(itemKey);
                final Map<BanAction, BanActionData> actions = pl.getUtils().getBanActionsFromItemSection(worlds, actionCs, d);
                if (actions.isEmpty()) continue;

                // Adding into the map
                for (final World w : worlds)
                    for (final BannedItem item : items)
                        addNewBan(w, item, actions);
            }
        }
    }

    /**
     * This will add a new entry to the blacklist.
     * @param world bukkit world <i>({@link World})</i>
     * @param item banned item <i>({@link BannedItem})</i>
     * @param map map containing {@link BanAction} and their respective {@link BanActionData}
     */
    public void addNewBan(@NotNull final World world, @NotNull final BannedItem item, @NotNull final Map<BanAction, BanActionData> map) {
        final Items items = getOrDefault(world, new Items());
        final String customName = item instanceof ICustomName ? ((ICustomName) item).getName() : null;
        final CustomBannedItem customBannedItem = item instanceof CustomBannedItem ? (CustomBannedItem) item : null;
        final Map<BanAction, BanActionData> bannedItemMap = customBannedItem != null ? items.getCustomItems().getOrDefault(customBannedItem, new EnumMap<>(BanAction.class)) : items.getItems().getOrDefault(item, new EnumMap<>(BanAction.class));

        if (customName == null)
            bannedItemMap.putAll(map);
        else
            for (final Entry<BanAction, BanActionData> e : map.entrySet()) {
                final BanActionData data = new BanActionData();
                data.getMap().putAll(e.getValue().getMap());
                data.getMap().put(BanDataType.CUSTOMNAME, customName);
                bannedItemMap.put(e.getKey(), data);
            }

        if (customBannedItem != null)
            items.getCustomItems().put(customBannedItem, bannedItemMap);
        else
            items.getItems().put(item, bannedItemMap);

        put(world, items);
    }

    /**
     * Try to get the ban actions data for this item with this action.
     * @param world bukkit world <i>({@link World})</i>
     * @param item banned item <i>({@link BannedItem})</i>
     * @param action the ban action <i>({@link BanAction})</i>
     * @return the {@link BanActionData} object if the item is banned, or null if there is no banned action for this item with this action in this world
     */
    @Nullable
    public BanActionData getBanData(@NotNull final World world, @NotNull final BannedItem item, @NotNull final BanAction action) {
        return !containsKey(world) ? null : get(world).get(item, action);
    }

    /**
     * Trying to get the ban actions with their respective ban actions data for this item in the said world.
     * @param world bukkit world <i>({@link World})</i>
     * @param item banned item <i>({@link BannedItem})</i>
     * @return a map containing the ban action types and their respective ban actions, or null if this item is not banned in this world
     */
    @Nullable
    public Map<BanAction, BanActionData> getBanActions(@NotNull final World world, @NotNull final BannedItem item) {
        return !containsKey(world) ? null : get(world).get(item);
    }

    /**
     * Check if the action with the item is blacklisted for the player.
     * @param player player involved
     * @param location the effective location where the action occurs, using player location if null
     * @param item the banned item
     * @param sendMessage send a message to the player if banned
     * @param action action to check
     * @param data some ban data
     * @return true if the item is blacklisted for the player world, otherwise false
     */
    public boolean isBlacklisted(@NotNull final Player player, @Nullable final Location location, @NotNull final BannedItem item, final boolean sendMessage, @NotNull final BanAction action, @Nullable final BanData... data) {
        /* Checking blacklisted */
        final Map<BanAction, BanActionData> map = getBanActions(player.getWorld(), item);
        if (map == null || map.isEmpty() || !map.containsKey(action)) return false;

        // Checking custom data
        final BanActionData blacklistData = map.get(action);
        final Map<BanDataType, Object> dataMap = blacklistData.getMap();
        if (Utils.isNullOrEmpty(data) || Arrays.stream(data).allMatch(blacklistData::contains)) {
            // Checking creative data?
            if (dataMap.containsKey(BanDataType.GAMEMODE)) {
                final Set<GameMode> set = blacklistData.getData(BanDataType.GAMEMODE);
                if (set != null && !set.contains(player.getGameMode())) return false;
            }

            // Checking region data?
            if (dataMap.containsKey(BanDataType.REGION)) {
                final IWorldGuardHook hook = pl.getHooks().getWorldGuardHook();
                if (hook != null) {
                    final Set<com.sk89q.worldguard.protection.regions.ProtectedRegion> regions = blacklistData.getData(BanDataType.REGION);
                    if (regions != null && !regions.isEmpty()) {
                        final Set<com.sk89q.worldguard.protection.regions.ProtectedRegion> standingRegions = hook.getStandingRegions(location == null ? player.getLocation() : location);
                        if (regions.stream().noneMatch(standingRegions::contains)) return false;
                    }
                }
            }

            // Checking cooldown?
            long playerCooldown = -1L;
            if (dataMap.containsKey(BanDataType.COOLDOWN)) {
                final long cooldown = (long) dataMap.get(BanDataType.COOLDOWN);
                final Map<UUID, Long> cooldowns = blacklistData.getCooldowns();

                // Not in cooldown? Adding!
                if (!cooldowns.containsKey(player.getUniqueId())) {
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldown);
                    return false;
                }

                // Checking cooldown
                playerCooldown = cooldowns.get(player.getUniqueId());

                // Not in cooldown anymore?
                if (playerCooldown < System.currentTimeMillis()) {
                    cooldowns.remove(player.getUniqueId()); // not in cooldown anymore, cleaning up'
                    return false;
                }
            }

            // Permission data?
            final String itemName = dataMap.containsKey(BanDataType.CUSTOMNAME) ? String.valueOf(dataMap.get(BanDataType.CUSTOMNAME)) : item.getType().name().toLowerCase(Locale.ROOT);
            if (dataMap.containsKey(BanDataType.PERMISSION)) {
                if (player.hasPermission((String) dataMap.get(BanDataType.PERMISSION)))
                    return false;
            } else {
                // Bypass permission?
                if (pl.getUtils().hasPermission(player, itemName, action, data))
                    return false;
            }

            // Calling event?
            if (pl.getBanConfig().getConfig().getBoolean("api.playerbanitemevent")) {
                final PlayerBanItemEvent e = new PlayerBanItemEvent(player, PlayerBanItemEvent.Type.BLACKLIST, item, action, blacklistData, data);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled()) return false;
            }

            // Checking delete?
            if (map.containsKey(BanAction.DELETE))
                Bukkit.getScheduler().runTask(pl, () -> pl.getUtils().deleteItemFromInventoryView(player));

            if (sendMessage) {
                if (playerCooldown > 0) {
                    final List<String> message = blacklistData.getData(BanDataType.MESSAGE);
                    if (message != null) {
                        final long finalCooldown = playerCooldown;
                        message.stream().map(m -> m.replace("{time}", pl.getUtils().getCooldownString(finalCooldown - System.currentTimeMillis()))).forEach(player::sendMessage);
                    }
                } else
                    pl.getUtils().sendBanMessageAndAnimation(player, itemName, action, blacklistData);
            }

            // Run?
            if (dataMap.containsKey(BanDataType.RUN)) {
                final List<String> commands = blacklistData.getData(BanDataType.RUN);
                if (commands != null)
                    for (final String command : commands)
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                command.replace("{player}", player.getName())
                                        .replace("{world}", player.getWorld().getName())
                                        .replace("{itemname}", itemName));
            }
            return true;
        }

        return false;
    }

    /**
     * This method is used to check if the item is banned, not involving a player
     * This method is mainly used to check dispensers <i>dispense</i> and hoppers <i>transfer</i>
     * @param world bukkit world
     * @param item the banned item
     * @param action ban action
     * @param data optional ban data
     * @return true if the item is blacklisted for the player world, otherwise false
     */
    public boolean isBlacklisted(@NotNull final World world, @NotNull final BannedItem item, @NotNull final BanAction action, @Nullable final BanData... data) {
        final BanActionData blacklistData = getBanData(world, item, action);
        return blacklistData != null && (data == null || Arrays.stream(data).allMatch(blacklistData::contains));
    }

    /**
     * @return the total amount of banned items in all worlds
     */
    public int getTotal() {
        return values().stream().mapToInt(Items::getTotal).sum();
    }
}
