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
package fr.andross.banitem.utils;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.andross.banitem.BanItem;
import fr.andross.banitem.database.CustomItems;
import fr.andross.banitem.options.BanOption;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.hooks.IWorldGuardHook;
import fr.andross.banitem.utils.item.BannedItem;
import fr.andross.banitem.utils.item.MetaType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A listable class which will attempt to create List object from gived data.
 * Mainly used to load configurations.
 * @version 2.4
 * @author Andross
 */
public class Listable {
    protected final BanItem pl;
    private CustomItems customItems = null;

    // Caching all objects
    private final List<String> commands = Arrays.asList("add", "check", "customitem", "help", "info", "log", "reload");
    private final List<BanOption> options = Arrays.asList(BanOption.values());
    private final List<BannedItem> items = Arrays.stream(Material.values()).map(BannedItem::new).collect(Collectors.toList());
    private final List<EntityType> entities = Arrays.asList(EntityType.values());
    private final List<GameMode> gamemodes = Arrays.asList(GameMode.values());
    private final List<InventoryType> inventories = Arrays.asList(InventoryType.values());
    private final List<MetaType> metas = Arrays.asList(MetaType.values());
    private final List<Enchantment> enchantments = Arrays.asList(Enchantment.values());
    private final List<PotionType> potions = Arrays.asList(PotionType.values());

    public Listable(@NotNull final BanItem pl) {
        this.pl = pl;
    }

    /**
     * Setting the custom items instance
     * @param customItems custom items instance
     */
    public void setCustomItems(@NotNull final CustomItems customItems) {
        this.customItems = customItems;
    }

    /**
     * @param string string to split
     * @return a list of splitted string
     */
    @NotNull
    public List<String> getSplittedList(@NotNull final String string) {
        return newList(string.trim().replaceAll("\\s+", "").split(","));
    }

    /**
     * @param list list of strings to split
     * @return a list containing all strings splitted from another one
     */
    @NotNull
    public List<String> getSplittedList(@NotNull final List<String> list) {
        final List<String> newList = new ArrayList<>();
        for (final String string : list) newList.addAll(getSplittedList(string));
        return newList;
    }

    /**
     * Trying to get a listable string of an Object.
     * Mainly used to extract strings from a {@link org.bukkit.configuration.ConfigurationSection#get(String)}
     * @param o object to parse
     * @return a splitted list of elements in this object
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public List<String> getStringList(@Nullable final Object o) {
        if (o == null) return new ArrayList<>();
        if (o instanceof String) return newList((String) o);
        else if (o instanceof List) return ((List<String>) o);
        else if (o instanceof String[]) return newList((String[])o);
        else return new ArrayList<>();
    }

    /**
     * Trying to get a list of type, mainly from a config section.
     * If there is any error, this will be debugged
     * @param type type of data
     * @param o object to parse
     * @param d debugger, returning empty list if null and if there is any error
     * @param worlds list of worlds where the option should apply, used for regions
     * @param <T> parameter respecting the type class
     * @return a list of parsed type
     */
    @NotNull
    public <T> List<T> getList(@NotNull final Type type, @Nullable final Object o, @Nullable final Debug d, @Nullable final List<World> worlds) {
        List<String> stringList = getStringList(o);
        stringList = getSplittedList(stringList);
        return getList(type, stringList, d, worlds);
    }

    /**
     * Trying to get a list of type from a String
     * If there is any error, this will be debugged
     * @param type type of data
     * @param string string to split and parse
     * @param d debugger, returning empty list if null and if there is any error
     * @param <T> parameter respecting the type class
     * @return a list of parsed type
     */
    @NotNull
    public <T> List<T> getList(@NotNull final Type type, @NotNull final String string, @Nullable final Debug d) {
        return getList(type, getSplittedList(string), d, null);
    }

    /**
     * Trying to get a list of type
     * If there is any error, this will be debugged
     * @param type type of data
     * @param strings list of strings used to parse
     * @param d debugger, returning empty list if null and if there is any error
     * @param worlds list of worlds, used for region checking
     * @param <T> parameter respecting the type class
     * @return a list of parsed type
     */
    @NotNull
    public <T> List<T> getList(@NotNull final Type type, @NotNull final List<String> strings, @Nullable final Debug d, @Nullable final List<World> worlds) {
        final List<T> list = new ArrayList<>();
        if (strings.isEmpty()) return list;

        for (String key : strings) {
            try {
                if (key.equals("*")) {
                    list.addAll(getAllObjects(type, worlds));
                    continue;
                }
                final boolean remove = key.startsWith("!");
                if (remove) key = key.substring(1);

                if (worlds != null && !worlds.isEmpty()) {
                    for (final World world : worlds) {
                        final T o = getObject(type, key, world);
                        if (o == null) throw new Exception();
                        if (remove) list.remove(o); else list.add(o);
                    }
                } else {
                    final T o = getObject(type, key, null);
                    if (o == null) throw new Exception();
                    if (remove) list.remove(o);
                    else list.add(o);
                }
            } catch (final Exception e) {
                if (d == null) return new ArrayList<>();
                d.clone().add(type, "&cUnknown " + type.name().toLowerCase() + " &e&l" + key + "&c.").sendDebug();
            }
        }

        return list;
    }

    /**
     * This method will give all possibles objects for the given type.
     * @param type type to get
     * @param worlds list of worlds, used for regions
     * @param <T> parameter of the type
     * @return list of all elements for the given type
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private <T> List<T> getAllObjects(@NotNull final Type type, @Nullable final List<World> worlds) {
        switch (type) {
            case WORLD: return (List<T>) Bukkit.getWorlds();
            case OPTION: return (List<T>) options;
            case ITEM: {
                final List<BannedItem> list = new ArrayList<>(items);
                list.addAll(customItems.values());
                return (List<T>) list;
            }
            case ENTITY: return (List<T>) entities;
            case GAMEMODE: return (List<T>) gamemodes;
            case INVENTORY: return (List<T>) inventories;
            case REGION: {
                final IWorldGuardHook hook = pl.getHooks().getWorldGuardHook();
                final List<ProtectedRegion> list = new ArrayList<>();
                if (hook == null || worlds == null || worlds.isEmpty()) return (List<T>) list;
                for (final World w : worlds) list.addAll(hook.getAllProtectedRegions(w));
                return (List<T>) list;
            }
            default: return new ArrayList<>();
        }
    }

    /**
     * This method will try to get an element for the given type
     * @param type type of the element
     * @param key object that should be get from this type
     * @param world world, used to check regions
     * @param <T> parameter
     * @return an object of type, null if not found
     * @throws Exception for any Enum non matches
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private <T> T getObject(@NotNull final Type type, @NotNull final String key, @Nullable final World world) throws Exception {
        switch (type) {
            case WORLD: return (T) Bukkit.getWorld(key);
            case OPTION: return (T) BanOption.valueOf(key.toUpperCase());
            case ITEM: {
                final Material m = Material.matchMaterial(key);
                if (m != null) return (T) new BannedItem(m);
                final BannedItem bi = customItems.get(key);
                return (T) bi;
            }
            case ENTITY: return (T) EntityType.valueOf(key.toUpperCase());
            case GAMEMODE: return (T) GameMode.valueOf(key.toUpperCase());
            case INVENTORY: return (T) InventoryType.valueOf(key.toUpperCase());
            case REGION: {
                final IWorldGuardHook hook = pl.getHooks().getWorldGuardHook();
                if (hook == null || world == null) return null;
                return (T) hook.getProtectedRegion(world, key);
            }
            default: return null;
        }
    }

    /**
     * This will create a new list <i>(mutable)</i> from an array
     * @param a elements
     * @param <T> parameter
     * @return a mutable list of elements
     */
    @SafeVarargs
    @NotNull
    public final <T> List<T> newList(@NotNull final T... a) {
        return new ArrayList<>(Arrays.asList(a));
    }

    /**
     * This is used to set a type of data recovered
     */
    public enum Type {
        WORLD,
        OPTION,
        ITEM,
        ENTITY,
        GAMEMODE,
        INVENTORY,
        METADATA,
        METADATA_ENCHANTMENT,
        METADATA_POTION,
        REGION
    }

    /**
     * List of plugin sub commands
     * @return list of plugin sub commands
     */
    @NotNull
    public List<String> getCommands() {
        return commands;
    }

    /**
     * List of ban options
     * @return list of ban options
     */
    @NotNull
    public List<BanOption> getOptions() {
        return options;
    }

    /**
     * List of banned items (all materials, without metadata).
     * <b>This does not includes the custom items!</b>
     * @return list of plugin sub commands
     */
    @NotNull
    public List<BannedItem> getItems() {
        return items;
    }

    /**
     * List of entity types
     * @return list of entity types
     */
    @NotNull
    public List<EntityType> getEntities() {
        return entities;
    }

    /**
     * List of gamemodes
     * @return list of gamemodes
     */
    @NotNull
    public List<GameMode> getGamemodes() {
        return gamemodes;
    }

    /**
     * List of inventory types
     * @return list of inventory types
     */
    @NotNull
    public List<InventoryType> getInventories() {
        return inventories;
    }

    /**
     * List of metas types
     * @return list of metas types
     */
    @NotNull
    public List<MetaType> getMetas() {
        return metas;
    }

    /**
     * List of enchantments
     * @return list of enchantments
     */
    @NotNull
    public List<Enchantment> getEnchantments() {
        return enchantments;
    }

    /**
     * List of potions
     * @return list of potions
     */
    @NotNull
    public List<PotionType> getPotions() {
        return potions;
    }
}
