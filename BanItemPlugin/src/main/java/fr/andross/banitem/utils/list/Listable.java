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
package fr.andross.banitem.utils.list;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.andross.banitem.BanDatabase;
import fr.andross.banitem.BanItem;
import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.utils.Chat;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.enchantments.EnchantmentHelper;
import fr.andross.banitem.utils.hooks.IWorldGuardHook;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A powerful listable class which will attempt to create List object from given data.
 * Mainly used to load configurations.
 *
 * @author Andross
 * @version 3.1
 */
public final class Listable {

    /**
     * Split a string into a list of string, based on ',' character.
     * Each item will be trimmed.
     *
     * @param string the string to split
     * @return the string split into a list of multiple value, separated by ',' character
     */
    @NotNull
    public static List<String> splitToList(@NotNull final String string) {
        return newList(string.trim().split(","))
                .stream()
                .map(String::trim)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Split each element in the list into a list of string, based on ',' character.
     * Each item will be trimmed.
     *
     * @param list the string to split
     * @return list of all elements split by ',' character
     */
    @NotNull
    public static List<String> splitToList(@Nullable final List<String> list) {
        final List<String> newList = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return newList;
        }
        for (final String string : list) {
            newList.addAll(splitToList(string));
        }
        return newList;
    }

    /**
     * Trying to get a list of string from an Object.
     * Mainly used to extract strings from a {@link org.bukkit.configuration.ConfigurationSection#get(String)}
     *
     * @param o object to parse
     * @return a list of elements in this object
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static List<String> getStringList(@Nullable final Object o) {
        if (o instanceof String) {
            return newList((String) o);
        }
        if (o instanceof List) {
            return ((List<String>) o);
        }
        if (o instanceof String[]) {
            return newList((String[]) o);
        }
        return new ArrayList<>();
    }

    /**
     * Get a list of string based on each elements split with ',' character,
     * based on the type of the object (string, list, array).
     *
     * @param o the object
     * @return a list of all elements split with ',' character
     */
    @NotNull
    public static List<String> getSplitStringList(@Nullable final Object o) {
        return splitToList(getStringList(o));
    }

    /**
     * This will create a new <i>(mutable)</i> list from an array.
     *
     * @param a   elements
     * @param <T> parameter
     * @return a mutable list of elements
     */
    @SafeVarargs
    @NotNull
    public static <T> List<T> newList(@NotNull final T... a) {
        return new ArrayList<>(Arrays.asList(a));
    }

    /**
     * Get a list of type. This should <b>not</b> be used for Items, World nor Regions.
     * Use instead {@link #getItems(BanDatabase, Object, Debug)}, {@link #getWorlds(Object, Debug)},
     * and {@link #getRegionsList(BanItem, Object, Debug, Collection)},
     *
     * @param type type of data
     * @param obj  object to get a list from
     * @param d    debugger, returning empty list if null and if there is any error
     * @param <T>  parameter respecting the type class
     * @return a list of parsed type
     */
    @NotNull
    public static <T> List<T> getList(@NotNull final ListType type,
                                      @NotNull final Object obj,
                                      @Nullable final Debug d) {
        final List<T> list = new ArrayList<>();
        final List<String> strings = getSplitStringList(obj);
        if (strings.isEmpty()) {
            return list;
        }

        for (String key : strings) {
            try {
                if (key.equals("*")) {
                    list.addAll(getAllObjects(type));
                    continue;
                }
                final boolean remove = key.startsWith("!");
                if (remove) {
                    key = key.substring(1);
                }
                final T o = getObject(type, key);
                if (o == null) {
                    throw new Exception();
                }
                if (remove) {
                    list.remove(o);
                } else {
                    list.add(o);
                }
            } catch (final Exception e) {
                if (d != null) {
                    d.clone().add(type, "&cUnknown " + type.name().toLowerCase() + " &e&l" + key + "&c.").sendDebug();
                }
            }
        }

        return list;
    }

    /**
     * Trying to get a list of type.
     * If there is any error, this will be debugged.
     *
     * @param obj object to get a list from
     * @param d   debugger, returning empty list if null and if there is any error
     * @return a list of parsed type
     */
    @NotNull
    public static List<World> getWorlds(@NotNull final Object obj, @Nullable final Debug d) {
        final List<World> worlds = new ArrayList<>();
        final List<String> strings = getSplitStringList(obj).stream().map(Chat::stripColors).collect(Collectors.toList());
        if (strings.isEmpty()) {
            return worlds;
        }

        for (String worldName : strings) {
            // Regex?
            if (worldName.startsWith("#")) {
                final Pattern pattern;
                try {
                    pattern = Pattern.compile(worldName.substring(1));
                } catch (final PatternSyntaxException e) {
                    if (d != null) {
                        d.clone().add(ListType.WORLD, "&cInvalid regex syntax &e&l" + worldName + "&c.").sendDebug();
                    }
                    continue;
                }
                // Getting world
                Bukkit.getWorlds().stream()
                        .filter(w -> pattern.matcher(w.getName()).find())
                        .forEach(worlds::add);
                continue;
            }

            if (worldName.equals("*")) {
                worlds.addAll(Bukkit.getWorlds());
                continue;
            }

            final boolean remove = worldName.startsWith("!");
            if (remove) {
                worldName = worldName.substring(1);
            }

            // Getting the world
            final World w = Bukkit.getWorld(worldName);
            if (w == null) {
                if (d != null) {
                    d.clone().add(ListType.WORLD, "&cUnknown world &e&l" + worldName + "&c.").sendDebug();
                }
            } else {
                if (remove) {
                    worlds.remove(w);
                } else {
                    worlds.add(w);
                }
            }
        }

        return worlds;
    }

    /**
     * Trying to get a list of materials.
     * If there is any error, this will be debugged.
     *
     * @param obj object to get a list from
     * @param d   debugger, returning empty list if null and if there is any error
     * @return a list of parsed type
     */
    @NotNull
    public static List<Material> getMaterials(@NotNull final Object obj,
                                              @Nullable final Debug d) {
        final List<Material> materials = new ArrayList<>();
        final List<String> strings = getSplitStringList(obj).stream().map(Chat::stripColors).collect(Collectors.toList());
        if (strings.isEmpty()) {
            return materials;
        }

        for (String materialName : strings) {
            // Regex?
            if (materialName.startsWith("#")) {
                final Pattern pattern;
                try {
                    pattern = Pattern.compile(materialName.substring(1));
                } catch (final PatternSyntaxException e) {
                    if (d != null) {
                        d.clone().add(ListType.ITEM, "&cInvalid regex syntax &e&l" + materialName + "&c.").sendDebug();
                    }
                    continue;
                }
                // Getting materials
                Arrays.stream(Material.values()).filter(m -> pattern.matcher(m.name()).find()).forEach(materials::add);
                continue;
            }

            if (materialName.equals("*")) {
                materials.addAll(Arrays.asList(Material.values()));
                continue;
            }

            final boolean remove = materialName.startsWith("!");
            if (remove) {
                materialName = materialName.substring(1);
            }

            // Getting the material
            final Material m = Material.matchMaterial(materialName);
            if (m == null) {
                if (d != null) {
                    d.clone().add(ListType.MATERIAL, "&cUnknown material &e&l" + materialName + "&c.").sendDebug();
                }
            } else {
                if (remove) {
                    materials.remove(m);
                } else {
                    materials.add(m);
                }
            }
        }

        return materials;
    }

    /**
     * Trying to get a list of type.
     * If there is any error, this will be debugged.
     *
     * @param database the current database instance
     * @param obj      object to get a list from
     * @param d        debugger, returning empty list if null and if there is any error
     * @return a list of items
     */
    @NotNull
    public static List<BannedItem> getItems(@NotNull final BanDatabase database,
                                            @NotNull final Object obj,
                                            @Nullable final Debug d) {
        final List<BannedItem> items = new ArrayList<>();
        final List<String> strings = getSplitStringList(obj);
        if (strings.isEmpty()) {
            return items;
        }

        for (String itemName : strings) {
            // Regex?
            if (itemName.startsWith("#")) {
                final Pattern pattern;
                try {
                    pattern = Pattern.compile(itemName.substring(1));
                } catch (final PatternSyntaxException e) {
                    if (d != null) {
                        d.clone().add(ListType.ITEM, "&cInvalid regex syntax &e&l" + itemName + "&c.").sendDebug();
                    }
                    continue;
                }
                // Getting from custom item
                database.getCustomItems().entrySet().stream()
                        .filter(e -> pattern.matcher(e.getKey()).find())
                        .forEach(e -> items.add(e.getValue()));
                // Getting from meta item
                database.getMetaItems().entrySet().stream()
                        .filter(e -> pattern.matcher(e.getKey()).find())
                        .forEach(e -> items.add(e.getValue()));
                // Getting from materials
                Stream.of(Material.values())
                        .filter(m -> pattern.matcher(m.name()).find())
                        .forEach(m -> items.add(new BannedItem(m)));
                continue;
            }

            if (itemName.equals("*")) {
                items.addAll(Arrays.stream(Material.values()).map(BannedItem::new).collect(Collectors.toList()));
                continue;
            }

            final boolean remove = itemName.startsWith("!");
            if (remove) {
                itemName = itemName.substring(1);
            }

            // Is a custom item?
            if (database.getCustomItems().containsKey(itemName)) {
                final BannedItem bannedItem = database.getCustomItems().get(itemName);
                if (remove) {
                    items.remove(bannedItem);
                } else {
                    items.add(bannedItem);
                }
                continue;
            }

            // Is a meta item?
            if (database.getMetaItems().containsKey(itemName)) {
                final BannedItem bannedItem = database.getMetaItems().get(itemName);
                if (remove) {
                    items.remove(bannedItem);
                } else {
                    items.add(bannedItem);
                }
                continue;
            }

            // Getting the material?
            final Material m = Material.matchMaterial(itemName);
            if (m == null) {
                if (d != null) {
                    d.clone().add(ListType.ITEM, "&cUnknown item &e&l" + itemName + "&c.").sendDebug();
                }
            } else {
                final BannedItem bannedItem = new BannedItem(m);
                if (remove) {
                    items.remove(bannedItem);
                } else {
                    items.add(bannedItem);
                }
            }
        }

        return items;
    }

    /**
     * Get a list of protected regions (WorldGuard).
     *
     * @param pl     the plugin instance
     * @param obj    object to get a list from
     * @param d      debugger, returning empty list if null and if there is any error
     * @param worlds list of worlds where to get the regions
     * @return a list of regions
     */
    @NotNull
    public static List<ProtectedRegion> getRegionsList(@NotNull final BanItem pl,
                                                       @NotNull final Object obj,
                                                       @Nullable final Debug d,
                                                       @NotNull final Collection<World> worlds) {
        final List<ProtectedRegion> list = new ArrayList<>();
        final List<String> strings = getSplitStringList(obj);
        if (strings.isEmpty()) {
            return list;
        }

        // Getting WorldGuard hook
        final IWorldGuardHook hook = pl.getHooks().getWorldGuardHook();
        if (hook == null) {
            return list;
        }

        for (String key : strings) {
            if (key.equals("*")) {
                // Getting all regions
                worlds.forEach(w -> list.addAll(hook.getAllProtectedRegions(w)));
                continue;
            }

            final boolean remove = key.startsWith("!");
            if (remove) {
                key = key.substring(1);
            }

            // Getting regions
            for (final World w : worlds) {
                final ProtectedRegion protectedRegion = hook.getProtectedRegion(w, key);
                if (protectedRegion == null) {
                    if (d != null) {
                        d.clone().add(ListType.REGION, "&cUnknown region &e&l" + key + "&c for world &e&l" + w.getName() + "&c.").sendDebug();
                    }
                } else {
                    if (remove) {
                        list.remove(protectedRegion);
                    } else {
                        list.add(protectedRegion);
                    }
                }
            }
        }

        return list;
    }

    /**
     * This method will try to get an element for the given type.
     *
     * @param type type of the element
     * @param key  object that should be get from this type
     * @param <T>  parameter
     * @return an object of type, null if not found
     * @throws IllegalArgumentException for any Enum non matches
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T getObject(@NotNull final ListType type,
                                  @NotNull final String key) throws IllegalArgumentException {
        switch (type) {
            case WORLD:
                return (T) Bukkit.getWorld(key);
            case ACTION:
                return (T) BanAction.valueOf(key.toUpperCase());
            case ITEM: {
                final Material m = Material.matchMaterial(key);
                return m == null ? null : (T) new BannedItem(m);
            }
            case ENTITY:
                return (T) EntityType.valueOf(key.toUpperCase());
            case ENCHANTMENT:
                return (T) EnchantmentHelper.getEnchantmentWrapper(key);
            case GAMEMODE:
                return (T) GameMode.valueOf(key.toUpperCase());
            case INVENTORY:
                return (T) InventoryType.valueOf(key.toUpperCase());
            case MATERIAL:
                return (T) Material.matchMaterial(key);
            default:
                return null;
        }
    }

    /**
     * This method will give all possibles objects for the given type.
     *
     * @param type type to get
     * @param <T>  parameter of the type
     * @return list of all elements for the given type
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> List<T> getAllObjects(@NotNull final ListType type) {
        switch (type) {
            case WORLD:
                return (List<T>) Bukkit.getWorlds();
            case ACTION:
                return (List<T>) Arrays.stream(BanAction.values()).collect(Collectors.toList());
            case ITEM:
                return (List<T>) Arrays.stream(Material.values()).map(BannedItem::new).collect(Collectors.toList());
            case ENTITY:
                return (List<T>) Arrays.stream(EntityType.values()).collect(Collectors.toList());
            case ENCHANTMENT:
                final List<T> enchantments = new ArrayList<>();
                Arrays.stream(Enchantment.values())
                        .map(EnchantmentHelper::getAllEnchantmentWrappers)
                        .forEach(l -> enchantments.addAll((Collection<? extends T>) l));
                return enchantments;
            case GAMEMODE:
                return (List<T>) Arrays.stream(GameMode.values()).collect(Collectors.toList());
            case INVENTORY:
                return (List<T>) Arrays.stream(InventoryType.values()).collect(Collectors.toList());
            case MATERIAL:
                return (List<T>) Arrays.stream(Material.values()).collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }
}
