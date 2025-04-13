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
package fr.andross.banitem.database.items;

import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.actions.BanActionData;
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.items.CustomBannedItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * Map that store all the banned items, with their actions and actions data.
 *
 * @author Andross
 * @version 3.1.1
 */
public class Items {
    /**
     * Items map, containing all items. Includes normal and meta items.
     */
    protected final Map<BannedItem, Map<BanAction, BanActionData>> items = new HashMap<>();

    /**
     * Items map, containing all custom items.
     */
    protected final Map<CustomBannedItem, Map<BanAction, BanActionData>> customItems = new HashMap<>();

    /**
     * Constructor
     */
    public Items() {}

    /**
     * Get a map of actions and their respective data for a banned item object, if present.
     *
     * @param bannedItem the banned item
     * @return a map of actions and data if found, otherwise null.
     */
    @Nullable
    public Map<BanAction, BanActionData> get(@NotNull final BannedItem bannedItem) {
        // Custom items?
        if (!customItems.isEmpty()) {
            for (final Map.Entry<CustomBannedItem, Map<BanAction, BanActionData>> e : customItems.entrySet()) {
                if (e.getKey().matches(bannedItem)) {
                    return e.getValue();
                }
            }
        }

        // Meta item?
        if (items.containsKey(bannedItem)) {
            return items.get(bannedItem);
        }

        // Simple material item?
        final BannedItem simpleBannedItem = new BannedItem(bannedItem.getType());
        return items.get(simpleBannedItem);
    }

    /**
     * Get the BanActionData of the BannedItem for the said action, if present.
     *
     * @param bannedItem the banned item
     * @param action     the action
     * @return the BanActionData if found, otherwise null.
     */
    @Nullable
    public BanActionData get(@NotNull final BannedItem bannedItem, @NotNull final BanAction action) {
        final Map<BanAction, BanActionData> map = get(bannedItem);
        return map == null ? null : map.get(action);
    }

    /**
     * Get the items map (include normal and meta items).
     *
     * @return the items map
     */
    @NotNull
    public Map<BannedItem, Map<BanAction, BanActionData>> getItems() {
        return items;
    }

    /**
     * Get the custom items map.
     *
     * @return the custom items map
     */
    @NotNull
    public Map<CustomBannedItem, Map<BanAction, BanActionData>> getCustomItems() {
        return customItems;
    }

    /**
     * Get the total amount of items added.
     *
     * @return the total amount of items added
     */
    public int getTotalAmountOfItems() {
        return items.size() + customItems.size();
    }

    /**
     * Get all actions saved for the items, unmodifiable.
     *
     * @return all actions saved for the items
     */
    @NotNull
    @Unmodifiable
    public Set<BanAction> getAllActions() {
        final Set<BanAction> set = EnumSet.noneOf(BanAction.class);
        items.values().stream().map(Map::keySet).forEach(set::addAll);
        customItems.values().stream().map(Map::keySet).forEach(set::addAll);
        return Collections.unmodifiableSet(set);
    }
}
