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
    public ProtectedRegion getProtectedRegion(@NotNull final World world, @NotNull final String regionName) {
        final RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        final RegionManager regions = container.get(world);
        return regions == null ? null : regions.getRegion(regionName);
    }

    @NotNull
    @Override
    public List<ProtectedRegion> getAllProtectedRegions(@NotNull final World world) {
        final RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        final RegionManager regions = container.get(world);
        final List<ProtectedRegion> list = new ArrayList<>();
        if (regions == null) return list;
        list.addAll(regions.getRegions().values());
        return list;
    }

    @NotNull
    @Override
    public Set<ProtectedRegion> getStandingRegions(@NotNull final Location loc) {
        final RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        final RegionManager regions = container.get(loc.getWorld());
        final Set<ProtectedRegion> set = new HashSet<>();
        if (regions == null) return set;
        final ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(loc);
        return applicableRegionSet == null || applicableRegionSet.size() == 0 ? set : applicableRegionSet.getRegions();
    }
}

