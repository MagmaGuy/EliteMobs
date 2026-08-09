package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.menus.premade.LootMenuConfig;
import com.magmaguy.elitemobs.items.customloottable.SharedLootTable;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Need/greed menu with exact roll identities, pagination, and support for overlapping active loot sessions. */
public class LootMenu extends EliteMenu {
    private static final Map<UUID, List<LootMenu>> playerLoot = new HashMap<>();
    private static final Map<UUID, Integer> nextSessionIndex = new HashMap<>();
    private static final List<Integer> glassSlots = List.of(4, 13, 22, 31, 40, 49);
    private static final int previousPageSlot = 0;
    private static final int greedInfo = 2;
    private static final int pageIndicatorSlot = 4;
    private static final int needInfo = 6;
    private static final int nextPageSlot = 8;
    private static final List<Integer> greedSlots = List.of(9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48);
    private static final List<Integer> needSlots = List.of(14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53);
    private static final int entriesPerPage = 20;

    private final SharedLootTable sharedLootTable;
    private final SharedLootTable.PlayerTable playerTable;
    private final Inventory inventory;
    private final Player player;
    private final Map<Integer, SharedLootTable.LootRollEntry> displayedEntries = new HashMap<>();
    private int page;

    public LootMenu(Player player, SharedLootTable sharedLootTable, SharedLootTable.PlayerTable playerTable) {
        this.inventory = Bukkit.createInventory(player, 54);
        this.sharedLootTable = sharedLootTable;
        this.playerTable = playerTable;
        this.player = player;
        for (int i : glassSlots) inventory.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        inventory.setItem(greedInfo,
                ItemStackGenerator.generateItemStack(
                        Material.ORANGE_STAINED_GLASS_PANE,
                        LootMenuConfig.getGreedListTitle(),
                        List.of(LootMenuConfig.getGreedListLore1(),
                                LootMenuConfig.getGreedListLore2(),
                                LootMenuConfig.getGreedListLore3())));
        inventory.setItem(needInfo,
                ItemStackGenerator.generateItemStack(
                        Material.GREEN_STAINED_GLASS_PANE,
                        LootMenuConfig.getNeedListTitle(),
                        List.of(LootMenuConfig.getNeedListLore1(),
                                LootMenuConfig.getNeedListLore2(),
                                LootMenuConfig.getNeedListLore3())));
        List<LootMenu> sessions = playerLoot.computeIfAbsent(player.getUniqueId(), ignored -> new ArrayList<>());
        sessions.add(this);
        nextSessionIndex.put(player.getUniqueId(), sessions.size() - 1);
    }

    public static void shutdown() {
        playerLoot.clear();
        nextSessionIndex.clear();
    }

    /** Backward-compatible snapshot exposing the most recent active loot menu per player. */
    public static HashMap<UUID, LootMenu> getPlayerLoot() {
        HashMap<UUID, LootMenu> latest = new HashMap<>();
        playerLoot.forEach((playerId, sessions) -> {
            if (!sessions.isEmpty()) latest.put(playerId, sessions.get(sessions.size() - 1));
        });
        return latest;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public static void openMenu(Player player) {
        List<LootMenu> sessions = playerLoot.get(player.getUniqueId());
        if (sessions == null || sessions.isEmpty()) {
            player.sendMessage(LootMenuConfig.getNoGroupLootMessage());
            return;
        }
        int index = Math.min(nextSessionIndex.getOrDefault(player.getUniqueId(), sessions.size() - 1), sessions.size() - 1);
        LootMenu lootMenu = sessions.get(index);
        nextSessionIndex.put(player.getUniqueId(), index == 0 ? sessions.size() - 1 : index - 1);
        lootMenu.open();
    }

    private void open() {
        player.openInventory(renderMenu());
    }

    private Inventory renderMenu() {
        greedSlots.forEach(slot -> inventory.setItem(slot, null));
        needSlots.forEach(slot -> inventory.setItem(slot, null));
        displayedEntries.clear();

        List<SharedLootTable.LootRollEntry> entries = sharedLootTable.getLoot(player);
        int totalPages = Math.max(1, (entries.size() + entriesPerPage - 1) / entriesPerPage);
        page = Math.max(0, Math.min(page, totalPages - 1));
        int fromIndex = page * entriesPerPage;
        int toIndex = Math.min(entries.size(), fromIndex + entriesPerPage);

        int greedIndex = 0;
        int needIndex = 0;
        for (SharedLootTable.LootRollEntry entry : entries.subList(fromIndex, toIndex)) {
            int slot;
            if (playerTable.needs(entry.id())) slot = needSlots.get(needIndex++);
            else slot = greedSlots.get(greedIndex++);
            inventory.setItem(slot, entry.itemStack());
            displayedEntries.put(slot, entry);
        }

        inventory.setItem(pageIndicatorSlot, ItemStackGenerator.generateItemStack(
                Material.PAPER,
                LootMenuConfig.getPageIndicatorTitle()
                        .replace("$current", String.valueOf(page + 1))
                        .replace("$total", String.valueOf(totalPages))));
        inventory.setItem(previousPageSlot, page > 0
                ? ItemStackGenerator.generateItemStack(Material.ARROW, LootMenuConfig.getPreviousPageTitle())
                : null);
        inventory.setItem(nextPageSlot, page + 1 < totalPages
                ? ItemStackGenerator.generateItemStack(Material.ARROW, LootMenuConfig.getNextPageTitle())
                : null);
        return inventory;
    }

    public void removeMenu() {
        List<LootMenu> sessions = playerLoot.get(player.getUniqueId());
        if (sessions == null) return;
        if (player.isOnline() && player.getOpenInventory().getTopInventory().equals(inventory)) player.closeInventory();
        sessions.remove(this);
        if (sessions.isEmpty()) {
            playerLoot.remove(player.getUniqueId());
            nextSessionIndex.remove(player.getUniqueId());
        } else {
            nextSessionIndex.put(player.getUniqueId(), sessions.size() - 1);
        }
    }

    private static LootMenu findOpenMenu(Player player, Inventory inventory) {
        List<LootMenu> sessions = playerLoot.get(player.getUniqueId());
        if (sessions == null) return null;
        for (LootMenu session : sessions)
            if (session.inventory.equals(inventory)) return session;
        return null;
    }

    public static class LootMenuEvents implements Listener {
        @EventHandler
        public void onInventoryInteract(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            LootMenu lootMenu = findOpenMenu(player, event.getInventory());
            if (lootMenu == null) return;
            event.setCancelled(true);
            if (isBottomMenu(event)) return;

            int slot = event.getSlot();
            if (slot == previousPageSlot && lootMenu.page > 0) {
                lootMenu.page--;
                lootMenu.open();
                return;
            }
            int totalEntries = lootMenu.sharedLootTable.getLoot(player).size();
            if (slot == nextPageSlot && (lootMenu.page + 1) * entriesPerPage < totalEntries) {
                lootMenu.page++;
                lootMenu.open();
                return;
            }

            SharedLootTable.LootRollEntry entry = lootMenu.displayedEntries.get(slot);
            if (entry == null) return;
            if (greedSlots.contains(slot)) lootMenu.playerTable.setNeed(entry, true);
            else if (needSlots.contains(slot)) lootMenu.playerTable.setNeed(entry, false);
            lootMenu.open();
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            SharedLootTable.onPlayerQuit(event.getPlayer().getUniqueId());
            playerLoot.remove(event.getPlayer().getUniqueId());
            nextSessionIndex.remove(event.getPlayer().getUniqueId());
        }
    }
}
