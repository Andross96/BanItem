package fr.andross.banitem.Maps;

import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BannedItem;

import java.util.HashMap;
import java.util.Map;

public class Blacklisted extends HashMap<String, Map<BannedItem, Map<BanOption, String>>> {

    public void addNewBan(final String w, final BannedItem i, final Map<BanOption, String> o) {
        Map<BannedItem, Map<BanOption, String>> newmap = get(w);
        if(newmap == null) newmap = new HashMap<>();
        newmap.put(i, o);
        put(w, newmap);
    }

    public int getTotal() {
        int i = 0;
        for (Map<BannedItem, Map<BanOption, String>> map : values()) i += map.keySet().size();
        return i;
    }

}
