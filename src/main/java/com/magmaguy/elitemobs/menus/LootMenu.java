package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.menus.premade.LootMenuConfig;
import com.magmaguy.elitemobs.items.customloottable.SharedLootTable;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import lombok.Getter;
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
import java.util.UUID;

public class LootMenu extends EliteMenu {
    @Getter
    private static final HashMap<UUID, LootMenu> playerLoot = new HashMap<>();
    private static final List<Integer> glassSlots = List.of(4, 13, 22, 31, 40, 49);
    private static final int greedInfo = 2;
    private static final int needInfo = 6;
    private static final List<Integer> greedSlots = List.of(9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48);
    private static final List<Integer> needSlots = List.of(14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53);
    private final SharedLootTable sharedLootTable;
    private final SharedLootTable.PlayerTable playerTable;
    @Getter
    private final Inventory inventory;
    private final Player player;

    public LootMenu(Player player, SharedLootTable sharedLootTable, SharedLootTable.PlayerTable playerTable) {
        this.inventory = Bukkit.createInventory(player, 54);
        this.sharedLootTable = sharedLootTable;
        this.playerTable = playerTable;
        this.player = player;
        for (int i : glassSlots) inventory.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        //Info slots
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
        playerLoot.put(player.getUniqueId(), this);
    }

    public static void shutdown() {
        playerLoot.clear();
    }

    public static void openMenu(Player player) {
        LootMenu lootMenu = playerLoot.get(player.getUniqueId());
        if (lootMenu == null) {
            player.sendMessage(LootMenuConfig.getNoGroupLootMessage());
            return;
        }
        player.openInventory(lootMenu.renderMenu());
    }

    private Inventory renderMenu() {
        greedSlots.forEach(slot -> inventory.setItem(slot, null));
        needSlots.forEach(slot -> inventory.setItem(slot, null));
        int greedIndex = 0;
        List<ItemStack> greedItems = new ArrayList<>(sharedLootTable.getLoot());
        greedItems.removeAll(playerTable.getNeedItems());
        for (ItemStack greedItem : greedItems) {
            if (greedIndex >= greedSlots.size()) break;
            inventory.setItem(greedSlots.get(greedIndex), greedItem);
            greedIndex++;
        }
        int needIndex = 0;
        for (ItemStack needItem : playerTable.getNeedItems()) {
            if (needIndex >= needSlots.size()) break;
            inventory.setItem(needSlots.get(needIndex), needItem);
            needIndex++;
        }
        return inventory;
    }

    public void removeMenu() {
        LootMenu lootMenu = playerLoot.get(player.getUniqueId());
        if (lootMenu == this) {
            if (player.isOnline() && player.getOpenInventory().getTopInventory().equals(lootMenu.getInventory()))
                player.closeInventory();
            playerLoot.remove(player.getUniqueId());
        }
    }


    public static class LootMenuEvents implements Listener {
        @EventHandler
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            LootMenu lootMenu = playerLoot.get(player.getUniqueId());
            if (lootMenu == null) return;
            if (!lootMenu.inventory.equals(event.getInventory())) return;
            event.setCancelled(true);
            if (isBottomMenu(event)) return;
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
            lootMenu.renderMenu();
            int slot = event.getSlot();
            //Greed slots
            if (greedSlots.contains(slot)) {
                lootMenu.playerTable.addNeed(event.getCurrentItem());
                openMenu(player);
            }
            //Need slots
            else if (needSlots.contains(slot)) {
                lootMenu.playerTable.removeNeed(event.getCurrentItem());
                openMenu(player);
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            playerLoot.remove(event.getPlayer().getUniqueId());
        }
    }
}
