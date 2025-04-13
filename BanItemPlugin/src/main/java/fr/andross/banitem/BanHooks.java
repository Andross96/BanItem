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
package fr.andross.banitem;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import fr.andross.banitem.utils.hooks.IWorldGuardHook;
import fr.andross.banitem.utils.hooks.WorldGuard6Hook;
import fr.andross.banitem.utils.hooks.WorldGuard7Hook;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Hooks manager.
 *
 * @author Andross
 * @version 3.1
 */
public final class BanHooks {
    private boolean isWorldGuardEnabled = false;
    private IWorldGuardHook worldGuardHook = null;
    private boolean isAdvancedEnchantmentsEnabled = false;
    private boolean isPlaceholderApiEnabled = false;

    /**
     * Loading the hooks.
     * This should not be used externally.
     * Use {@link fr.andross.banitem.BanItemAPI#load(CommandSender, java.io.File)} instead.
     */
    BanHooks(@NotNull final BanItem pl, @NotNull final CommandSender sender) {
        // WorldGuard?
        if (pl.getBanConfig().getConfig().getBoolean("hooks.worldguard"))
            try {
                final WorldGuardPlugin worldGuardPlugin = WorldGuardPlugin.inst();
                if (worldGuardPlugin == null || !worldGuardPlugin.isEnabled()) {
                    throw new Exception("no worldguard plugin found");
                }
                final String version = worldGuardPlugin.getDescription().getVersion();
                if (version.startsWith("7")) {
                    worldGuardHook = new WorldGuard7Hook();
                } else if (version.startsWith("6")) {
                    worldGuardHook = new WorldGuard6Hook();
                } else {
                    throw new Exception("unknown WorldGuard version " + version);
                }
                isWorldGuardEnabled = true;
            } catch (final Throwable e) {
                pl.getUtils().sendMessage(sender, "&c[Hooks] Can not hook with WorldGuard.");
                isWorldGuardEnabled = false;
            }

        // AdvancedEnchantments?
        if (pl.getBanConfig().getConfig().getBoolean("hooks.advancedenchantments")) {
            try {
                net.advancedplugins.ae.api.AEAPI.getAllEnchantments();
                isAdvancedEnchantmentsEnabled = true;
            } catch (final Throwable e) {
                pl.getUtils().sendMessage(sender, "&c[Hooks] Can not hook with AdvancedEnchantments: " + e.getMessage());
                isAdvancedEnchantmentsEnabled = false;
            }
        }

        // PlaceholderAPI?
        if (pl.getBanConfig().getConfig().getBoolean("hooks.placeholderapi")) {
            try {
                PlaceholderAPI.getRegisteredIdentifiers();
                isPlaceholderApiEnabled = true;
            } catch (final Throwable e) {
                pl.getUtils().sendMessage(sender, "&c[Hooks] Can not hook with PlaceholderAPI: " + e.getMessage());
                isPlaceholderApiEnabled = false;
            }
        }
    }

    /**
     * Check if the plugin is successfully hooked with WorldGuard.
     *
     * @return true if the plugin is successfully hooked with WorldGuard
     */
    public boolean isWorldGuardEnabled() {
        return isWorldGuardEnabled;
    }

    /**
     * Get the worldguard hook.
     *
     * @return the worldguard hook if hooked, otherwise null
     */
    @Nullable
    public IWorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }

    /**
     * Check if the plugin is hooked with AdvancedEnchantments.
     *
     * @return true if the plugin is successfully hooked with AdvancedEnchantments
     */
    public boolean isAdvancedEnchantmentsEnabled() {
        return isAdvancedEnchantmentsEnabled;
    }

    /**
     * Check if the plugin is hooked with PlaceholderAPI.
     *
     * @return true if the plugin is successfully hooked with PlaceholderAPI
     */
    public boolean isPlaceholderApiEnabled() {
        return isPlaceholderApiEnabled;
    }
}
