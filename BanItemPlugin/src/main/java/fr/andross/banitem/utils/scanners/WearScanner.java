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
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * A simple async scanner to check if players wears a banned item
 * @version 3.1
 * @author Andross
 */
public final class WearScanner {
    private final BanItem pl;
    private final BanUtils utils;
    private boolean enabled;
    private int taskId = -1;

    public WearScanner(@NotNull final BanItem pl, @NotNull final BanUtils utils) {
        this.pl = pl;
        this.utils = utils;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            if (taskId < 0)
                taskId = pl.getServer().getScheduler().runTaskTimerAsynchronously(pl, this::run, 20L, 20L).getTaskId();
        } else {
            if (taskId > -1) {
                pl.getServer().getScheduler().cancelTask(taskId);
                taskId = -1;
            }
        }
    }

    // Async
    private void run() {
        Bukkit.getOnlinePlayers().forEach(utils::checkPlayerArmors);
    }

}
