package fr.andross.banitem.Utils;

import fr.andross.banitem.Maps.CustomItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BanUtils {

    @Nullable
    public List<World> getWorldsFromString(final String s){
        if (s.equals("*")) return Bukkit.getWorlds();

        final List<World> worlds = new ArrayList<>();
        for (String w : s.trim().replaceAll("\\s+", "").split(",")) {
            final World world = Bukkit.getWorld(w);
            if (world == null) return null;
            worlds.add(world);
        }
        return worlds;
    }

    @Nullable
    public BannedItem getBannedItemFromString(final String material, final CustomItems custom_items){
        final Material m = Material.matchMaterial(material);
        return m == null ? custom_items.get(material) : new BannedItem(m);
    }

    @Nullable
    public List<BanOption> getBanOptionsFromString(final String options) {
        List<BanOption> optionsList = new ArrayList<>();

        switch (options.toLowerCase()) {
            case "*": return Arrays.asList(BanOption.values());
            case "*!":
                for (BanOption o : BanOption.values()) if (o != BanOption.CREATIVE) optionsList.add(o);
                return optionsList;
            case "*b": return Arrays.asList(BanOption.PLACE, BanOption.BREAK);
            case "*i": return Arrays.asList(BanOption.INTERACT, BanOption.USE);
            default:
                for (String option : options.toUpperCase().trim().replaceAll("\\s+", "").split(",")) {
                    try {
                        optionsList.add(BanOption.valueOf(option));
                    } catch (Exception e) {
                        return null;
                    }
                }
                break;
        }

        return optionsList;
    }
}
