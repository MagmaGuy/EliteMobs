package com.magmaguy.elitemobs.commands;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by MagmaGuy on 04/05/2017.
 */
public class getLootMenu implements Listener {

    private int currentHeaderPage = 1;
    private int currentLootPage = 1;
    private boolean filter = false;
    private int filterRank = 0;

    private List<Integer> lootSlots = new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31,
            32, 33, 34, 37, 38, 39, 40, 41, 42, 43, 46, 47, 48, 49, 50, 51, 52));

    private String shopName = GetLootMenuConfig.menuName;

    public void lootGUI(Player player) {

        Inventory fakeChestInventory = Bukkit.createInventory(null, 54, shopName);
        tierConstructor(fakeChestInventory);
        headerConstructor(fakeChestInventory);
        lootNavigationConstructor(fakeChestInventory);
        lootConstructor(fakeChestInventory);

        player.openInventory(fakeChestInventory);

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

                if (getLootList.size() >= counter + ((currentLootPage - 1) * 35)) {

                    inventory.setItem(number, getLootList.get(counter - 1 + ((currentLootPage - 1) * 35)));

                }

            } else {

                List<ItemStack> currentRankLoot = CustomItem.getTieredLoot().get(filterRank);

                if (currentRankLoot.size() >= counter + ((currentLootPage - 1) * 35)) {

                    inventory.setItem(number, currentRankLoot.get(counter - 1 + ((currentLootPage - 1) * 35)));

                }

            }

            counter++;

        }

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (event.getView().getTitle().isEmpty()) return;

        if (event.getView().getTitle().equalsIgnoreCase(shopName)) {

            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {

                event.setCancelled(true);
                return;

            }

            String itemDisplayName = event.getCurrentItem().getItemMeta().getDisplayName();
            Player player = (Player) event.getWhoClicked();

            if (itemDisplayName != null) {

                if (itemDisplayName.equals(GetLootMenuConfig.nextLootItem)) {

                    currentLootPage++;
                    lootGUI(player);

                } else if (itemDisplayName.equals(GetLootMenuConfig.previousLootItem)) {

                    if (currentLootPage > 1) {

                        currentLootPage--;
                        lootGUI(player);

                    }

                } else if (itemDisplayName.equals(GetLootMenuConfig.rightArrowItem)) {

                    currentHeaderPage++;
                    lootGUI(player);

                } else if (itemDisplayName.equals(GetLootMenuConfig.leftArrowItem)) {

                    if (currentHeaderPage > 1) {

                        currentHeaderPage--;
                        lootGUI(player);

                    }

                } else if (itemDisplayName.equals(GetLootMenuConfig.infoItem)) {

                    filter = false;
                    currentLootPage = 1;
                    lootGUI(player);

                } else if (event.getCurrentItem().getItemMeta().getLore().get(0).equals(GetLootMenuConfig.itemFilterTranslation)) {

                    filter = true;
                    String[] lore = itemDisplayName.split("\\s");
                    filterRank = Integer.valueOf(lore[1]);
                    currentLootPage = 1;
                    lootGUI(player);

                }

            }

            if (lootSlots.contains(event.getSlot()))
                player.getInventory().addItem(event.getCurrentItem());

            event.setCancelled(true);

        }

    }

}
