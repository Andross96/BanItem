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
package fr.andross.banitem.utils.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WorldGuard7Hook implements IWorldGuardHook {

    @Nullable
    @Override
    public ProtectedRegion getProtectedRegion(@NotNull final World world,
                                              @NotNull final String regionName) {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regions = container.get(BukkitAdapter.adapt(world));
        return regions == null ? null : regions.getRegion(regionName);
    }

    @NotNull
    @Override
    public List<ProtectedRegion> getAllProtectedRegions(@NotNull final World world) {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        final List<ProtectedRegion> protectedRegions = new ArrayList<>();
        if (regionManager == null) {
            return protectedRegions;
        }
        protectedRegions.addAll(regionManager.getRegions().values());
        return protectedRegions;
    }

    @NotNull
    @Override
    public Set<ProtectedRegion> getStandingRegions(@NotNull final Location loc) {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final Set<ProtectedRegion> protectedRegions = new HashSet<>();
        final World world = loc.getWorld();
        if (world == null) {
            return protectedRegions;
        }
        final RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if (regions == null) {
            return protectedRegions;
        }
        final ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        return applicableRegionSet == null ||
                applicableRegionSet.size() == 0 ? protectedRegions : applicableRegionSet.getRegions();
    }

}
