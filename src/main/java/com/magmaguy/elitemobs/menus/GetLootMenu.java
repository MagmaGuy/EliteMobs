package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.menus.premade.GetLootMenuConfig;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Created by MagmaGuy on 04/05/2017.
 */
public class GetLootMenu extends EliteMenu implements Listener {

    private static final List<Integer> lootSlots = new ArrayList<>(new ArrayList<>(List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31,
            32, 33, 34, 37, 38, 39, 40, 41, 42, 43, 46, 47, 48, 49, 50, 51, 52)));
    public static HashMap<UUID, GetLootMenu> inventories = new HashMap<>();

    public GetLootMenu(Player player) {

        Inventory fakeChestInventory = Bukkit.createInventory(null, 54, shopName);
        tierConstructor(fakeChestInventory);
        headerConstructor(fakeChestInventory);
        lootNavigationConstructor(fakeChestInventory);
        lootConstructor(fakeChestInventory);

        open(player, fakeChestInventory);
    }
    private final String shopName = GetLootMenuConfig.menuName;
    public int currentHeaderPage = 1;
    public int currentLootPage = 1;
    public boolean filter = false;
    public int filterRank = 0;
    public Inventory inventory;
    private final Map<Integer, Integer> headerTiers = new HashMap<>();
    private boolean hasNextHeaderPage;
    private boolean hasNextLootPage;

    public GetLootMenu(Player player, GetLootMenu getLootMenu) {
        this.currentHeaderPage = getLootMenu.currentHeaderPage;
        this.currentLootPage = getLootMenu.currentLootPage;
        this.filter = getLootMenu.filter;
        this.filterRank = getLootMenu.filterRank;
        Inventory fakeChestInventory = Bukkit.createInventory(null, 54, shopName);
        tierConstructor(fakeChestInventory);
        headerConstructor(fakeChestInventory);
        lootNavigationConstructor(fakeChestInventory);
        lootConstructor(fakeChestInventory);

        open(player, fakeChestInventory);
    }

    private void open(Player player, Inventory inventory) {
        this.inventory = inventory;
        player.openInventory(inventory);
        if (player.getOpenInventory().getTopInventory() == inventory)
            inventories.put(player.getUniqueId(), this);
    }

    public static void shutdown() {
        for (GetLootMenu menu : new ArrayList<>(inventories.values()))
            for (var viewer : new ArrayList<>(menu.inventory.getViewers()))
                viewer.closeInventory();
        inventories.clear();
    }

    private void headerConstructor(Inventory inventory) {
        inventory.setItem(0, GetLootMenuConfig.leftArrowItem);
        inventory.setItem(8, GetLootMenuConfig.rightArrowItem);
        inventory.setItem(4, GetLootMenuConfig.infoItem);
    }

    private void tierConstructor(Inventory inventory) {

        List<Integer> tierSlots = new ArrayList<>();

        tierSlots.add(1);
        tierSlots.add(2);
        tierSlots.add(3);
        tierSlots.add(5);
        tierSlots.add(6);
        tierSlots.add(7);

        List<Integer> keySet = new ArrayList<>(CustomItem.getTieredLoot().keySet());
        Collections.sort(keySet);
        hasNextHeaderPage = keySet.size() > currentHeaderPage * 6;

        int counter = 1;
        for (int number : tierSlots) {
            if (keySet.size() >= counter + ((currentHeaderPage - 1) * 6)) {
                int tier = keySet.get((counter - 1) + ((currentHeaderPage - 1) * 6));
                headerTiers.put(number, tier);
                ItemStack chest = new ItemStack(Material.CHEST, 1);
                ItemMeta chestItemMeta = chest.getItemMeta();
                chestItemMeta.setDisplayName(GetLootMenuConfig.tierTranslation + " " + tier);
                List<String> lore = new ArrayList();
                lore.add(GetLootMenuConfig.itemFilterTranslation);
                chestItemMeta.setLore(lore);
                chest.setItemMeta(chestItemMeta);
                inventory.setItem(number, chest);
            }
            counter++;
        }

    }

    private void lootNavigationConstructor(Inventory inventory) {
        inventory.setItem(27, GetLootMenuConfig.previousLootItem);
        inventory.setItem(35, GetLootMenuConfig.nextLootItem);
    }

    private void lootConstructor(Inventory inventory) {

        List<ItemStack> getLootList;
        if (filter) {
            getLootList = CustomItem.getTieredLoot().get(filterRank);
            if (getLootList == null) getLootList = List.of();
        } else {
            getLootList = new ArrayList<>();
            for (List<ItemStack> list : CustomItem.getTieredLoot().values())
                getLootList.addAll(list);
        }
        hasNextLootPage = getLootList.size() > currentLootPage * lootSlots.size();

        int counter = 1;
        for (int number : lootSlots) {
            if (getLootList.size() >= counter + ((currentLootPage - 1) * lootSlots.size()))
                inventory.setItem(number, getLootList.get(counter - 1 + ((currentLootPage - 1) * lootSlots.size())));
            counter++;
        }
    }

    public static class GetLootMenuListener implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent event) {

            GetLootMenu getLootMenu = inventories.get(event.getWhoClicked().getUniqueId());
            if (getLootMenu == null || !getLootMenu.inventory.equals(event.getInventory())) return;
            if (event.getClickedInventory() == null) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();

            if (!isTopMenu(event)) return;
            //CASE: If the player clicked something in the actual getloot menu

            if (currentItem == null) return;

            //CASE: If it was one of the items that they can get
            if (lootSlots.contains(event.getSlot())) {
                player.getInventory().addItem(currentItem);
                return;
            }

            if (currentItem == null || currentItem.getItemMeta() == null) return;
            //CASE: If it was the "back to home" button
            if (event.getSlot() == 4) {
                new GetLootMenu(player);
                return;
            }

            //CASE: Left header arrow, previous tier
            if (event.getSlot() == 0) {
                if (getLootMenu.currentHeaderPage > 1) {
                    getLootMenu.currentHeaderPage--;
                    new GetLootMenu(player, getLootMenu);
                }
                return;
            }

            if (event.getSlot() == 8) {
                if (!getLootMenu.hasNextHeaderPage) return;
                getLootMenu.currentHeaderPage++;
                new GetLootMenu(player, getLootMenu);
                return;
            }

            if (event.getSlot() == 27) {
                if (getLootMenu.currentLootPage - 1 < 1) return;
                getLootMenu.currentLootPage--;
                new GetLootMenu(player, getLootMenu);
                return;
            }

            if (event.getSlot() == 35) {
                if (!getLootMenu.hasNextLootPage) return;
                getLootMenu.currentLootPage++;
                new GetLootMenu(player, getLootMenu);
                return;
            }

            Integer tier = getLootMenu.headerTiers.get(event.getSlot());
            if (tier != null) {
                getLootMenu.filter = true;
                getLootMenu.filterRank = tier;
                getLootMenu.currentLootPage = 1;
                new GetLootMenu(player, getLootMenu);
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            GetLootMenu menu = inventories.get(event.getPlayer().getUniqueId());
            if (menu != null && menu.inventory.equals(event.getInventory()))
                inventories.remove(event.getPlayer().getUniqueId(), menu);
        }

    }

}
