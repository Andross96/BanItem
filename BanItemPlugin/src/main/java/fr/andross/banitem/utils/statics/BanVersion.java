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
package fr.andross.banitem.utils.statics;

import org.bukkit.Bukkit;

/**
 * Class that contains some notable versions nodes, so the plugin can handle multiple versions
 * Those variables are used to check versions compatibility
 * @version 3.0.1
 * @author Andross
 */
public final class BanVersion {
    /**
     * in 1.16+, color codes now support HEX.
     */
    public static final boolean v16OrMore = Bukkit.getBukkitVersion().matches("(1\\.16)(.*)");

    /**
     * In 1.14+, CustomModelData has been added.
     */
    public static final boolean v14OrMore = Bukkit.getBukkitVersion().matches("(1\\.14)(.*)|(1\\.15)(.*)") || v16OrMore;

    /**
     * In 1.13+, MaterialData are totally removed from ItemStack, and the durability is part of ItemMeta.
     * Added NamespacedKey enchantments
     */
    public static final boolean v13OrMore = Bukkit.getBukkitVersion().matches("(1\\.13)(.*)") || v14OrMore;

    /**
     * In 1.12+, the PlayerPickupItemEvent is now deprecated, and should use the EntityPickupItemEvent.
     */
    public static final boolean v12OrMore = Bukkit.getBukkitVersion().matches("(1\\.12)(.*)") || v13OrMore;

    /**
     * In 1.11+, ItemMeta#(set|is)Unbreakable has been added.
     */
    public static final boolean v11OrMore = Bukkit.getBukkitVersion().matches("(1\\.11)(.*)") || v12OrMore;

    /**
     * In 1.9+, the off hand have been added. Also added PotionMeta#getBasePotionData(). Also added PrepareAnvilEvent.
     */
    public static final boolean v9OrMore = Bukkit.getBukkitVersion().matches("(1\\.9)(.*)|(1\\.10)(.*)") || v11OrMore;

    /**
     * In 1.8+, armor stand event have been added.
     */
    public static final boolean v8OrMore = Bukkit.getBukkitVersion().matches("(1\\.8)(.*)") || v9OrMore;
}
