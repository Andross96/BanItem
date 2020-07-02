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
    public ProtectedRegion getProtectedRegion(@NotNull final World world, @NotNull final String regionName) {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regions = container.get(BukkitAdapter.adapt(world));
        return regions == null ? null : regions.getRegion(regionName);
    }

    @NotNull
    @Override
    public List<ProtectedRegion> getAllProtectedRegions(@NotNull final World world) {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regions = container.get(BukkitAdapter.adapt(world));
        final List<ProtectedRegion> list = new ArrayList<>();
        if (regions == null) return list;
        list.addAll(regions.getRegions().values());
        return list;
    }

    @NotNull
    @Override
    public Set<ProtectedRegion> getStandingRegions(@NotNull final Location loc) {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final Set<ProtectedRegion> set = new HashSet<>();
        final World world = loc.getWorld();
        if (world == null) return set;
        final RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if (regions == null) return set;
        final ApplicableRegionSet applicableRegionSet = regions.getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        return applicableRegionSet == null || applicableRegionSet.size() == 0 ? set : applicableRegionSet.getRegions();
    }

}
