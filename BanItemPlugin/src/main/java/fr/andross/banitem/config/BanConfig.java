package fr.andross.banitem.config;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.BanItemAPI;
import fr.andross.banitem.BanUtils;
import fr.andross.banitem.options.BanOption;
import fr.andross.banitem.utils.BanAnimation;
import fr.andross.banitem.utils.Listable;
import fr.andross.banitem.utils.debug.Debug;
import fr.andross.banitem.utils.debug.DebugMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class that contains the ban config
 * @version 2.3
 * @author Andross
 */
public final class BanConfig {
    /**
     * The FileConfiguration used
     */
    private final FileConfiguration config;

    /**
     * Better and more detailled loading error messages
     */
    private boolean betterDebug;

    /**
     * Plugin prefix
     */
    private String prefix;

    /**
     * Contains a list of configurations for an option
     */
    private final BanConfigOptions optionsConfig;

    /**
     * No permission message <i>(for /banitem)</i>
     */
    private String noPermission;

    /**
     * This will allow the plugin to call 'PlayerBanItemEvent' whenever an item is banned
     * This is used to allow others plugins to modify the behavior
     *
     * This is used to check if the plugin should call {@link fr.andross.banitem.utils.events.PlayerBanItemEvent} whenever an action should be banned
     */
    private boolean useEventApi;

    /**
     * Check if an update is available or not
     */
    private boolean checkUpdate;

    /**
     * Set of options that should have maximum listening priority
     * Giving maximum priority will force the ban item plugin to have the final word on an event
     * This is used mainly to also block other plugins events, if the action is banned
     * If you modify this list, you'll also have to reload the listeners
     */
    private final Set<BanOption> priority = new HashSet<>();

    /**
     * Hooks manager
     */
    private final BanConfigHooks hooks;

    /**
     * Ban animation (particle/sound)
     */
    private BanAnimation animation;

    /**
     * This should not be instantiate. Use {@link BanItemAPI#load(CommandSender, FileConfiguration)} instead.
     * @param pl the plugin instance
     * @param sender the sender who executed this
     * @param config the configuration file used
     */
    public BanConfig(@NotNull final BanItem pl, @NotNull final CommandSender sender, @NotNull final FileConfiguration config) {
        this.config = config;
        final BanUtils utils = pl.getUtils();

        // Loading variables from config
        betterDebug = config.getBoolean("better-debug");
        prefix = config.getString("prefix");
        if (prefix == null) prefix = ""; else prefix = utils.color(prefix);
        noPermission = config.getString("no-permission");
        if (noPermission == null) noPermission = ""; else noPermission = utils.color(noPermission);
        useEventApi = config.getBoolean("use-event-api");
        checkUpdate = config.getBoolean("check-update");
        optionsConfig = new BanConfigOptions(config.getConfigurationSection("options"));
        hooks = new BanConfigHooks(config.getConfigurationSection("hooks"));
        animation = new BanAnimation(utils, sender, this);

        if (config.contains("priority")) {
            List<String> priorityNames = utils.getStringList(config.get("priority"));
            priorityNames = utils.getSplittedList(priorityNames);
            if (!priorityNames.isEmpty())
                priority.addAll(utils.getList(Listable.Type.OPTION, priorityNames, new Debug(pl, sender, new DebugMessage(null, "config.yml"), new DebugMessage(null, "priority")), null));
        }
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
        config.set("prefix", prefix);
        config.set("options", optionsConfig.serialize());
        config.set("no-permission", noPermission);
        config.set("use-event-api", useEventApi);
        config.set("check-update", checkUpdate);
        config.set("priority", priority.isEmpty() ? null : priority.stream().map(BanOption::getName).collect(Collectors.toList()));
        config.set("hooks", hooks.serialize());

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
     * Get the plugin prefix
     * @return the plugin prefix
     */
    @NotNull
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the plugin prefix
     * @param prefix the plugin prefix
     */
    public void setPrefix(@NotNull final String prefix) {
        this.prefix = prefix;
    }

    /**
     * Get the options configurations
     * @return the options configurations
     */
    @NotNull
    public BanConfigOptions getOptionsConfig() {
        return optionsConfig;
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

    /**
     * If the plugin should check for updates
     * @return true if the plugin should check for updates
     */
    public boolean isCheckUpdate() {
        return checkUpdate;
    }

    /**
     * Set if the plugin should check for updates
     * @param checkUpdate check for update
     */
    public void setCheckUpdate(final boolean checkUpdate) {
        this.checkUpdate = checkUpdate;
    }

    /**
     * List of options priority
     * @return list of options priority
     */
    public Set<BanOption> getPriority() {
        return priority;
    }

    /**
     * Get the hooks
     * @return the hooks
     */
    public BanConfigHooks getHooks() {
        return hooks;
    }
}
