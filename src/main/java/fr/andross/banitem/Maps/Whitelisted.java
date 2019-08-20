package fr.andross.banitem.Maps;
import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BannedItem;
import fr.andross.banitem.Utils.WhitelistedWorld;

import java.util.HashMap;
import java.util.List;

public class Whitelisted extends HashMap<String, WhitelistedWorld> {

    public void addNewException(final String world, final String message, final BannedItem i, final List<BanOption> o) {
        WhitelistedWorld wlw = get(world);
        if (wlw == null) wlw = new WhitelistedWorld(message);
        wlw.addNewEntry(i, o);
        put(world, wlw);
    }

    public int getTotal() {
        int i = 0;
        for (WhitelistedWorld wlw : values()) i += wlw.getWhitelisted().keySet().size();
        return i;
    }

}
