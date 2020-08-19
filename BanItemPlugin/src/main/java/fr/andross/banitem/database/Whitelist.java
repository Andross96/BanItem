/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2020 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import fr.andross.banitem.BanItem;
import fr.andross.banitem.BanUtils;
import fr.andross.banitem.options.BanData;
import fr.andross.banitem.options.BanDataType;
import fr.andross.banitem.options.BanOption;
import fr.andross.banitem.options.BanOptionData;
import fr.andross.banitem.utils.Listable;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.debug.DebugMessage;
import fr.andross.banitem.utils.events.PlayerBanItemEvent;
import fr.andross.banitem.utils.hooks.IWorldGuardHook;
import fr.andross.banitem.utils.item.BannedItem;
import fr.andross.banitem.utils.item.BannedItemMeta;
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
import java.util.stream.Collectors;

/**
 * Map that contains whitelistedworlds
 * @version 2.4
 * @author Andross
 */
public class Whitelist extends HashMap<World, WhitelistedWorld> {
    private final BanItem pl;

    /**
     * Constructor for a whitelist map
     * @param pl main instance
     * @param section {@link ConfigurationSection} which contains the blacklist node
     * @param sender {@link CommandSender} who to send the debug messages
     */
    public Whitelist(@NotNull final BanItem pl, @NotNull final CommandSender sender, @Nullable final ConfigurationSection section) {
        this.pl = pl;

        // Loading whitelist
        if (section == null) return;

        final Listable listable = pl.getUtils();
        for (final String worldKey : section.getKeys(false)) { // Looping through worlds
            // Checking the world
            final List<World> worlds = listable.getList(Listable.Type.WORLD, worldKey, new Debug(pl, sender, new DebugMessage(null, "config.yml"), new DebugMessage(null, "whitelist")));
            if (worlds.isEmpty()) continue;

            // Getting item info
            final ConfigurationSection itemsSection = section.getConfigurationSection(worldKey);
            if (itemsSection == null) continue;

            List<String> messages = null;
            List<BanOption> ignored = null;
            for (final String itemKey : itemsSection.getKeys(false)) {
                // Preparing debugger
                final Debug d = new Debug(pl, sender, new DebugMessage(null, "config.yml"), new DebugMessage(null, "whitelist"), new DebugMessage(Listable.Type.WORLD, worldKey), new DebugMessage(Listable.Type.ITEM, itemKey));

                // Blocked message?
                if (itemKey.equalsIgnoreCase("message")) {
                    final List<String> message = listable.getStringList(itemsSection.get(itemKey));
                    if (message.isEmpty()) continue;
                    messages = message.stream().filter(Objects::nonNull).map(pl.getUtils()::color).collect(Collectors.toList());
                    continue;
                }

                // Ignored options?
                if (itemKey.equalsIgnoreCase("ignored")) {
                    final List<String> ignoredOptions = listable.getStringList(itemsSection.get(itemKey));
                    if (ignoredOptions.isEmpty()) continue;
                    ignored = listable.getList(Listable.Type.OPTION, pl.getUtils().getSplittedList(ignoredOptions), d, worlds);
                    continue;
                }

                // Getting items
                final List<BannedItem> items = listable.getList(Listable.Type.ITEM, itemKey, d);
                if (items.isEmpty()) continue;

                // Getting options for the item
                final Map<BanOption, BanOptionData> options = new HashMap<>();
                final ConfigurationSection optionsSection = itemsSection.getConfigurationSection(itemKey);
                if (optionsSection == null) {
                    final String optionsNames = itemsSection.getString(itemKey);
                    if (optionsNames == null) continue;
                    final List<BanOption> optionsList = listable.getList(Listable.Type.OPTION, optionsNames, d);
                    if (optionsList.isEmpty()) continue;
                    for (final BanOption option : optionsList) options.put(option, new BanOptionData());
                } else options.putAll(pl.getUtils().getBanOptionsFromItemSection(worlds, optionsSection, d));

                if (options.isEmpty()) continue;

                // Adding into the map
                for (final World w : worlds)
                    for (final BannedItem item : items)
                        addNewException(getOrCreateWhitelistedWorld(w, messages, ignored), item, options);

            }
        }
    }

    /**
     * This method will create a new whitelisted world, and add/replace it into the map
     * @param world the bukkit world
     * @param messages list of "not allowed" messages
     * @param ignored list of ignored options
     * @return the new whitelistedworld object
     */
    @NotNull
    public WhitelistedWorld createNewWhitelistedWorld(@NotNull final World world, @Nullable final List<String> messages, @Nullable final List<BanOption> ignored) {
        final WhitelistedWorld ww = new WhitelistedWorld(world, messages, ignored);
        put(world, ww);
        return ww;
    }

    /**
     * This method try to get an already existing whitelisted world, else create and put one.
     * @param world the bukkit world
     * @param messages list of "not allowed" messages
     * @param ignored list of ignored options
     * @return an existing whitelistedworld object, otherwise a new one
     */
    public WhitelistedWorld getOrCreateWhitelistedWorld(@NotNull final World world, @Nullable final List<String> messages, @Nullable final List<BanOption> ignored) {
        if (containsKey(world)) return get(world);
        return createNewWhitelistedWorld(world, messages, ignored);
    }

    /**
     * This will add a new exception <i>(allowed item)</i> into the WhitelistedWorld object.
     * @param ww whitelistedworld, can be get with {@link Whitelist#getOrCreateWhitelistedWorld(World, List, List)}
     * @param item the item
     * @param options options with their respective datas
     */
    public void addNewException(@NotNull final WhitelistedWorld ww, @NotNull final BannedItem item, @NotNull final Map<BanOption, BanOptionData> options) {
        ww.addNewEntry(item, options);
        put(ww.getWorld(), ww);
    }

    /**
     * Check if the item is whitelisted <i>(allowed)</i>
     * <b>Does not consider permission!</b> <i>(You'll have to use {@link BanUtils#hasPermission(Player, String, String, BanOption, BanData...)})</i>
     * @param player player involved
     * @param location the effective location where the action occurs
     * @param item the banned item
     * @param sendMessage send a message to the player if not allowed
     * @param option ban option
     * @param data optional ban data
     * @return true if the item is whitelisted <i>(allowed)</i>, otherwise false
     */
    public boolean isWhitelisted(@NotNull final Player player, @NotNull final Location location, @NotNull final BannedItem item, final boolean sendMessage, @NotNull final BanOption option, @Nullable final BanData... data) {
        final WhitelistedWorld ww = get(player.getWorld());
        if (ww == null) return true;

        // Ignored option?
        if (ww.getIgnored().contains(option)) return true;

        /* Checking whitelist */
        final Map<BanOption, BanOptionData> map = ww.get(item);
        if (map != null && !map.isEmpty() && map.containsKey(option)) { // In whitelist
            final BanOptionData whitelisted = map.get(option);
            // Checking custom data
            if (data == null || Arrays.stream(data).allMatch(whitelisted::contains)) {
                // Checking metadata?
                if (whitelisted.containsKey(BanDataType.METADATA)) {
                    final BannedItemMeta meta = whitelisted.getMetadata();
                    if (meta != null && !meta.matches(item.toItemStack())) {
                        if (sendMessage) {
                            String itemName = pl.getBanDatabase().getCustomItems().getName(item);
                            if (itemName == null) itemName = item.getType().name();
                            pl.getUtils().sendMessage(player, itemName, option, whitelisted);
                        }
                        return false;
                    }
                }

                // Checking gamemode data?
                if (whitelisted.containsKey(BanDataType.GAMEMODE)) {
                    final Set<GameMode> set = whitelisted.getData(BanDataType.GAMEMODE);
                    if (set != null && !set.contains(player.getGameMode())) { // Gamemode not whitelisted
                        if (sendMessage) {
                            String itemName = pl.getBanDatabase().getCustomItems().getName(item);
                            if (itemName == null) itemName = item.getType().name();
                            pl.getUtils().sendMessage(player, itemName, option, whitelisted);
                        }
                        return false;
                    }
                }

                // Checking region data?
                if (whitelisted.containsKey(BanDataType.REGION)) {
                    final IWorldGuardHook hook = pl.getHooks().getWorldGuardHook();
                    if (hook != null) {
                        final Set<ProtectedRegion> regions = whitelisted.getData(BanDataType.REGION);
                        if (regions != null && !regions.isEmpty()) {
                            final Set<ProtectedRegion> standingRegions = hook.getStandingRegions(location);
                            if (regions.stream().noneMatch(standingRegions::contains)) {
                                if (sendMessage) {
                                    String itemName = pl.getBanDatabase().getCustomItems().getName(item);
                                    if (itemName == null) itemName = item.getType().name();
                                    pl.getUtils().sendMessage(player, itemName, option, whitelisted);
                                }
                                return false;
                            }
                        }
                    }
                }

                // Calling event?
                if (pl.getBanConfig().isUseEventApi()) {
                    final PlayerBanItemEvent e = new PlayerBanItemEvent(player, PlayerBanItemEvent.Type.WHITELIST, item, option, whitelisted, data);
                    Bukkit.getPluginManager().callEvent(e);
                    return !e.isCancelled();
                }

                return true;
            }
        }

        if (sendMessage) pl.getUtils().sendMessage(player, option, ww.getMessages());
        return false;
    }

    /**
     * This method is used to check if the item is whitelisted, not involving a player
     * This method is mainly used to check dispensers <i>dispense</i> and hoppers <i>transfer</i>
     * @param world bukkit world
     * @param item the banned item
     * @param option ban option
     * @param data optional ban data
     * @return true if the item is whitelisted <i>(allowed)</i>, otherwise false
     */
    public boolean isWhitelisted(@NotNull final World world, @NotNull final BannedItem item, @NotNull final BanOption option, @Nullable final BanData... data) {
        final WhitelistedWorld ww = get(world);
        if (ww == null) return true;

        // Ignored option?
        if (ww.getIgnored().contains(option)) return true;

        /* Checking whitelist */
        // Checking by item (can include meta)?
        Map<BanOption, BanOptionData> map = ww.get(item);
        // Checking by item without meta?
        if (map == null) {
            final BannedItem itemType = new BannedItem(item, false);
            map = ww.get(itemType);
        }

        if (map != null && map.containsKey(option)) { // In whitelist
            final BanOptionData whitelisted = map.get(option);
            // Checking custom data
            return data == null || Arrays.stream(data).allMatch(whitelisted::contains);
        }
        return false;
    }

    /**
     * @return the total amount of items allowed, in all worlds
     */
    public int getTotal() {
        int i = 0;
        for (final WhitelistedWorld ww : values()) i += ww.getTotal();
        return i;
    }

}
