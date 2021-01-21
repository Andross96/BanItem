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
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Map containing all allowed items of a world
 * @version 3.0
 * @author Andross
 */
public final class WhitelistedWorld extends Items {
    private final World world;
    private final List<String> messages = new ArrayList<>();
    private final Set<BanAction> ignored = EnumSet.noneOf(BanAction.class);

    /**
     * This constructor should not be used like this <i>(as it will not been stored into the Whitelist map)</i>
     * Use {@link Whitelist#createNewWhitelistedWorld(World, List, List)} instead.
     * @param world bukkit world
     * @param messages list of messages to send if the item is not allowed
     * @param ignored list of ignored actions
     */
    public WhitelistedWorld(@NotNull final World world, @Nullable final List<String> messages, @Nullable final List<BanAction> ignored) {
        this.world = world;
        if (messages != null) this.messages.addAll(messages);
        if (ignored != null) this.ignored.addAll(ignored);
    }

    /**
     * This will add a new entry to the whitelist
     * @param item banned item <i>({@link BannedItem})</i>
     * @param map map containing {@link BanAction} and their respective {@link BanActionData}
     */
    public void addNewEntry(@NotNull final BannedItem item, @NotNull final Map<BanAction, BanActionData> map) {
        if (item instanceof CustomBannedItem) {
            final CustomBannedItem customBannedItem = (CustomBannedItem) item;
            final Map<BanAction, BanActionData> bannedItemMap = customItems.getOrDefault(customBannedItem, new HashMap<>());
            for (final Map.Entry<BanAction, BanActionData> e : map.entrySet()) {
                e.getValue().getMap().put(BanDataType.CUSTOMNAME, customBannedItem.getName());
                bannedItemMap.put(e.getKey(), e.getValue());
            }
            customItems.put(customBannedItem, bannedItemMap);
        } else {
            final Map<BanAction, BanActionData> bannedItemMap = items.getOrDefault(item, new HashMap<>());
            bannedItemMap.putAll(map);
            items.put(item, bannedItemMap);
        }
    }

    /**
     * @return the bukkit world
     */
    @NotNull
    public World getWorld() {
        return world;
    }

    /**
     * @return list of messages, empty if none configured
     */
    @NotNull
    public List<String> getMessages() {
        return messages;
    }

    /**
     * @return set of ignored actions, empty if none configured
     */
    @NotNull
    public Set<BanAction> getIgnored() {
        return ignored;
    }
}
