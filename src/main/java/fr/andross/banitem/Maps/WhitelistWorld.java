package fr.andross.banitem.Maps;

import fr.andross.banitem.Utils.BanOption;
import fr.andross.banitem.Utils.BannedItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WhitelistWorld {
    private final String message;
    private final Set<BanOption> ignored = new HashSet<>();
    private final Map<Material, Set<BanOption>> whitelist = new HashMap<>();
    private final Map<BannedItem, Set<BanOption>> customItemsWhitelist = new HashMap<>();

    public WhitelistWorld(final String message, final List<BanOption> ignored) {
        this.message = message;
        if (ignored != null) this.ignored.addAll(ignored);
    }

    /**
     * Add the banned <b>options</b> for the <b>material</b> into the whitelist of this world.
     *
     * @param material the material
     * @param options the list of ban options
     */
    public void addNewEntry(@NotNull final Material material, @NotNull final List<BanOption> options) {
        whitelist.put(material, new HashSet<>(options));
    }

    /**
     * Add the banned <b>options</b> for this exact <b>item</b> <i>(including durability, data, meta etc.)</i> into the whitelist of this world.
     *
     * @param item the ItemStack
     * @param options the list of ban options
     */
    public void addNewEntry(@NotNull final ItemStack item, @NotNull final List<BanOption> options) {
        customItemsWhitelist.put(new BannedItem(item), new HashSet<>(options));
    }

    void addNewEntry(@NotNull final BannedItem bi, @NotNull final List<BanOption> o) {
        customItemsWhitelist.put(bi, new HashSet<>(o));
    }

    /**
     * Get a Set of banned options for the <b>item</b>.
     *
     * @param item the ItemStack
     * @return set of banned options, empty if no ban options is set for the item
     */
    @NotNull
    public Set<BanOption> getBanOptions(@NotNull final ItemStack item) {
        return getBanOptions(new BannedItem(item));
    }

    public Set<BanOption> getBanOptions(final BannedItem bi) {
        final Set<BanOption> options = new HashSet<>();
        if (whitelist.containsKey(bi.getType())) options.addAll(whitelist.get(bi.getType()));
        if (customItemsWhitelist.containsKey(bi)) options.addAll(customItemsWhitelist.get(bi));
        return options;
    }

    /**
     * Get the whitelist message for this world
     *
     * @return whitelist message for this world
     */
    public String getMessage() { return message; }

    /**
     * Check if the ban option is ignored for this world
     *
     * @param option The ban option
     * @return true if the option is ignored (not checked by the plugin for this world), otherwise false
     */
    public boolean isIgnored(@NotNull final BanOption option) { return ignored.contains(option); }

    public void remove(final Material m) {
        whitelist.remove(m);
    }

    public void remove(final BannedItem bi) {
        customItemsWhitelist.remove(bi);
    }

    public int count() { return whitelist.size() + customItemsWhitelist.size(); }
}
