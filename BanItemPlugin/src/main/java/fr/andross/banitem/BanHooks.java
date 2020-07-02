package fr.andross.banitem;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import fr.andross.banitem.config.BanConfigHooks;
import fr.andross.banitem.utils.hooks.IWorldGuardHook;
import fr.andross.banitem.utils.hooks.WorldGuard6Hook;
import fr.andross.banitem.utils.hooks.WorldGuard7Hook;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Hooks manager
 * @version 2.3
 * @author Andross
 */
public final class BanHooks {
    private boolean isWorldGuardEnabled = false;
    private IWorldGuardHook worldGuardHook = null;

    BanHooks(@NotNull final BanItem pl, @NotNull final CommandSender sender) {
        // Checking config
        final BanConfigHooks hooks = pl.getBanConfig().getHooks();

        // WorldGuard?
        if (hooks.isWorldGuard())
            try {
                final WorldGuardPlugin worldGuardPlugin = WorldGuardPlugin.inst();
                if (worldGuardPlugin == null || !worldGuardPlugin.isEnabled()) throw new Exception();
                final String version = worldGuardPlugin.getDescription().getVersion();
                if (version.startsWith("7")) worldGuardHook = new WorldGuard7Hook();
                else if (version.startsWith("6")) worldGuardHook = new WorldGuard6Hook();
                else throw new Exception();
                isWorldGuardEnabled = true;
            } catch (final Error | Exception e) {
                pl.getUtils().sendMessage(sender, "&c[Hooks] Can not hook with WorldGuard.");
                isWorldGuardEnabled = false;
            }
    }

    /**
     * Check if the plugin is successfully hooked with WorldGuard
     * @return true if the plugin is successfully hooked with WorldGuard
     */
    public boolean isWorldGuardEnabled() {
        return isWorldGuardEnabled;
    }


    /**
     * Get the worldguard hook
     * @return the worldguard hook
     */
    @Nullable
    public IWorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }
}
