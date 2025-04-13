/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2021 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your action) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.andross.banitem.utils.scanners;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.BanUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Async scanner to check if players wears a banned item.
 * If a banned item is worn, a next sync task will be run to handle it.
 *
 * @author Andross
 * @version 3.1.1
 */
public final class WearScanner {
    private final BanItem plugin;
    private final BanUtils utils;
    private boolean enabled;
    private int taskId = -1;

    /**
     * Prepare wear scanner.
     *
     * @param plugin The ban item plugin instance
     * @param utils The ban item plugin utility class
     */
    public WearScanner(@NotNull final BanItem plugin, @NotNull final BanUtils utils) {
        this.plugin = plugin;
        this.utils = utils;
    }

    /**
     * Check if the wear scanner is enabled.
     *
     * @return true if the wear scanner is enabled, otherwise false.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set if the wear scanner is enabled.
     *
     * @param enabled If the wear scanner should be enabled
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            if (taskId < 0) {
                taskId = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () ->
                                plugin.getServer().getOnlinePlayers().forEach(utils::checkPlayerArmors),
                        16L, 16L).getTaskId();
            }
        } else {
            if (taskId > -1) {
                plugin.getServer().getScheduler().cancelTask(taskId);
                taskId = -1;
            }
        }
    }
}
