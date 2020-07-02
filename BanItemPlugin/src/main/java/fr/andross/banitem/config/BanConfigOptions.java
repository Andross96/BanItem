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
 * @version 2.3
 * @author Andross
 */
public final class BanConfigOptions {
    // Rename
    private final List<String> renameCommands = new ArrayList<>();

    // Transfer
    private boolean hoppersBlock = false;

    BanConfigOptions(@Nullable final ConfigurationSection section) {
        if (section == null) return;

        // Rename
        if (section.contains("rename")) renameCommands.addAll(section.getStringList("rename").stream().map(String::toLowerCase).collect(Collectors.toList()));

        // Transfer
        if (section.contains("transfer")) hoppersBlock = section.getBoolean("transfer.hoppers-block");
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

        return map;
    }

}
