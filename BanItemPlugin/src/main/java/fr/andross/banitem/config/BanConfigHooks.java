package fr.andross.banitem.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class offers a way to store and get the hooks options
 * @version 2.3
 * @author Andross
 */
public final class BanConfigHooks {
    private boolean worldGuard = false;

    BanConfigHooks(@Nullable final ConfigurationSection section) {
        if (section == null) return;
        worldGuard = section.getBoolean("worldguard");
    }

    /**
     * Check if the WorldGuard hook is activated
     * @return true if the worldguard hook is activated
     */
    public boolean isWorldGuard() {
        return worldGuard;
    }

    /**
     * Set if the plugin should hook with worldguard
     * @param worldGuard if the plugin should hook with worldguard
     */
    public void setWorldGuard(final boolean worldGuard) {
        this.worldGuard = worldGuard;
    }

    /**
     * Serializing the hooks
     * @return a serialized map of the hooks
     */
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("worldguard", worldGuard);
        return map;
    }
}
