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
package fr.andross.banitem.utils;

import fr.andross.banitem.BanConfig;
import fr.andross.banitem.utils.statics.BanVersion;
import fr.andross.banitem.utils.statics.Chat;
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
 * @version 3.0
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
     * Loading a ban animation.
     * @param sender the sender using this
     * @param banConfig the ban configuration used
     */
    public BanAnimation(@NotNull final CommandSender sender, @NotNull final BanConfig banConfig) {
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
                sender.sendMessage(banConfig.getPrefix() + Chat.color("&cInvalid sound parameters set in " + banConfig.getConfigName() + "."));
            }
        }
        // Particle
        final boolean particleEnabled = config.getBoolean("particle.enabled");
        if (particleEnabled && BanVersion.v9OrMore) {
            try {
                particle = Particle.valueOf(config.getString("particle.type"));
                amount = config.getInt("particle.amount");
            } catch (final Exception e) {
                sender.sendMessage(banConfig.getPrefix() + Chat.color("&cInvalid particle parameters set in " + banConfig.getConfigName() + "."));
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
