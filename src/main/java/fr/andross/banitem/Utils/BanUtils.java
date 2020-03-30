package fr.andross.banitem.Utils;

import fr.andross.banitem.BanItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class BanUtils {
    private static final List<BanOption> allOptions = Arrays.stream(BanOption.values()).filter(o -> o != BanOption.CREATIVE && o != BanOption.DELETE).collect(Collectors.toList());

    @NotNull
    public static List<World> getWorldsFromString(@NotNull final CommandSender sender, final boolean blacklist, @NotNull final String s) {
        if (s.equals("*")) return Bukkit.getWorlds();

        final String section = blacklist ? "blacklist" : "whitelist";
        final List<World> worlds = new ArrayList<>();
        final List<World> disabledWorlds = new ArrayList<>();
        for (String w : s.trim().replaceAll("\\s+", "").split(",")) {
            if (w.equals("*")) {
                worlds.addAll(Bukkit.getWorlds());
                continue;
            }

            final boolean remove = w.startsWith("!");
            if (remove) w = w.substring(1);

            final World world = Bukkit.getWorld(w);
            if (world == null) {
                sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &cUnknown world &e" + w + "&c set in " + section + " of config.yml"));
                continue;
            }

            if (remove) disabledWorlds.add(world); else worlds.add(world);
        }

        worlds.removeAll(disabledWorlds);
        return worlds;
    }

    @NotNull
    public static List<BanOption> getBanOptionsFromString(@NotNull final CommandSender sender, final boolean blacklist, @NotNull final String options, @NotNull final String material, @NotNull final String world) {
        if (options.equals("*")) return allOptions;

        final String section = blacklist ? "blacklist" : "whitelist";
        final List<BanOption> optionsList = new ArrayList<>();
        final List<BanOption> disabledOptions = new ArrayList<>();
        for (String option : options.toUpperCase().trim().replaceAll("\\s+", "").split(",")) {
            try {
                if (option.equals("*")) {
                    optionsList.addAll(allOptions);
                    continue;
                }

                final boolean remove = option.startsWith("!");
                if (remove) option = option.substring(1);
                final BanOption banOption = BanOption.valueOf(option);

                if (remove) disabledOptions.add(banOption); else optionsList.add(banOption);
            } catch (final Exception e) {
                sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &cInvalid option &e" + option + "&c for item &e" + material + "&c set for world &e" + world + "&c in " + section + " of config.yml"));
            }
        }

        optionsList.removeAll(disabledOptions);
        return optionsList;
    }

    public static void deleteItemFromInventory(@NotNull final HumanEntity p, @NotNull final Inventory... invs) {
        final BanItem pl = BanItem.getInstance();
        final String world = p.getWorld().getName();
        final Map<Material, Map<BanOption, String>> map = pl.getBanDatabase().getBlacklist().get(world);
        final Map<BannedItem, Map<BanOption, String>> customMap = pl.getBanDatabase().getBlacklist().getCustomItems(world);
        if (map == null && customMap == null) return;

        for (final Inventory inv : invs) {
            for (int i = 0; i < inv.getSize(); i++) {
                final ItemStack item = inv.getItem(i);
                if (item == null || item.getType() == Material.AIR) continue;

                // Handling normal items
                if (map != null) {
                    final Map<BanOption, String> options = map.get(item.getType());
                    if (options != null && options.containsKey(BanOption.DELETE)) {
                        if (hasPermission(p, world, item.getType().name().toLowerCase(), null, BanOption.DELETE.getName())) continue;
                        final String message = options.get(BanOption.DELETE);
                        if (message != null && !message.isEmpty()) p.sendMessage(message);
                        inv.clear(i);
                    }
                }

                // Handling custom items
                if (customMap == null) continue;
                final BannedItem bi = new BannedItem(item);
                final Map<BanOption, String> options = customMap.get(bi);
                if (options != null && options.containsKey(BanOption.DELETE)) {
                    final String customItemName = pl.getBanDatabase().getCustomItems().getName(bi);
                    if (hasPermission(p, world, null, customItemName, BanOption.DELETE.getName())) continue;
                    final String message = options.get(BanOption.DELETE);
                    if (message != null && !message.isEmpty()) p.sendMessage(message);
                    inv.clear(i);
                }
            }
        }
    }

    public static boolean hasPermission(@NotNull final Permissible p, @NotNull final String w, @Nullable final String item, @Nullable final String customName, @Nullable final String option) {
        if (item != null) {
            if (p.hasPermission("banitem.bypass." + w + "." + item + "." + option)) return true;
            if (p.hasPermission("banitem.bypass.allworlds." + item + "." + option)) return true;
        }
        if (customName != null) {
            if (p.hasPermission("banitem.bypass." + w + "." + customName + "." + option)) return true;
            if (p.hasPermission("banitem.bypass.allworlds." + customName + "." + option)) return true;
        }
        return false;
    }

    public static boolean isNullOrAir(@Nullable final ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }
}
