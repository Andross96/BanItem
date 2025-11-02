/*
 * MGT-BanItem - NeoForge port of BanItem plugin
 * Copyright (C) 2024 GnomoMuitoLoco
 */
package fr.andross.banitem;

import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.items.BannedItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.EntityItemPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Main event listener for MGT-BanItem.
 * Handles Forge events and checks against blacklist/whitelist.
 */
@EventBusSubscriber(modid = ModMain.MODID)
public class BanListener {
    private final ModMain plugin;
    private int activated = 0;

    public BanListener(@NotNull final ModMain plugin) {
        this.plugin = plugin;
    }

    /**
     * Load/reload listeners based on current configuration.
     */
    public void load() {
        final BanDatabase db = plugin.getBanDatabase();
        final Set<BanAction> blacklist = db.getBlacklist().getActions();
        activated = 0;

        ModMain.LOGGER.info("Loaded {} active listeners", activated);
    }

    // ===== Event Handlers =====

    /**
     * Handle block break events (BREAK action)
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        final ModMain plugin = ModMain.getInstance();
        if (plugin == null || plugin.getBanDatabase() == null) {
            return;
        }

        final ItemStack itemInHand = player.getMainHandItem();
        if (BanUtils.isNullOrAir(itemInHand)) {
            return;
        }

        final BannedItem bannedItem = new BannedItem(itemInHand);
        // TODO: Check if item is banned for BREAK action
        // if (isBanned(player, bannedItem, BanAction.BREAK)) {
        //     event.setCanceled(true);
        // }
    }

    /**
     * Handle item drop events (DROP action)
     */
    @SubscribeEvent
    public static void onItemDrop(ItemTossEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        final ModMain plugin = ModMain.getInstance();
        if (plugin == null || plugin.getBanDatabase() == null) {
            return;
        }

        final ItemStack droppedItem = event.getEntity().getItem();
        if (BanUtils.isNullOrAir(droppedItem)) {
            return;
        }

        final BannedItem bannedItem = new BannedItem(droppedItem);
        // TODO: Check if item is banned for DROP action
        // if (isBanned(player, bannedItem, BanAction.DROP)) {
        //     event.setCanceled(true);
        // }
    }

    /**
     * Handle item pickup events (PICKUP action)
     */
    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        final ModMain plugin = ModMain.getInstance();
        if (plugin == null || plugin.getBanDatabase() == null) {
            return;
        }

        final ItemStack pickedItem = event.getItem().getItem();
        if (BanUtils.isNullOrAir(pickedItem)) {
            return;
        }

        final BannedItem bannedItem = new BannedItem(pickedItem);
        // TODO: Check if item is banned for PICKUP action
        // if (isBanned(player, bannedItem, BanAction.PICKUP)) {
        //     event.setCanceled(true);
        // }
    }

    /**
     * Handle right-click/use events (USE action)
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        final ModMain plugin = ModMain.getInstance();
        if (plugin == null || plugin.getBanDatabase() == null) {
            return;
        }

        final ItemStack usedItem = event.getItemStack();
        if (BanUtils.isNullOrAir(usedItem)) {
            return;
        }

        final BannedItem bannedItem = new BannedItem(usedItem);
        // TODO: Check if item is banned for USE action
        // if (isBanned(player, bannedItem, BanAction.USE)) {
        //     event.setCanceled(true);
        // }
    }

    /**
     * Handle block place events (PLACE action)
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        final ModMain plugin = ModMain.getInstance();
        if (plugin == null || plugin.getBanDatabase() == null) {
            return;
        }

        final ItemStack placedItem = player.getMainHandItem();
        if (BanUtils.isNullOrAir(placedItem)) {
            return;
        }

        final BannedItem bannedItem = new BannedItem(placedItem);
        // TODO: Check if item is banned for PLACE action
        // if (isBanned(player, bannedItem, BanAction.PLACE)) {
        //     event.setCanceled(true);
        // }
    }

    /**
     * Handle attack events (ATTACK action)
     */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        final ModMain plugin = ModMain.getInstance();
        if (plugin == null || plugin.getBanDatabase() == null) {
            return;
        }

        final ItemStack weaponItem = player.getMainHandItem();
        if (BanUtils.isNullOrAir(weaponItem)) {
            return;
        }

        final BannedItem bannedItem = new BannedItem(weaponItem);
        // TODO: Check if item is banned for ATTACK action
        // if (isBanned(player, bannedItem, BanAction.ATTACK)) {
        //     event.setCanceled(true);
        // }
    }

    /**
     * Handle inventory open/close for DELETE action
     */
    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        final ModMain plugin = ModMain.getInstance();
        if (plugin == null || plugin.getBanDatabase() == null) {
            return;
        }

        // TODO: Check and delete banned items from player inventory
        // deleteItemFromInventory(player);
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        final ModMain plugin = ModMain.getInstance();
        if (plugin == null || plugin.getBanDatabase() == null) {
            return;
        }

        // TODO: Check and delete banned items from player inventory
        // deleteItemFromInventory(player);
    }

    /**
     * Get the number of activated listeners.
     */
    public int getActivated() {
        return activated;
    }
}
