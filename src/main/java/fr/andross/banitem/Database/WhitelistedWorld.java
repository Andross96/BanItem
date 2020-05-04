package fr.andross.banitem.Database;

import fr.andross.banitem.Options.BanOption;
import fr.andross.banitem.Options.BanOptionData;
import fr.andross.banitem.Utils.Item.BannedItem;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Map containing all allowed items of a world
 * @version 2.1
 * @author Andross
 */
public final class WhitelistedWorld extends ItemMap {
    private final World world;
    private final List<String> messages = new ArrayList<>();
    private final Set<BanOption> ignored = new HashSet<>();

    /**
     * This constructor should not be used like this <i>(as it will not been stored into the Whitelist map)</i>
     * Use {@link Whitelist#createNewWhitelistedWorld(World, List, List)} instead.
     * @param world bukkit world
     * @param messages list of messages to send if the item is not allowed
     * @param ignored list of ignored options
     */
    public WhitelistedWorld(@NotNull final World world, @Nullable final List<String> messages, @Nullable final List<BanOption> ignored) {
        this.world = world;
        if (messages != null) this.messages.addAll(messages);
        if (ignored != null) this.ignored.addAll(ignored);
    }

    /**
     * This will add a new entry to the whitelist
     *
     * @param item banned item <i>({@link BannedItem})</i>
     * @param options map containing {@link BanOption} and their respective {@link BanOptionData}
     */
    public void addNewEntry(@NotNull final BannedItem item, @NotNull final Map<BanOption, BanOptionData> options) {
        final Map<BanOption, BanOptionData> map = getOrDefault(item, new HashMap<>());
        map.putAll(options);
        put(item, map);
    }

    /**
     * @return the bukkit world
     */
    @NotNull
    public World getWorld() {
        return world;
    }

    /**
     * @return list of messages, empty if none configured
     */
    @NotNull
    public List<String> getMessages() {
        return messages;
    }

    /**
     * @return set of ignored options, empty if none configured
     */
    @NotNull
    public Set<BanOption> getIgnored() {
        return ignored;
    }

    /**
     * @return total amount of items allowed in this world
     */
    public int getTotal() {
        return values().size();
    }
}
