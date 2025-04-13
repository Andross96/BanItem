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

import org.bukkit.Bukkit;

/**
 * Class that contains some notable versions, so the plugin can handle multiple versions.
 * Those variables are used to check versions compatibility.
 *
 * @author Andross
 * @version 3.4
 */
public final class MinecraftVersion {

    /**
     * In 1.21+, Crafter item has been added.
     */
    public static final boolean v21OrMore;

    /**
     * In 1.16+, color codes now support HEX.
     */
    public static final boolean v16OrMore;

    /**
     * In 1.14+, CustomModelData has been added.
     */
    public static final boolean v14OrMore;

    /**
     * In 1.13+, MaterialData are totally removed from ItemStack, and the durability is part of ItemMeta.
     * Added NamespacedKey enchantments.
     * Added PlayerItemMendEvent event.
     */
    public static final boolean v13OrMore;

    /**
     * In 1.12+, the PlayerPickupItemEvent is now deprecated, and should use the EntityPickupItemEvent.
     */
    public static final boolean v12OrMore;

    /**
     * In 1.11+, ItemMeta#(set|is)Unbreakable has been added.
     */
    public static final boolean v11OrMore;

    /**
     * In 1.9+, the offhand have been added. Also added PotionMeta#getBasePotionData(). Also added PrepareAnvilEvent.
     */
    public static final boolean v9OrMore;

    /**
     * In 1.8+, armor stand event have been added.
     */
    public static final boolean v8OrMore;

    static {
        final String bukkitVersion = Bukkit.getBukkitVersion();
        int subMcVersion;
        try {
            final String mcVersion = bukkitVersion.split("-")[0]; // 1.8.8
            subMcVersion = Integer.parseInt(mcVersion.split("\\.")[1]); // 8
        } catch (final Exception e) {
            subMcVersion = 1;
            Bukkit.getLogger().warning("[VERSION] Unrecognized/Unsupported Bukkit version!");
        }
        v21OrMore = subMcVersion >= 21;
        v16OrMore = subMcVersion >= 16;
        v14OrMore = subMcVersion >= 14;
        v13OrMore = subMcVersion >= 13;
        v12OrMore = subMcVersion >= 12;
        v11OrMore = subMcVersion >= 11;
        v9OrMore = subMcVersion >= 9;
        v8OrMore = subMcVersion >= 8;
    }
}
