package fr.andross.banitem.Utils;

import java.util.*;

public class WhitelistedWorld {
    private final String message;
    private final Map<BannedItem, Set<BanOption>> whitelisted = new HashMap<>();

    public WhitelistedWorld(final String message) {
        this.message = message;
    }

    public Map<BannedItem, Set<BanOption>> getWhitelisted() { return whitelisted; }

    public void addNewEntry(final BannedItem i, final List<BanOption> o) {
        whitelisted.put(i, new HashSet<>(o));
    }

    public String getMessage() { return message; }
}
