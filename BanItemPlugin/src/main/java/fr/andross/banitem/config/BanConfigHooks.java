/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2020 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.andross.banitem.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class offers a way to store and get the hooks options
 * @version 2.4
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
