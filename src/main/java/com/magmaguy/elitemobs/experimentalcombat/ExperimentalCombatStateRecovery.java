package com.magmaguy.elitemobs.experimentalcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.combattag.DungeonCombatRuntime;
import com.magmaguy.elitemobs.config.ExperimentalCombatConfig;
import com.magmaguy.elitemobs.experimentalcombat.passives.ClassPassiveRuntime;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Removes retired control items and persistent state left behind by interrupted combat sessions.
 *
 * <p>Normal exits are restored from the live runtime baseline. This listener is intentionally
 * registered even while [Alpha] Advanced Combat System is disabled, because a disabled build must still be
 * able to recover a player saved by an earlier enabled build.</p>
 */
public final class ExperimentalCombatStateRecovery implements Listener {
    private static final NamespacedKey RETIRED_FOCUS_KEY =
            NamespacedKey.fromString("elitemobs:experimental_combat_focus");

    public static void reconcile(Player player) {
        removeRetiredControls(player);
        if (ExperimentalCombatConfig.isEnabled()
                && !DungeonCombatRuntime.isInManagedCombatWorld(player))
            ArmorSkillHealthBonus.applyHealthBonus(player);
        if (ExperimentalCombatRuntime.isActive(player)) return;
        if (ExperimentalCombatConfig.isEnabled() && DungeonCombatRuntime.isEligiblePlayer(player)) return;
        clearOwnedState(player);
    }

    /** Clears reload-preserved state if initialization fails before a replacement runtime starts. */
    public static void clearAllOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) clearOwnedState(player);
    }

    private static void clearOwnedState(Player player) {
        removeRetiredControls(player);
        ExperimentalFoodItems.restorePlayerInventory(player);
        ExperimentalCombatRuntime.clearPersistedState(player);
        ClassPassiveRuntime.clearPersistedState(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            if (player.isOnline()) reconcile(player);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        // Also recover stale item/attribute state when the runtime is disabled.
        reconcile(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        refreshAfterInventoryMutation(player, event.getView().getTopInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        refreshAfterInventoryMutation(player, event.getView().getTopInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        refreshAfterInventoryMutation(player, event.getView().getTopInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        org.bukkit.inventory.ItemStack stack = event.getItem();
        if (ExperimentalFoodItems.restoreItem(stack)) event.setItem(stack);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (isRetiredControl(event.getItemDrop().getItemStack())) {
            event.getItemDrop().remove();
            return;
        }
        ExperimentalFoodItems.restoreDroppedItem(event.getItemDrop());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (isRetiredControl(event.getEntity().getItemStack())) {
            event.getEntity().remove();
            return;
        }
        ExperimentalFoodItems.restoreDroppedItem(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> refreshPlayerInventory(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(ExperimentalCombatStateRecovery::isRetiredControl);
        for (org.bukkit.inventory.ItemStack drop : event.getDrops())
            ExperimentalFoodItems.restoreItem(drop);
    }

    private static void refreshAfterInventoryMutation(Player player, org.bukkit.inventory.Inventory topInventory) {
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            removeRetiredControls(topInventory);
            ExperimentalFoodItems.restoreInventory(topInventory);
            ExperimentalFoodItems.restoreItem(player.getItemOnCursor());
            refreshPlayerInventory(player);
        });
    }

    private static void refreshPlayerInventory(Player player) {
        if (!player.isOnline()) return;
        removeRetiredControls(player);
        if (ExperimentalCombatRuntime.isActive(player))
            ExperimentalFoodItems.preparePlayerInventory(player);
        else
            ExperimentalFoodItems.restorePlayerInventory(player);
    }

    // Migration only: retired synthetic controls must never turn into usable vanilla nether stars.
    private static boolean isRetiredControl(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(
                RETIRED_FOCUS_KEY, PersistentDataType.BYTE);
    }

    private static void removeRetiredControls(Inventory inventory) {
        for (int slot = 0; slot < inventory.getSize(); slot++)
            if (isRetiredControl(inventory.getItem(slot))) inventory.setItem(slot, null);
    }

    private static void removeRetiredControls(Player player) {
        removeRetiredControls(player.getInventory());
        removeRetiredControls(player.getEnderChest());
        if (isRetiredControl(player.getItemOnCursor())) player.setItemOnCursor(null);
    }
}
