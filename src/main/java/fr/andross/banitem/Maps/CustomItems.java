package fr.andross.banitem.Maps;

import fr.andross.banitem.Utils.BannedItem;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CustomItems extends HashMap<String, BannedItem> {
    private final Map<BannedItem, String> reversed = new HashMap<>();

    @Override
    public BannedItem put(final String key, final BannedItem value) {
        reversed.put(value, key);
        return super.put(key, value);
    }

    @Override
    public void clear(){
        reversed.clear();
        super.clear();
    }

    @Override
    public BannedItem remove(final Object key) {
        reversed.remove(get(key));
        return super.remove(key);
    }

    @Nullable
    public String getName(final BannedItem value){
        return reversed.get(value);
    }
}