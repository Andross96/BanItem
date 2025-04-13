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

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A chat utility class.
 *
 * @author Andross
 * @version 3.2
 */
public abstract class Chat {
    /**
     * Represents the color character.
     */
    private static final char COLOR_CHAR = '\u00A7';

    /**
     * Represents the regex which match a color code.
     */
    private static final Pattern stripColorPattern = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-ORX]");

    /**
     * Represents the regex which match a HEX color.
     */
    private static final Pattern hexPattern = Pattern.compile("&#[A-Fa-f0-9]{6}");

    /**
     * Represents the regex which match a converted HEX color.
     */
    private static final Pattern hexStripPattern = Pattern.compile("(?i)" + COLOR_CHAR + "x(" + COLOR_CHAR + "[0-9A-FK-OR]){6}");

    /**
     * Static utility class.
     */
    private Chat() {}

    /**
     * Translate the color codes to make the string colored.
     *
     * @param text the text to translate
     * @return a colored string
     */
    @NotNull
    public static String color(@Nullable String text) {
        if (text == null) return "";

        String newText = text;
        if (MinecraftVersion.v16OrMore) {
            final Matcher matcher = hexPattern.matcher(text);
            while (matcher.find()) {
                String color = text.substring(matcher.start(), matcher.end()); // &#1258DA
                if (color.startsWith("&")) {
                    newText = newText.replace(color, ChatColor.of(color.substring(1)).toString());
                }
            }
        }

        char[] b = newText.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    /**
     * Remove the color codes on a string.
     *
     * @param text colored string
     * @return uncolored string
     */
    @NotNull
    public static String stripColors(@NotNull String text) {
        return stripColorPattern.matcher(text).replaceAll("");
    }

    /**
     * Revert the {@link #color(String)} method.
     *
     * @param text text
     * @return the uncolored text, with color codes
     */
    @Nullable
    @Contract("!null -> !null; null -> null")
    public static String revertColor(@Nullable String text) {
        if (text == null) {
            return null;
        }

        if (MinecraftVersion.v16OrMore) {
            final Matcher matcher = hexStripPattern.matcher(text);
            while (matcher.find()) {
                final String color = text.substring(matcher.start(), matcher.end()); // ex: §x§1§2§5§8§D§A
                text = text.replace(color, "&#" + color.substring(2).replace(String.valueOf(COLOR_CHAR), ""));
            }
        }

        char[] b = text.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == COLOR_CHAR && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = '&';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }
}
