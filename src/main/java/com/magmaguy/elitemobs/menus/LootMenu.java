package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.items.customloottable.SharedLootTable;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LootMenu extends EliteMenu {
    @Getter
    private static final HashMap<Player, LootMenu> playerLoot = new HashMap<>();
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
                        ChatColorConverter.convert("&2Greed Item List"),
                        List.of("Click to move to the Need item list!",
                                "Items in the greed list will only",
                                "be rolled for if no one needs them!")));
        inventory.setItem(needInfo,
                ItemStackGenerator.generateItemStack(
                        Material.GREEN_STAINED_GLASS_PANE,
                        ChatColorConverter.convert("&2Need Item List"),
                        List.of("Click to move to the Greed item list!",
                                "Items in the need list will only",
                                "be rolled for people who needed them!")));
        playerLoot.put(player, this);
    }

    public static void openMenu(Player player) {
        LootMenu lootMenu = playerLoot.get(player);
        if (lootMenu == null) {
            player.sendMessage(ChatColorConverter.convert("&4[EliteMobs] &6You don't currently have any group loot to vote on!"));
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
        LootMenu lootMenu = playerLoot.get(player);
        if (lootMenu == this) {
            if (player.getOpenInventory().equals(lootMenu.getInventory())) player.closeInventory();
            playerLoot.remove(player);
        }
    }


    public static class LootMenuEvents implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            LootMenu lootMenu = playerLoot.get(player);
            if (lootMenu == null) return;
            if (!lootMenu.inventory.equals(event.getInventory())) return;
            event.setCancelled(true);
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
    }
}
