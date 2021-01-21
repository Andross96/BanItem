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
package fr.andross.banitem.items;

import fr.andross.banitem.utils.BanVersion;
import fr.andross.banitem.utils.statics.Chat;
import fr.andross.banitem.utils.statics.EnchantmentHelper;
import fr.andross.banitem.utils.statics.Utils;
import fr.andross.banitem.utils.statics.list.Listable;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An item wrapper, which store custom item meta
 * @version 3.0
 * @author Andross
 */
public final class CustomBannedItem extends BannedItem {
    private final String name;
    private final Map<MetaType, Object> meta = new EnumMap<>(MetaType.class);

    public CustomBannedItem(@NotNull final Material m, @NotNull final String name) {
        super(m);
        this.name = name;
    }

    public CustomBannedItem(@NotNull final Material m, @NotNull final String name, @NotNull final ConfigurationSection section) throws Exception {
        super(m);
        this.name = name;

        for (final String key : section.getKeys(false)) {
            if (key.equalsIgnoreCase("material")) continue;

            // Getting type
            final MetaType type;
            try {
                type = MetaType.valueOf(key.toUpperCase().replace("-", "_"));
            } catch (final Exception e) {
                throw new Exception("&cunknown metadata &e&l" + key + "&c.");
            }

            // Getting object
            Object o = section.get(key);
            if (o == null) continue;
            if (!type.validate(o)) throw new Exception("&cinvalid metadata value for metadata &e&l" + key + "&c.");

            // Preparing the object
            switch (type) {
                case DISPLAYNAME_EQUALS: case DISPLAYNAME_CONTAINS: {
                    // Colorizing the object
                    o = Chat.color((String) o);
                    break;
                }

                case LORE_EQUALS: case LORE_CONTAINS: {
                    // Colorizing the list
                    final List<String> list = Listable.getStringList(o);
                    o = list.stream().map(Chat::color).collect(Collectors.toList());
                    break;
                }

                case LORE_LINE_CONTAINS: {
                    // Colorizing the string
                    o = Chat.color(o.toString());
                    break;
                }

                case DURABILITY: break; // Nothing to prepare

                case ENCHANTMENT_EQUALS: case ENCHANTMENT_CONTAINS: {
                    // Getting a map of enchantment
                    final Map<Enchantment, Integer> map = new HashMap<>();

                    for (final String string : Listable.getSplittedStringList(o)) {
                        final Enchantment enchantment;
                        final int level;
                        if (!string.contains(":")) {
                            enchantment = EnchantmentHelper.getEnchantment(string);
                            level = -1;
                        } else {
                            final String[] s = string.split(":");
                            enchantment = EnchantmentHelper.getEnchantment(s[0]);
                            try {
                                level = Integer.parseInt(s[1]);
                            } catch (final NumberFormatException e) {
                                throw new Exception("&cinvalid level '" + s[1] + "' for metadata &e&l" + key + "&c.");
                            }
                        }

                        if (enchantment == null) throw new Exception("&cinvalid enchantment '" + string + "' for metadata &e&l" + key + "&c.");
                        map.put(enchantment, level);
                    }

                    o = map;
                    break;
                }

                case POTION: {
                    final Map<PotionEffectType, Integer> types = new HashMap<>();

                    for (final String string : Listable.getSplittedStringList(o)) {
                        final PotionType potionType;
                        final int level;
                        try {
                            if (string.contains(":")) {
                                final String[] s = string.split(":");
                                potionType = PotionType.valueOf(s[0].toUpperCase());
                                level = Integer.parseInt(s[1]);
                            } else {
                                potionType = PotionType.valueOf(string.toUpperCase());
                                level = -1;
                            }
                            types.put(potionType.getEffectType(), level);
                        } catch (final Exception e) {
                            throw new Exception("&cinvalid potion '" + string + "' for metadata &e&l" + key + "&c.");
                        }
                    }

                    o = types;
                    break;
                }

                case MODELDATA_EQUALS: {
                    if (!BanVersion.v14OrMore)
                        throw new Exception("&ccan not use ModelData on MC<1.14");
                    break;
                }
            }

            this.meta.put(type, o);
        }
    }

    /**
     * Comparing the ItemMeta of the item with the item meta stored
     * @param item the item stack to compare
     * @return true if the item meta matches, otherwise false
     */
    @SuppressWarnings("unchecked")
    public boolean matches(@NotNull final ItemStack item) {
        // Not the same material
        if (item.getType() != getType()) return false;

        // Checking potion firstly, because of the incompatibility with MC<1.9
        if (meta.containsKey(MetaType.POTION)) {
            final Map<PotionEffectType, Integer> potions = Utils.getAllPotionEffects(item);
            if (potions.isEmpty()) return false;
            final Map<PotionEffectType, Integer> map = (Map<PotionEffectType, Integer>) meta.get(MetaType.POTION);
            if (map.entrySet().stream().noneMatch(entry -> {
                if (!potions.containsKey(entry.getKey())) return false;
                final int level = entry.getValue();
                return level <= 0 || level == potions.get(entry.getKey());
            })) return false;
        }

        if (!(!BanVersion.v9OrMore && item.getType() == Material.POTION && !item.hasItemMeta()) && !item.hasItemMeta()) return false;
        final ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return false;

        // All meta are matching?
        for (final Map.Entry<MetaType, Object> e : meta.entrySet()) {
            switch (e.getKey()) {
                case DISPLAYNAME_EQUALS:
                    if (!itemMeta.hasDisplayName() || !itemMeta.getDisplayName().equals(e.getValue().toString()))
                        return false;
                    break;

                case DISPLAYNAME_CONTAINS:
                    if (!itemMeta.hasDisplayName() || !itemMeta.getDisplayName().contains(e.getValue().toString()))
                        return false;
                    break;

                case LORE_EQUALS:
                    if (!itemMeta.hasLore() || !e.getValue().equals(itemMeta.getLore())) return false;
                    break;

                case LORE_CONTAINS: {
                    final List<String> itemLore = itemMeta.hasLore() ? itemMeta.getLore() : null;
                    if (itemLore == null) break;
                    final List<String> lore = (List<String>) e.getValue();
                    if (itemLore.stream().noneMatch(lore::contains)) return false;
                    break;
                }

                case LORE_LINE_CONTAINS: {
                    final List<String> itemLore = itemMeta.hasLore() ? itemMeta.getLore() : null;
                    if (itemLore == null) break;
                    final String lore = e.getValue().toString();
                    if (itemLore.stream().noneMatch(l -> l.contains(lore))) return false;
                    break;
                }

                case DURABILITY:
                    final int dura = !BanVersion.v13OrMore ? item.getDurability() : ((Damageable) itemMeta).getDamage();
                    if (dura != (int) e.getValue()) return false;
                    break;

                case ENCHANTMENT_EQUALS: {
                    final Map<Enchantment, Integer> enchants = Utils.getAllEnchants(item);
                    if (enchants.isEmpty()) return false;
                    final Map<Enchantment, Integer> map = (Map<Enchantment, Integer>) e.getValue();

                    for (final Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                        if (!enchants.containsKey(entry.getKey())) return false;
                        final int level = enchants.get(entry.getKey());
                        if (entry.getValue() > 0 && entry.getValue() != level) return false;
                    }

                    break;
                }

                case ENCHANTMENT_CONTAINS: {
                    final Map<Enchantment, Integer> enchants = Utils.getAllEnchants(item);
                    if (enchants.isEmpty()) return false;
                    final Map<Enchantment, Integer> map = (Map<Enchantment, Integer>) e.getValue();

                    if (map.entrySet().stream().noneMatch(entry -> {
                        if (!enchants.containsKey(entry.getKey())) return false;
                        final int level = entry.getValue();
                        return level <= 0 || level == enchants.get(entry.getKey());
                    })) return false;

                    break;
                }

                case POTION: {
                    // Already checked before
                    break;
                }

                case MODELDATA_EQUALS:
                    if (!itemMeta.hasCustomModelData() || itemMeta.getCustomModelData() != (int) e.getValue()) return false;
                    break;
            }
        }

        return true;
    }

    /**
     * Get the custom banned item name from customitems.yml
     * @return the custom banned item name from customitems.yml
     */
    @NotNull
    public String getName() {
        return name;
    }


    /**
     * Get the map of meta to compare
     * @return the map of meta to compare
     */
    @NotNull
    public Map<MetaType, Object> getMeta() {
        return meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomBannedItem that = (CustomBannedItem) o;
        return Objects.equals(meta, that.meta) && getType() == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), meta);
    }
}
