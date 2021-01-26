package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.menus.premade.GetLootMenuConfig;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Created by MagmaGuy on 04/05/2017.
 */
public class GetLootMenu extends EliteMenu implements Listener {

    public int currentHeaderPage = 1;
    public int currentLootPage = 1;
    public boolean filter = false;
    public int filterRank = 0;
    public Inventory inventory;

    private static final List<Integer> lootSlots = new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31,
            32, 33, 34, 37, 38, 39, 40, 41, 42, 43, 46, 47, 48, 49, 50, 51, 52));

    public static HashMap<Player, GetLootMenu> inventories = new HashMap<>();

    private final String shopName = GetLootMenuConfig.menuName;

    public GetLootMenu(Player player) {

        Inventory fakeChestInventory = Bukkit.createInventory(null, 54, shopName);
        tierConstructor(fakeChestInventory);
        headerConstructor(fakeChestInventory);
        lootNavigationConstructor(fakeChestInventory);
        lootConstructor(fakeChestInventory);

        player.openInventory(fakeChestInventory);
        inventory = fakeChestInventory;

        inventories.put(player, this);
    }

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

        player.openInventory(fakeChestInventory);
        inventory = fakeChestInventory;

        inventories.put(player, this);
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

        int counter = 1;
        for (int number : tierSlots) {
            if (keySet.size() >= counter + ((currentHeaderPage - 1) * 6)) {
                ItemStack chest = new ItemStack(Material.CHEST, 1);
                ItemMeta chestItemMeta = chest.getItemMeta();
                chestItemMeta.setDisplayName(GetLootMenuConfig.tierTranslation + " " + keySet.get((counter - 1) + ((currentHeaderPage - 1) * 6)));
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

        List<ItemStack> getLootList = new ArrayList<>();
        for (List<ItemStack> list : CustomItem.getTieredLoot().values())
            getLootList.addAll(list);

        int counter = 1;
        for (int number : lootSlots) {
            if (!filter) {
                if (getLootList.size() >= counter + ((currentLootPage - 1) * 35))
                    inventory.setItem(number, getLootList.get(counter - 1 + ((currentLootPage - 1) * 35)));
            } else {
                List<ItemStack> currentRankLoot = CustomItem.getTieredLoot().get(filterRank);
                if (currentRankLoot.size() >= counter + ((currentLootPage - 1) * 35))
                    inventory.setItem(number, currentRankLoot.get(counter - 1 + ((currentLootPage - 1) * 35)));
            }
            counter++;
        }
    }

    public static class GetLootMenuListener implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent event) {

            GetLootMenu getLootMenu = null;
            for (GetLootMenu iteratedMenu : inventories.values())
                if (iteratedMenu != null && iteratedMenu.inventory.equals(event.getInventory())) {
                    getLootMenu = iteratedMenu;
                    break;
                }
            if (getLootMenu == null) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();

            if (!isTopMenu(event)) return;
            //CASE: If the player clicked something in the actual getloot menu

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
                if (getLootMenu.currentHeaderPage - 1 > 1) {
                    getLootMenu.currentHeaderPage--;
                    new GetLootMenu(player, getLootMenu);
                }
                return;
            }

            if (event.getSlot() == 8) {
                getLootMenu.currentHeaderPage++;
                new GetLootMenu(player, getLootMenu);
                return;
            }

            if (event.getSlot() == 27) {
                getLootMenu.currentLootPage--;
                new GetLootMenu(player, getLootMenu);
                return;
            }

            if (event.getSlot() == 35) {
                getLootMenu.currentLootPage++;
                new GetLootMenu(player, getLootMenu);
                return;
            }

            if (event.getSlot() == 1 ||
                    event.getSlot() == 2 ||
                    event.getSlot() == 3 ||
                    event.getSlot() == 5 ||
                    event.getSlot() == 6 ||
                    event.getSlot() == 7) {
                getLootMenu.filter = true;
                getLootMenu.filterRank = Integer.parseInt(currentItem.getItemMeta().getDisplayName().split("\\s")[1]);
                getLootMenu.currentLootPage = 1;
                new GetLootMenu(player, getLootMenu);
                return;
            }
        }

    }

}
