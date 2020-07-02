package fr.andross.banitem.utils;

import fr.andross.banitem.BanUtils;
import fr.andross.banitem.config.BanConfig;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Animation class
 * @version 2.3
 * @author Andross
 */
public final class BanAnimation {
    // Sound
    private Sound sound = null;
    private int volume = 3;
    private int pitch = 3;
    private boolean worldSound = false;

    // Particle
    private Particle particle = null;
    private int amount = 10;

    /**
     * Empty animation
     */
    public BanAnimation() { }

    /**
     * Loading ban animation.
     * This should not be used externally.
     * Use {@link fr.andross.banitem.BanItemAPI#load(CommandSender, FileConfiguration)} instead.
     * @param utils ban utils
     * @param sender the sender of this request
     * @param banConfig the ban config instance
     */
    public BanAnimation(@NotNull final BanUtils utils, @NotNull final CommandSender sender, @NotNull final BanConfig banConfig) {
        final FileConfiguration config = banConfig.getConfig();

        // Sound
        final boolean soundEnabled = config.getBoolean("sound.enabled");
        if (soundEnabled) {
            try {
                sound = Sound.valueOf(config.getString("sound.type"));
                volume = config.getInt("sound.volume");
                pitch = config.getInt("sound.pitch");
                worldSound = config.getBoolean("sound.worldSound");
            } catch (final Exception e) {
                sender.sendMessage(banConfig.getPrefix() + utils.color("&cInvalid sound parameters set in config.yml."));
            }
        }
        // Particle
        final boolean particleEnabled = config.getBoolean("particle.enabled");
        if (particleEnabled && BanVersion.v9OrMore) {
            try {
                particle = Particle.valueOf(config.getString("particle.type"));
                amount = config.getInt("particle.amount");
            } catch (final Exception e) {
                sender.sendMessage(banConfig.getPrefix() + utils.color("&cInvalid particle parameters set in config.yml."));
            }
        }
    }

    /**
     * Running animation, if available
     * @param p the player
     */
    public void runAnimation(@NotNull final Player p) {
        // Sound
        if (sound != null) {
            if (worldSound) p.getWorld().playSound(p.getLocation(), sound, volume, pitch);
            else p.playSound(p.getLocation(), sound, volume, pitch);
        }
        // Particle
        if (particle != null) p.spawnParticle(particle, p.getLocation().add(0, 2.5, 0), amount);
    }

    /**
     * Serializing this ban animation object
     * @return a serialized map of this ban animation
     */
    @NotNull
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new LinkedHashMap<>();

        // Sound
        final Map<String, Object> soundSection = new LinkedHashMap<>();
        soundSection.put("enabled", sound != null);
        soundSection.put("type", sound == null ? "Unknown" : sound.name());
        soundSection.put("volume", volume);
        soundSection.put("pitch", pitch);
        soundSection.put("worldSound", worldSound);
        map.put("sound", soundSection);

        // Particle
        final Map<String, Object> particleSection = new LinkedHashMap<>();
        particleSection.put("enabled", particle != null);
        particleSection.put("type", particle == null ? "Unknown" : particle.name());
        particleSection.put("amount", amount);
        map.put("particle", particleSection);

        return map;
    }

}
