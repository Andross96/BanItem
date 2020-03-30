package fr.andross.banitem.Utils;

import fr.andross.banitem.BanItem;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BanAnimation {
    // Sound
    private Sound sound;
    private int volume = 3;
    private int pitch = 3;
    private boolean worldSound = false;

    // Particle
    private Particle particle;
    private int amount = 10;

    public BanAnimation(@NotNull final CommandSender sender) {
        final BanItem pl = BanItem.getInstance();
        // Sound
        final boolean soundEnabled = pl.getConfig().getBoolean("sound.enabled");
        if (soundEnabled) {
            try {
                sound = Sound.valueOf(pl.getConfig().getString("sound.type"));
                volume = pl.getConfig().getInt("sound.volume");
                pitch = pl.getConfig().getInt("sound.pitch");
                worldSound = pl.getConfig().getBoolean("sound.worldSound");
            } catch (final Exception e) {
                sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &cInvalid sound parameters set in config.yml."));
            }
        }
        // Particle
        final boolean particleEnabled = pl.getConfig().getBoolean("particle.enabled");
        if (particleEnabled && pl.isv9OrMore()) {
            try {
                particle = Particle.valueOf(pl.getConfig().getString("particle.type"));
                amount = pl.getConfig().getInt("particle.amount");
            } catch (final Exception e) {
                sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &cInvalid particle parameters set in config.yml."));
            }
        }
    }

    public void runAnimation(@NotNull final Player p) {
        // Sound
        if (sound != null) {
            if (worldSound) p.getWorld().playSound(p.getLocation(), sound, volume, pitch);
            else p.playSound(p.getLocation(), sound, volume, pitch);
        }
        // Particle
        if (particle != null) p.spawnParticle(particle, p.getLocation().add(0, 2.5, 0), amount);
    }

}
