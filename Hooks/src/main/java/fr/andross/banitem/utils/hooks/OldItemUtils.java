package fr.andross.banitem.utils.hooks;

import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public final class OldItemUtils {

    public static boolean isUnbreakable(@NotNull final ItemMeta itemMeta) {
        return itemMeta.spigot().isUnbreakable();
    }

}
