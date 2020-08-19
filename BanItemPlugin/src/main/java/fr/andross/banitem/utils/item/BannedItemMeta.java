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
package fr.andross.banitem.utils.item;

import fr.andross.banitem.BanUtils;
import fr.andross.banitem.utils.Listable;
import fr.andross.banitem.utils.debug.Debug;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class offers a way to store and compare the item meta
 * @version 2.4
 * @author Andross
 */
public class BannedItemMeta {
    private final Map<MetaType, Object> meta = new HashMap<>();

    /**
     * Creating a banned item meta from a configuration section
     * @param utils ban utils
     * @param section the configuration section to load
     * @param d the debugger
     * @throws Exception if something is not good
     */
    public BannedItemMeta(@NotNull final BanUtils utils, @NotNull final ConfigurationSection section, @NotNull final Debug d) throws Exception {
        for (final String key : section.getKeys(false)) {
            // Getting type
            final MetaType type;
            try {
                type = MetaType.valueOf(key.toUpperCase().replace("-", "_"));
            } catch (final Exception e) {
                d.clone().add(Listable.Type.METADATA, "&cUnknown metadata &e&l" + key + "&c.").sendDebug();
                throw new Exception();
            }

            // Getting object
            Object o;
            try {
                // Loading object
                o = section.get(key);
                if (o == null) continue;

                // Validating the object
                type.validate(o);
            } catch (final Exception e) {
                d.clone().add(Listable.Type.METADATA, "&cInvalid metadata value for metadata &e&l" + key + "&c.").sendDebug();
                throw new Exception();
            }

            // Preparing the object
            switch (type) {
                case DISPLAYNAME_EQUALS: case DISPLAYNAME_CONTAINS: {
                    // Colorizing the object
                    o = utils.color((String) o);
                    break;
                }

                case LORE_EQUALS: case LORE_CONTAINS: {
                    // Colorizing the list
                    final List<String> list = utils.getStringList(o);
                    o = list.stream().map(utils::color).collect(Collectors.toList());
                    break;
                }

                case DURABILITY: break; // Nothing to prepare

                case ENCHANTMENT_EQUALS: case ENCHANTMENT_CONTAINS: {
                    // Getting a map of enchantment
                    final Map<Enchantment, Integer> map = new HashMap<>();
                    List<String> list = utils.getStringList(o);
                    list = utils.getSplittedList(list);

                    for (final String string : list) {
                        try {
                            final String[] s = string.split(":");
                            final Enchantment enchantment = Enchantment.getByName(s[0].toUpperCase());
                            if (enchantment == null) throw new Exception();
                            final Integer level = Integer.valueOf(s[1]);
                            map.put(enchantment, level);
                        } catch (final Exception e) {
                            d.clone().add(Listable.Type.METADATA_ENCHANTMENT, "&cInvalid enchantment '" + string + "' for metadata &e&l" + key + "&c.").sendDebug();
                            throw new Exception();
                        }
                    }

                    o = map;
                    break;
                }

                case POTION: {
                    List<String> list = utils.getStringList(o);
                    list = utils.getSplittedList(list);
                    final Map<PotionEffectType, Integer> types = new HashMap<>();

                    for (final String string : list) {
                        try {
                            final PotionType potionType;
                            final int level;
                            if (string.contains(":")) {
                                final String[] s = string.split(":");
                                potionType = PotionType.valueOf(s[0].toUpperCase());
                                level = Integer.parseInt(s[1]);
                            } else {
                                potionType = PotionType.valueOf(string.toUpperCase());
                                level = 1;
                            }
                            types.put(potionType.getEffectType(), level);
                        } catch (final Exception e) {
                            d.clone().add(Listable.Type.METADATA_POTION, "&cInvalid potion '" + string + "' for metadata &e&l" + key + "&c.").sendDebug();
                            throw new Exception();
                        }
                    }

                    o = types;
                    break;
                }
            }

            this.meta.put(type, o);
        }
    }

    /**
     * Comparing the current item meta with the stored one
     * @param item the item stack to compare
     * @return true if the item meta matches, otherwise false
     */
    public boolean matches(@NotNull final ItemStack item) {
        // Matching any meta datas?
        final ItemMeta itemMeta = item.getItemMeta();
        for (final Map.Entry<MetaType, Object> e : meta.entrySet())
            if (!e.getKey().matches(item, itemMeta, e.getValue())) return false;

        return true;
    }

}
