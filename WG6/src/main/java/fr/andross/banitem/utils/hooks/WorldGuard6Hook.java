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

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WorldGuard6Hook implements IWorldGuardHook {

    @Nullable
    @Override
    public ProtectedRegion getProtectedRegion(@NotNull final World world,
                                              @NotNull final String regionName) {
        final RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        final RegionManager regions = container.get(world);
        return regions == null ? null : regions.getRegion(regionName);
    }

    @NotNull
    @Override
    public List<ProtectedRegion> getAllProtectedRegions(@NotNull final World world) {
        final RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        final RegionManager regions = container.get(world);
        final List<ProtectedRegion> protectedRegions = new ArrayList<>();
        if (regions == null) {
            return protectedRegions;
        }
        protectedRegions.addAll(regions.getRegions().values());
        return protectedRegions;
    }

    @NotNull
    @Override
    public Set<ProtectedRegion> getStandingRegions(@NotNull final Location loc) {
        final RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        final RegionManager regions = container.get(loc.getWorld());
        final Set<ProtectedRegion> protectedRegions = new HashSet<>();
        if (regions == null) {
            return protectedRegions;
        }
        final ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(loc);
        return applicableRegionSet == null ||
                applicableRegionSet.size() == 0 ? protectedRegions : applicableRegionSet.getRegions();
    }
}

