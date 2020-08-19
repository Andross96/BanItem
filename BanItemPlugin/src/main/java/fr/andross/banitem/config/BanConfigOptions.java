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
package fr.andross.banitem.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class offers a way to store and get the ban options configuration
 * @version 2.4
 * @author Andross
 */
public final class BanConfigOptions {
    // Rename
    private final List<String> renameCommands = new ArrayList<>();

    // Transfer
    private boolean hoppersBlock = false;

    // Wear
    private boolean regionCheck = false;

    BanConfigOptions(@Nullable final ConfigurationSection section) {
        if (section == null) return;

        // Rename
        if (section.contains("rename")) renameCommands.addAll(section.getStringList("rename").stream().map(String::toLowerCase).collect(Collectors.toList()));

        // Transfer
        if (section.contains("transfer")) hoppersBlock = section.getBoolean("transfer.hoppers-block");

        // Wear
        if (section.contains("wear")) regionCheck = section.getBoolean("wear.region-check");
    }

    /**
     * Get the list of rename commands
     * @return list of rename commands
     */
    @NotNull
    public List<String> getRenameCommands() {
        return renameCommands;
    }

    /**
     * Check if the hoppers should be blocked
     * @return if the hoppers are blocked
     */
    public boolean isHoppersBlock() {
        return hoppersBlock;
    }

    /**
     * Set if the hoppers should be blocked
     * @param hoppersBlock if the hoppers are blocked
     */
    public void setHoppersBlock(final boolean hoppersBlock) {
        this.hoppersBlock = hoppersBlock;
    }

    /**
     * Check if the plugin should check when a player enter or exit a worldguard region
     * @return if the regions are checked
     */
    public boolean isRegionCheck() {
        return regionCheck;
    }

    /**
     * Set if the plugin should check when a player enter or exit a worldugard region
     * @param regionCheck if the regions are checked
     */
    public void setRegionCheck(final boolean regionCheck) {
        this.regionCheck = regionCheck;
    }

    /**
     * Serializing the options
     * @return a serialized map of the options
     */
    @NotNull
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new LinkedHashMap<>();

        // Rename
        map.put("rename", renameCommands);

        // Transfer
        final Map<String, Object> transferSection = new LinkedHashMap<>();
        transferSection.put("hoppers-block", hoppersBlock);
        map.put("transfer", transferSection);

        // Wear
        final Map<String, Object> wearSection = new LinkedHashMap<>();
        wearSection.put("region-check", regionCheck);
        map.put("wear", wearSection);

        return map;
    }

}
