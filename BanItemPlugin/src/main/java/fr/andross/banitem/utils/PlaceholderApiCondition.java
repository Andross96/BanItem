package fr.andross.banitem.utils;

import org.bukkit.entity.Player;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A PlaceholderAPI condition handler class, linked to {@link fr.andross.banitem.actions.BanActionData},
 * in order to apply a ban on an item only if the Placeholder API value match a configured value.
 * Only available if hook with PlaceholderAPI plugin is enabled.
 */
public class PlaceholderApiCondition {

    private final String placeholder;
    private final Pattern pattern;
    private final String expectedValue;

    /**
     * Prepare a PlaceholderAPI condition check.
     * The configuration must have the syntax:
     * <li><code>%placeholder_value%=expectedValue</code> - for exact string matching</li>
     * <li><code>%placeholder_value%=#expectedRegex</code> - for regex matching</li>
     *
     * @param placeholderConditionConfiguration a placeholder API condition handler
     */
    public PlaceholderApiCondition(final String placeholderConditionConfiguration) throws IllegalArgumentException {
        if (!placeholderConditionConfiguration.contains("=")) {
            throw new IllegalArgumentException("invalid configuration syntax - must contains an '=' " +
                    "with format \"%placeholder_value%=expectedValue\"");
        }

        final String[] split = placeholderConditionConfiguration.split("=");
        this.placeholder = split[0];

        if (split[1].startsWith("#")) { // Using regex to match value
            try {
                this.pattern = Pattern.compile(split[1].substring(1));
            } catch (final PatternSyntaxException e) {
                throw new IllegalArgumentException("invalid regex pattern entered : " + e.getMessage());
            }
            this.expectedValue = null;
        } else { // simple string matching
            this.pattern = null;
            this.expectedValue = split[1];
        }
    }

    public boolean doesConditionMatch(final Player player) {
        final String placeholderValue = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, placeholder);
        if (pattern != null) {
            return pattern.matcher(placeholderValue).find();
        } else {
            return expectedValue.equals(placeholderValue);
        }
    }

}
