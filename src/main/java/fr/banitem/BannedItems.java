package fr.banitem;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

class BannedItems {
    private final Map<String, Map<Material, BannedItem>> banned = new HashMap<>();

    BannedItem getBannedItem(final String world, final Material m){
        if(!banned.containsKey(world)) return null;
        if(!banned.get(world).containsKey(m)) return null;
        return banned.get(world).get(m);
    }

    void addBannedItem(final String world, final Material m, final BannedItem bi){
        Map<Material, BannedItem> map = banned.get(world);
        if(map == null) map = new HashMap<>();
        map.put(m, bi);
        banned.put(world, map);
    }

    void clearAll(){ banned.clear(); }
}
