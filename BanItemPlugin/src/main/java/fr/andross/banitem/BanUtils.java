package fr.andross.banitem;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.andross.banitem.database.Blacklist;
import fr.andross.banitem.options.BanData;
import fr.andross.banitem.options.BanDataType;
import fr.andross.banitem.options.BanOption;
import fr.andross.banitem.options.BanOptionData;
import fr.andross.banitem.utils.BanVersion;
import fr.andross.banitem.utils.Listable;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.item.BannedItem;
import fr.andross.banitem.utils.item.BannedItemMeta;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * An utility class for the plugin
 * @version 2.3
 * @author Andross
 */
public final class BanUtils extends Listable {
    /**
     * Map for pickup cooldowns
     */
    private final Map<UUID, Long> pickupCooldowns = new HashMap<>();

    /**
     * A set containing the players in log mode
     */
    private final Set<UUID> logging = new HashSet<>();

    /**
     * This should not be instantiate. Use {@link BanItemAPI#load(CommandSender, FileConfiguration)} instead.
     */
    BanUtils(@NotNull final BanItem pl) {
        super(pl);
    }

    /**
     * Get a map of options and options data from a section
     * @param worlds list of worlds
     * @param section section
     * @param d debug
     * @return a map containing the ban options and their respective data from the ConfigurationSection
     */
    @NotNull
    public Map<BanOption, BanOptionData> getBanOptionsFromItemSection(@NotNull final List<World> worlds, @Nullable final ConfigurationSection section, @NotNull final Debug d) {
        final Map<BanOption, BanOptionData> options = new HashMap<>();
        if (section == null) return options;
        final List<BanOption> ignoredOptions = new ArrayList<>();
        for (final String key : section.getKeys(false)) {
            for (String option : key.toUpperCase().trim().replaceAll("\\s+", "").split(",")) {
                final Debug newDebug = d.clone();
                try {
                    final BanOptionData bo = getBanOptionsForItem(worlds, section, key, newDebug.add(Type.OPTION, key));
                    if (option.equals("*")) {
                        for (final BanOption type : getOptions()) options.put(type, bo);
                        continue;
                    }
                    final boolean remove = option.startsWith("!");
                    if (remove) option = option.substring(1);
                    final BanOption banOption = BanOption.valueOf(option);
                    if (remove) ignoredOptions.add(banOption); else options.put(banOption, bo);
                } catch (final Exception e) {
                    newDebug.add(Type.OPTION, "&cUnknown option &e&l" + option + "&c.").sendDebug();
                }
            }
        }
        // Removing ignored options
        for (final BanOption type : ignoredOptions) options.remove(type);

        return options;
    }

    /**
     * Get ban options data for a section
     * @param worlds list of worlds, used for regions
     * @param itemSection the option section
     * @param key the current data key
     * @param d debug
     * @return options data from the section of the specific option
     * @throws Exception if one of the data is invalid
     */
    @NotNull
    public BanOptionData getBanOptionsForItem(@NotNull final List<World> worlds, @NotNull final ConfigurationSection itemSection, @NotNull final String key, @NotNull final Debug d) throws Exception {
        final BanOptionData banOptionData = new BanOptionData();
        final ConfigurationSection section = itemSection.getConfigurationSection(key);
        if (section == null) {
            final List<String> messages = getStringList(itemSection.get(key));
            if (!messages.isEmpty()) banOptionData.put(BanDataType.MESSAGE, messages.stream().filter(this::isNotNullOrEmpty).map(this::color).collect(Collectors.toList()));
            return banOptionData;
        }

        // Handling data
        for (final String option : section.getKeys(false)) {
            final String optionLower = option.toLowerCase();
            switch (optionLower) {
                case "cooldown":
                    banOptionData.put(BanDataType.COOLDOWN, section.getLong("cooldown"));
                    break;

                case "entity": {
                    final List<EntityType> list = getList(Type.ENTITY, section.get(option), d.add(Type.ENTITY, option), null);
                    if (!list.isEmpty())
                        banOptionData.put(BanDataType.ENTITY, new HashSet<>(list));
                    break;
                }

                case "gamemode": {
                    final List<GameMode> list = getList(Type.GAMEMODE, section.get(option), d.add(Type.GAMEMODE, option), null);
                    if (!list.isEmpty())
                        banOptionData.put(BanDataType.GAMEMODE, new HashSet<>(list));
                    break;
                }

                case "inventory-from": case "inventory-to": {
                    final List<InventoryType> list = getList(Type.INVENTORY, section.get(option), d.add(Type.INVENTORY, option), null);
                    if (!list.isEmpty())
                        banOptionData.put(optionLower.equals("inventory-from") ? BanDataType.INVENTORY_FROM : BanDataType.INVENTORY_TO, new HashSet<>(list));
                    break;
                }

                case "log":
                    banOptionData.put(BanDataType.LOG, section.getBoolean("log"));
                    break;

                case "material": {
                    final List<BannedItem> list = getList(Type.ITEM, section.get(option), d.add(Type.ITEM, option), null);
                    if (!list.isEmpty())
                        banOptionData.put(BanDataType.MATERIAL, list.stream().map(BannedItem::getType).collect(Collectors.toSet()));
                    break;
                }

                case "message": {
                    final List<String> messages = getStringList(section.get(option));
                    if (!messages.isEmpty())
                        banOptionData.put(BanDataType.MESSAGE, messages.stream().filter(this::isNotNullOrEmpty).map(this::color).collect(Collectors.toList()));
                    break;
                }

                case "metadata": {
                    try {
                        final ConfigurationSection metadataSection = section.getConfigurationSection(option);
                        if (metadataSection == null) continue;
                        final BannedItemMeta meta = new BannedItemMeta(this, metadataSection, d.add(Type.METADATA, option));
                        banOptionData.put(BanDataType.METADATA, meta);
                    } catch (final Exception ignored) {
                        continue; // the error is debugged with the help of the debugger
                    }
                    break;
                }

                case "region": {
                    if (!pl.getHooks().isWorldGuardEnabled()) {
                        d.clone().add(Type.REGION, "&cUsed region metadata, but WorldGuard is not available.").sendDebug();
                        continue;
                    }

                    final List<ProtectedRegion> regions = getList(Type.REGION, section.get(option), d.add(Type.REGION, option), worlds);
                    if (!regions.isEmpty())
                        banOptionData.put(BanDataType.REGION, new HashSet<>(regions));
                    break;
                }
            }
        }

        return banOptionData;
    }

    /**
     * Method to check and delete banned item from any inventories
     * @param player any player
     * @param invs inventories
     */
    public void deleteItemFromInventory(@NotNull final Player player, @NotNull final Inventory... invs) {
        // Op or all permissions?
        if (player.isOp() || player.hasPermission("banitem.bypass.*")) return;

        // Getting blacklist
        final Blacklist blacklist = pl.getBanDatabase().getBlacklist();
        if (!blacklist.containsKey(player.getWorld())) return; // nothing banned in this world

        // Checking!
        for (final Inventory inv : invs) {
            for (int i = 0; i < inv.getSize(); i++) {
                final ItemStack item = inv.getItem(i);
                if (isNullOrAir(item)) continue;
                if (pl.getApi().isBanned(player, player.getLocation(), new BannedItem(item), BanOption.DELETE))
                    inv.clear(i);
            }
        }
    }

    /**
     * This method is used to send a ban message to player, if exists.
     * Mainly used for blacklist
     * @param player send the message to {@link HumanEntity}
     * @param itemName name of the item
     * @param option the ban option <i>(used for log)</i>
     * @param data the ban data <i>(containing the messages)</i>
     */
    public void sendMessage(@NotNull final Player player, @NotNull final String itemName, @NotNull final BanOption option, @Nullable final BanOptionData data) {
        if (data == null) return; // no message neither log

        // Checking pick up cooldown, to prevent spam
        if (option == BanOption.PICKUP) {
            final Long time = pickupCooldowns.get(player.getUniqueId());
            if (time == null) pickupCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            else {
                if (time + 1000 > System.currentTimeMillis()) return;
                else pickupCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }

        // Getting datas
        final List<String> message = data.getData(BanDataType.MESSAGE);
        final boolean log = data.getLog();

        // Logging?
        if (log && !logging.isEmpty()) {
            // Preparing message
            final String m =  color("&7&o[BanItem] " + // prefix
                        player.getName() + " " + // player name
                        "(" + player.getWorld().getName() + ") " + // world
                        "[" + itemName + "]: " + // item name
                        option.name()); // option

            // Sending log message
            for (final UUID uuid : logging) {
                final Player t = Bukkit.getPlayer(uuid);
                if (t != null) t.sendMessage(m);
            }
        }

        // No message set
        if (message == null) return;

        // Sending message & animation
        message.forEach(player::sendMessage);
        pl.getBanConfig().getAnimation().runAnimation(player);
    }

    /**
     * This method is used to send a ban message to player, if exists.
     * Mainly used for whitelist
     * @param player send the message to {@link HumanEntity}
     * @param option the ban option <i>(used for log)</i>
     * @param messages list of messages
     */
    public void sendMessage(@NotNull final HumanEntity player, @NotNull final BanOption option, @NotNull final List<String> messages) {
        if (messages.isEmpty()) return; // no message

        // Checking pick up cooldown, to prevent spam
        if (option == BanOption.PICKUP) {
            final Long time = pickupCooldowns.get(player.getUniqueId());
            if (time == null) pickupCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            else {
                if (time + 1000> System.currentTimeMillis()) return;
                else pickupCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }

        // Sending message & animation
        messages.forEach(player::sendMessage);
        if (player instanceof Player) pl.getBanConfig().getAnimation().runAnimation((Player) player);
    }


    /**
     * Method to check if the player has the bypass permission for either the item <i>(material name)</i> or custom name
     * @param player player to check
     * @param item material name tolowercase of the item to check <i>(without metadata)</i>
     * @param customName custom item name tolowercase <i>(has metadata)</i>
     * @param option option name
     * @param data additional data to check
     * @return true if the player has the permission to bypass the ban, otherwise false
     */
    public boolean hasPermission(@NotNull final Player player, @Nullable final String item, @Nullable final String customName, @NotNull final BanOption option, @Nullable final BanData... data) {
        final String world = player.getWorld().getName().toLowerCase();

        if (item != null) {
            if (!isNullOrEmpty(data)) {
                for (final BanData bd : data) {
                    if (player.hasPermission("banitem.bypass." + world + "." + item + "." + option.getName() + "." + bd.getType().getName())) return true;
                    if (player.hasPermission("banitem.bypass.allworlds." + item + "." + option.getName() + "." + bd.getType().getName())) return true;
                }
            } else {
                if (player.hasPermission("banitem.bypass." + world + "." + item + "." + option.getName())) return true;
                if (player.hasPermission("banitem.bypass.allworlds." + item + "." + option.getName())) return true;
            }
        }

        if (customName != null) {
            if (!isNullOrEmpty(data)) {
                for (final BanData bd : data) {
                    if (player.hasPermission("banitem.bypass." + world + "." + customName + "." + option.getName() + "." + bd.getType().getName())) return true;
                    return player.hasPermission("banitem.bypass.allworlds." + customName + "." + option.getName() + "." + bd.getType().getName());
                }
            } else {
                if (player.hasPermission("banitem.bypass." + world + "." + customName + "." + option.getName())) return true;
                return player.hasPermission("banitem.bypass.allworlds." + customName + "." + option.getName());
            }
        }

        return false;
    }

    /**
     * Get a friendly string of remaining time
     * @param time time in millis
     * @return a friendly string of remaining time
     */
    @NotNull
    public String getCooldownString(final long time) {
        if (time > 0 && time < 1000) return "0." + Integer.parseInt(Long.toString(time).substring(0, 1)) + "s";
        if (time <= 0) return "1s"; // soon

        final long days = TimeUnit.MILLISECONDS.toDays(time);
        final long hours = TimeUnit.MILLISECONDS.toHours(time) % 24;
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;

        final StringBuilder sb = new StringBuilder();
        if (days != 0) sb.append(days).append("d");
        if (hours != 0) sb.append(hours).append("h");
        if (minutes != 0) sb.append(minutes).append("m");
        if (seconds != 0) sb.append(seconds).append("s");

        return sb.length() == 0 ? "1s" : sb.toString();
    }

    /**
     * Used to check a player armor inventory
     * @param p player
     */
    public void checkPlayerArmors(final Player p) {
        final EntityEquipment ee = p.getEquipment();
        if (ee == null) return;

        final ItemStack[] items = new ItemStack[] { ee.getHelmet(), ee.getChestplate(), ee.getLeggings(), ee.getBoots() };
        int i = -1;

        for (final ItemStack item : items) {
            i++;
            if (isNullOrAir(item)) continue;
            if (!pl.getApi().isBanned(p, p.getLocation(), item, BanOption.WEAR)) continue;

            // Item can not be weared in this world
            switch (i) {
                case 0: ee.setHelmet(null); break;
                case 1: ee.setChestplate(null); break;
                case 2: ee.setLeggings(null); break;
                case 3: ee.setBoots(null); break;
                default: break;
            }
            final int freeSlot = p.getInventory().firstEmpty();
            // No empty space, dropping it, else adding it into inventory
            if (freeSlot == -1) p.getWorld().dropItemNaturally(p.getLocation(), item);
            else p.getInventory().setItem(freeSlot, item);
        }
    }

    /**
     * Quick utils to check if the item is null or if its type is Material.AIR
     * @param item the {@link ItemStack}
     * @return true if the ItemStack is null or AIR, otherwise false
     */
    public boolean isNullOrAir(@Nullable final ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    /**
     * Used to check if an array if null or empty.
     * Mainly used to check varargs.
     * @param a array
     * @return true is the array is null or empty, otherwise false
     */
    public boolean isNullOrEmpty(@Nullable final Object[] a) {
        return a == null || a.length == 0 || a[0] == null;
    }

    /**
     * Used to check if a String is <b>not</b> null nor empty
     * @param s the string to check
     * @return true if the string is not null nor empty, otherwise false
     */
    public boolean isNotNullOrEmpty(@Nullable final String s) {
        return s != null && !s.isEmpty();
    }

    /**
     * Get the item from the player hand, even AIR, regardless the version
     * @param p the {@link Player}
     * @return the ItemStack in the player's hand, possibly AIR
     */
    @NotNull
    public ItemStack getItemInHand(@NotNull final Player p) {
        final EntityEquipment ee = p.getEquipment();
        if (ee == null) return new ItemStack(Material.AIR);
        final ItemStack itemInHand = BanVersion.v9OrMore ? ee.getItemInMainHand() : ee.getItemInHand();
        return itemInHand == null ? new ItemStack(Material.AIR) : itemInHand;
    }

    /**
     * Get the display name of the item, empty string if empty
     * @param item the itemstack
     * @return the display name of the item, otherwise an empty string
     */
    @NotNull
    public String getItemDisplayname(@NotNull final ItemStack item) {
        if (!item.hasItemMeta()) return "";
        final ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return "";
        return !itemMeta.hasDisplayName() ? "" : itemMeta.getDisplayName();
    }

    /**
     * Translate the color codes to make the string colored
     * @param text the text to translate
     * @return a colored string
     */
    @NotNull
    public String color(@NotNull final String text) {
        char[] b = text.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = '\u00A7';
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }

    /**
     * Sending a prefixed and colored message to sender
     * @param sender sender
     * @param message message
     */
    public void sendMessage(@NotNull final CommandSender sender, @NotNull final String message) {
        sender.sendMessage(pl.getBanConfig().getPrefix() + color(message));
    }

    /**
     * Send a message if an update is available
     * This <b>must</b> be executed async
     */
    public void checkForUpdate() {
        try {
            final HttpsURLConnection c = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=67701").openConnection();
            c.setRequestMethod("GET");
            final String lastVersion = new BufferedReader(new InputStreamReader(c.getInputStream())).readLine();
            if (!lastVersion.equals(pl.getDescription().getVersion()))
                sendMessage(Bukkit.getConsoleSender(), "&aA newer version (&lv" + lastVersion + "&a) is available!");
        } catch (final IOException e) {
            sendMessage(Bukkit.getConsoleSender(), "&cUnable to communicate with the spigot api to check for newer versions.");
        }
    }

    /**
     * Get the pick up cooldowns map
     * @return map containing the cooldowns for pick up messages
     */
    @NotNull
    public Map<UUID, Long> getPickupCooldowns() {
        return pickupCooldowns;
    }

    /**
     * This map contains the players who activated the log in game with <i>/banitem log</i>
     * Players which log mode is activated will receive the logs messages for the banned items, if set in config
     * @return set of players uuid
     */
    @NotNull
    public Set<UUID> getLogging() {
        return logging;
    }
}
