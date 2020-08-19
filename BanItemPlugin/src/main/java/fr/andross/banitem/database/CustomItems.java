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

import fr.andross.banitem.BanItem;
import fr.andross.banitem.utils.item.BannedItem;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Map that contains all the custom items
 * This is a double map <i>(include a reversed map)</i>, for easier access of
 * custom items names and their respective banned item.
 * @version 2.4
 * @author Andross
 */
public final class CustomItems extends HashMap<String, BannedItem> {
    private final Map<BannedItem, String> reversed = new HashMap<>();
    private final File itemsFile;
    private FileConfiguration itemsConfig;

    /**
     * This will create a new instance of custom items map, with the items from <i>items.yml</i> file.
     * This should not be used externally, as it could create two different instance of this object.
     * You should use {@link fr.andross.banitem.BanItemAPI#load(CommandSender, FileConfiguration)} instead.
     * @param pl main instance
     * @param sender the sender who executed this command, for debug
     */
    public CustomItems(@NotNull final BanItem pl, @NotNull final CommandSender sender) {
        this.itemsFile = new File(pl.getDataFolder(), "items.yml");

        // Checking/Creating file
        try {
            // Trying to save the custom one, else creating a new one
            if (!itemsFile.isFile()) pl.saveResource("items.yml", false);
            if (!itemsFile.isFile()) if (!itemsFile.createNewFile()) throw new Exception();
        } catch (final Exception e) {
            e.printStackTrace();
            sender.sendMessage(pl.getBanConfig().getPrefix() + pl.getUtils().color("&cUnable to use custom items for this session."));
            return;
        }

        // Loading custom items
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        for (final String key : itemsConfig.getKeys(false)) {
            try {
                final ItemStack itemStack = (ItemStack) itemsConfig.get(key);
                if (itemStack == null) throw new Exception();
                put(key, new BannedItem(itemStack));
            } catch (final Exception e) {
                sender.sendMessage(pl.getBanConfig().getPrefix() + pl.getUtils().color("&cInvalid custom item &e" + key + "&c in items.yml."));
            }
        }
    }

    /**
     * Adding the banned item with this custom name into the map only <i>(not saved in file)</i>
     * @param key name of the custom item
     * @param value banned item
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    @Override
    public BannedItem put(@NotNull final String key, @Nullable final BannedItem value) {
        reversed.put(value, key);
        return super.put(key, value);
    }

    /**
     * Will clear the map and its reversed one
     */
    @Override
    public void clear() {
        reversed.clear();
        super.clear();
    }

    /**
     * Remove the banned item with this custom name from the map only <i>(not saved in file)</i>
     * @param key custom item name
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    @Override
    public BannedItem remove(@NotNull final Object key) {
        reversed.remove(get(key));
        return super.remove(key);
    }

    /**
     * Trying to get the custom item name of this banned item, if exists, from the reversed map
     * @param value the banned item
     * @return the name of this banned item, null if does not exists
     */
    @Nullable
    public String getName(@NotNull final BannedItem value) {
        return reversed.get(value);
    }

    /**
     * @return the reversed map
     */
    @NotNull
    public Map<BannedItem, String> getReversed() {
        return reversed;
    }

    /**
     * @return the file configuration used to create this instance, null if it was not correctly loaded
     */
    @Nullable
    public FileConfiguration getItemsConfig() {
        return itemsConfig;
    }

    /**
     * @return the "items.yml" file of the BanItem plugin
     */
    @NotNull
    public File getItemsFile() {
        return itemsFile;
    }
}