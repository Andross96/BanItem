package fr.andross.banitem.utils.hooks;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface IWorldGuardHook {

    @Nullable
    ProtectedRegion getProtectedRegion(@NotNull final World world, @NotNull final String regionName);

    @NotNull
    List<ProtectedRegion> getAllProtectedRegions(@NotNull final World world);

    @NotNull
    Set<ProtectedRegion> getStandingRegions(@NotNull final Location loc);

}