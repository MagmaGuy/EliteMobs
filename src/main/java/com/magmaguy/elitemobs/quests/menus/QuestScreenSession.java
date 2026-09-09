package com.magmaguy.elitemobs.quests.menus;

import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import com.magmaguy.magmacore.dialog.DialogManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Tracks only quest-owned presentations; ordinary inventories are not combat-dismissed. */
public final class QuestScreenSession implements Listener {
    public static final Object DIALOG_OWNER = new Object();
    private static final Set<UUID> books = new HashSet<>();

    public static void openedBook(Player player) { books.add(player.getUniqueId()); }

    public static void shutdown() {
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) forget(player);
        books.clear();
    }

    private static void forget(Player player) {
        books.remove(player.getUniqueId());
        DialogManager.forgetDialogOwner(player, DIALOG_OWNER);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getFinalDamage() <= 0) return;
        if (QuestInventoryMenu.isQuestInventory(player.getOpenInventory().getTopInventory())
                || books.remove(player.getUniqueId())) player.closeInventory();
        DialogManager.clearOwnedDialog(player, DIALOG_OWNER);
        QuestDialogueBossBarManager.close(player, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventory(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) forget(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteraction(PlayerInteractEvent event) { forget(event.getPlayer()); }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) { forget(event.getPlayer()); }

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent event) { forget(event.getPlayer()); }
}
