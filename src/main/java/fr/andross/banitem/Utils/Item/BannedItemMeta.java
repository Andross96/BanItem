package fr.andross.banitem.Utils.Item;

import fr.andross.banitem.BanUtils;
import fr.andross.banitem.Utils.Debug.Debug;
import fr.andross.banitem.Utils.Listable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class offers a way to store and compare the item meta
 * @version 2.1
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
            try {
                // Loading object
                Object o = section.get(key);
                if (o == null) continue;

                // Validating the object
                type.validate(o);

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
                            final String[] s = string.split(":");
                            final Enchantment enchantment = Enchantment.getByName(s[0].toUpperCase());
                            if (enchantment == null) throw new Exception();
                            final Integer level = Integer.valueOf(s[1]);
                            map.put(enchantment, level);
                        }

                        o = map;
                        break;
                    }

                    case POTION: {
                        List<String> list = utils.getStringList(o);
                        list = utils.getSplittedList(list);
                        final Set<PotionEffectType> types = new HashSet<>();

                        for (final String potionEffectTypeName : list) {
                            final PotionEffectType potionEffectType = PotionEffectType.getByName(potionEffectTypeName);
                            if (potionEffectType == null) throw new Exception();
                            types.add(potionEffectType);
                        }

                        o = types;
                        break;
                    }
                }

                this.meta.put(type, o);
            } catch (final Exception e) {
                final Listable.Type listType;
                switch (type) {
                    case ENCHANTMENT_CONTAINS: case ENCHANTMENT_EQUALS: listType = Listable.Type.METADATA_ENCHANTMENT; break;
                    case POTION: listType = Listable.Type.METADATA_POTION; break;
                    default: listType = null; break;
                }
                d.clone().add(listType == null ? Listable.Type.METADATA : listType, "&cInvalid metadata value for metadata &e&l" + key + "&c.").sendDebug();
                throw new Exception();
            }
        }
    }

    /**
     * Comparing the current item meta with the stored one
     * @param item the item stack to compare
     * @return true if the item meta matches, otherwise false
     */
    public boolean matches(@NotNull final ItemStack item) {
        if (!item.hasItemMeta()) return false;
        final ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return false;

        // Matching any meta datas?
        for (final Map.Entry<MetaType, Object> e : meta.entrySet())
            if (!e.getKey().matches(item, itemMeta, e.getValue())) return false;

        return true;
    }

}
