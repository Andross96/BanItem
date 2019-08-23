package fr.andross.banitem.Maps;

import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BannedItem;

import java.util.*;

public class WhitelistWorld {
    private final String message;
    private final Set<BanOption> ignored = new HashSet<>();
    private final Map<BannedItem, Set<BanOption>> whitelist = new HashMap<>();

    public WhitelistWorld(final String message, final List<BanOption> ignored) {
        this.message = message;
        if (ignored != null) this.ignored.addAll(ignored);
    }

    public Map<BannedItem, Set<BanOption>> getWhitelist() { return whitelist; }

    public void addNewEntry(final BannedItem i, final List<BanOption> o) {
        whitelist.put(i, new HashSet<>(o));
    }

    public String getMessage() { return message; }

    public boolean isIgnored(final BanOption option) { return ignored.contains(option); }
}
