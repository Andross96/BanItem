package fr.andross.banitem;

import fr.andross.banitem.Utils.Ban.BanAnimation;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Class that contains the ban config
 * @version 2.0
 * @author Andross
 */
public final class BanConfig {
    /**
     * The FileConfiguration used
     */
    private final FileConfiguration config;

    /**
     * Ban animation (particle/sound)
     */
    private BanAnimation animation;

    /**
     * Better and more detailled loading error messages
     */
    private boolean betterDebug;

    /**
     * Block hoppers to take banned item from inventories (items must have the 'transfer' option)
     *
     * This is used to activate a specific listener which will also handle the hoppers transfers
     * This should not be modified externally, as it will also needs reload the listeners
     */
    private boolean hoppersBlock;

    /**
     * No permission message <i>(for /banitem)</i>
     */
    private String noPermission;

    /**
     * # Message cooldown when player try to pickup a banned item (in milliseconds)
     */
    private long pickupCooldown;

    /**
     * This will allow the plugin to call 'PlayerBanItemEvent' whenever an item is banned
     * This is used to allow others plugins to modify the behavior
     *
     * This is used to check if the plugin should call {@link fr.andross.banitem.Events.PlayerBanItemEvent} whenever an action should be banned
     */
    private boolean useEventApi;

    /**
     * This should not be instantiate. Use {@link BanItemAPI#load(CommandSender, FileConfiguration)} instead.
     * @param utils the plugin utils
     * @param sender the sender who executed this
     * @param config the configuration file used
     */
    BanConfig(@NotNull final BanUtils utils, @NotNull final CommandSender sender, @NotNull final FileConfiguration config) {
        this.config = config;

        // Loading animation
        animation = new BanAnimation(utils, sender, config);

        // Loading variables from config
        betterDebug = config.getBoolean("better-debug");
        hoppersBlock = config.getBoolean("hoppers-block");
        noPermission = utils.color(Objects.requireNonNull(config.getString("no-permission", "&cYou do not have permission")));
        pickupCooldown = config.getLong("pickup-message-cooldown");
        if (pickupCooldown < 0) {
            sender.sendMessage(utils.getPrefix() + utils.color("&cInvalid '&epickup-message-cooldown&c' from config."));
            pickupCooldown = 0;
        }
        useEventApi = config.getBoolean("use-event-api");
    }

    /**
     * Attempt to save this configuration into the yaml file.
     * @param file file to save the config into
     * @throws IOException for any file exception
     */
    public void save(@NotNull final File file) throws IOException {
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        final Map<String, Object> animations = animation.serialize();
        config.set("sound", animations.get("sound"));
        config.set("particle", animations.get("particle"));
        config.set("better-debug", betterDebug);
        config.set("hoppers-block", hoppersBlock);
        config.set("no-permission", noPermission);
        config.set("pickup-message-cooldown", pickupCooldown);
        config.set("use-event-api", useEventApi);

        config.save(file);
    }

    /**
     * File configuration used
     * @return the file configuration used
     */
    @NotNull
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Get the ban animation object
     * @return the ban animation object
     */
    @NotNull
    public BanAnimation getAnimation() {
        return animation;
    }

    /**
     * Set the ban animation object
     * @param animation the new ban animation object
     */
    public void setAnimation(@NotNull final BanAnimation animation) {
        this.animation = animation;
    }

    /**
     * Check if the plugin should use the better debug
     * @return true if better debug is enabled, otherwise false
     */
    public boolean isBetterDebug() {
        return betterDebug;
    }

    /**
     * Set the better debug
     * @param betterDebug if better debug should be enabled or not
     */
    public void setBetterDebug(final boolean betterDebug) {
        this.betterDebug = betterDebug;
    }

    /**
     * Get if the plugin should block also the hoppers (for items with options transfer)
     * @return true if the config is enabled, otherwise false
     */
    public boolean isHoppersBlock() {
        return hoppersBlock;
    }

    /**
     * Set if the the hoppers should also be blocked (for items with options transfer) or not.
     * <b>Important:</b> any modification of this should be used with {@link BanListener#loadListeners()} to enable the listener
     * @param hoppersBlock boolean
     */
    public void setHoppersBlock(final boolean hoppersBlock) {
        this.hoppersBlock = hoppersBlock;
    }

    /**
     * No permission message
     * @return the no permission message set in config, already colored.
     */
    @NotNull
    public String getNoPermission() {
        return noPermission;
    }

    /**
     * Set the no permission message
     * @param noPermission no permission message
     */
    public void setNoPermission(@NotNull final String noPermission) {
        this.noPermission = noPermission;
    }

    /**
     * Get the pick up cooldown
     * @return the pick up cooldown
     */
    public long getPickupCooldown() {
        return pickupCooldown;
    }

    /**
     * Set the pick up cooldown
     * @param pickupCooldown the pick up cooldown
     */
    public void setPickupCooldown(final long pickupCooldown) {
        this.pickupCooldown = pickupCooldown;
    }

    /**
     * If the event api is enabled
     * @return true if the event api is enabled, otherwise false
     */
    public boolean isUseEventApi() {
        return useEventApi;
    }

    /**
     * Set the use of the event api
     * @param useEventApi if true, the plugin will use the event api.
     */
    public void setUseEventApi(final boolean useEventApi) {
        this.useEventApi = useEventApi;
    }
}
