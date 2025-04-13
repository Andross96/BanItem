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

import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.actions.BanDataType;
import fr.andross.banitem.database.items.Items;
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.items.CustomBannedItem;
import fr.andross.banitem.items.ICustomName;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Map containing all allowed items of a world
 *
 * @author Andross
 * @version 3.1.1
 */
public final class WhitelistedWorld extends Items {
    private final World world;
    private final List<String> messages = new ArrayList<>();
    private final Set<BanAction> ignored = EnumSet.noneOf(BanAction.class);

    /**
     * This constructor should not be used like this <i>(as it will not been stored into the Whitelist map)</i>
     * Use {@link Whitelist#createNewWhitelistedWorld(World, List, List)} instead.
     *
     * @param world    bukkit world
     * @param messages list of messages to send if the item is not allowed
     * @param ignored  list of ignored actions
     */
    public WhitelistedWorld(@NotNull final World world,
                            @Nullable final List<String> messages,
                            @Nullable final List<BanAction> ignored) {
        this.world = world;
        if (messages != null) {
            this.messages.addAll(messages);
        }
        if (ignored != null) {
            this.ignored.addAll(ignored);
        }
    }

    /**
     * This will add a new entry to the whitelist.
     *
     * @param item banned item <i>({@link BannedItem})</i>
     * @param map  map containing {@link BanAction} and their respective {@link BanActionData}
     */
    public void addNewEntry(@NotNull final BannedItem item,
                            @NotNull final Map<BanAction, BanActionData> map) {
        final String customName = item instanceof ICustomName ? ((ICustomName) item).getName() : null;
        final CustomBannedItem customBannedItem = item instanceof CustomBannedItem ? (CustomBannedItem) item : null;
        final Map<BanAction, BanActionData> bannedItemMap = customBannedItem != null ? customItems.getOrDefault(customBannedItem, new EnumMap<>(BanAction.class)) : items.getOrDefault(item, new EnumMap<>(BanAction.class));

        if (customName == null) {
            bannedItemMap.putAll(map);
        } else {
            for (final Map.Entry<BanAction, BanActionData> e : map.entrySet()) {
                final BanActionData data = new BanActionData();
                data.getMap().putAll(e.getValue().getMap());
                data.getMap().put(BanDataType.CUSTOMNAME, customName);
                bannedItemMap.put(e.getKey(), data);
            }
        }

        if (customBannedItem != null) {
            customItems.put(customBannedItem, bannedItemMap);
        } else {
            items.put(item, bannedItemMap);
        }
    }

    /**
     * The bukkit world.
     *
     * @return the bukkit world
     */
    @NotNull
    public World getWorld() {
        return world;
    }

    /**
     * List of messages, empty if none configured.
     *
     * @return list of messages, empty if none configured
     */
    @NotNull
    public List<String> getMessages() {
        return messages;
    }

    /**
     * Set of ignored actions, empty if none configured.
     *
     * @return set of ignored actions, empty if none configured
     */
    @NotNull
    public Set<BanAction> getIgnored() {
        return ignored;
    }
}
