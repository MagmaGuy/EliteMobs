package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.menus.premade.RefinerMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.RefinerMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.SellMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.RefinerMenuConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RefinerMenu extends EliteMenu {

    private static final List<Integer> inputSlots = RefinerMenuConfig.inputSlots;

    public static HashMap<Player, Inventory> inventories = new HashMap<>();

    /**
     * Creates a menu for scrapping elitemobs items. Only special Elite Mob items can be scrapped here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructRefinerMenu(Player player) {

        Inventory refinerInventory = Bukkit.createInventory(player, 54, RefinerMenuConfig.shopName);

        for (int i = 0; i < 54; i++) {

            if (i == RefinerMenuConfig.infoSlot) {
                refinerInventory.setItem(i, RefinerMenuConfig.infoButton);
                continue;
            }

            if (i == RefinerMenuConfig.cancelSlot) {
                refinerInventory.setItem(i, RefinerMenuConfig.cancelButton);
                continue;
            }

            if (i == RefinerMenuConfig.inputInformationSlot) {
                refinerInventory.setItem(i, RefinerMenuConfig.inputInfoButton);
                continue;
            }

            if (i == RefinerMenuConfig.outputInformationSlot) {
                refinerInventory.setItem(i, RefinerMenuConfig.outputInfoButton);
                continue;
            }


            if (i == RefinerMenuConfig.confirmSlot) {

                ItemStack clonedConfirmButton = RefinerMenuConfig.confirmButton.clone();

                List<String> lore = new ArrayList<>();
                for (String string : RefinerMenuConfig.confirmButton.getItemMeta().getLore())
                    lore.add(string);
                RefinerMenuConfig.confirmButton.getItemMeta().setLore(lore);
                ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
                clonedMeta.setLore(lore);
                clonedConfirmButton.setItemMeta(clonedMeta);
                refinerInventory.setItem(i, clonedConfirmButton);
                continue;

            }

            if (inputSlots.contains(i) || RefinerMenuConfig.outputSlots.contains(i))
                continue;

            refinerInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        player.openInventory(refinerInventory);
        createEliteMenu(player, refinerInventory, inventories);
    }


    public static class RefinerMenuEvents implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            if (!isEliteMenu(event, inventories)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            Inventory shopInventory = event.getView().getTopInventory();
            Inventory playerInventory = event.getView().getBottomInventory();

            if (EliteMenu.isBottomMenu(event)) {
                //CASE: If the player clicked on something in their inventory to put it on the shop

                if (!ItemTagger.hasEnchantment(currentItem.getItemMeta(), "EliteScrap")) return;

                boolean isFull = true;

                //If the shop is full, don't let the player put stuff in it
                for (int i : RefinerMenuConfig.inputSlots)
                    if (shopInventory.getItem(i) == null)
                        isFull = false;

                if (isFull) return;

                //Do transfer
                for (int slot : RefinerMenu.inputSlots)
                    if (shopInventory.getItem(slot) == null){
                        shopInventory.setItem(slot, currentItem);
                        playerInventory.clear(event.getSlot());
                        break;
                    }

                refreshOutputVisuals(shopInventory, RefinerMenuConfig.outputSlots, player);

            } else if (EliteMenu.isTopMenu(event)) {
                //CASE: Player clicked on the shop

                //Signature item, does nothing
                if (currentItem.equals(SellMenuConfig.infoButton))
                    return;

                //upgrade items in shop
                if (event.getSlot() == SellMenuConfig.confirmSlot) {

                    for (int i = 0; i < 2; i++)
                        for (ItemStack itemStack : calculateOutput(shopInventory, player)[i])
                            player.getInventory().addItem(itemStack);

                    for (int slot : RefinerMenuConfig.inputSlots)
                        shopInventory.setItem(slot, null);
                    for (int slot : RefinerMenuConfig.outputSlots)
                        shopInventory.setItem(slot, null);

                    return;
                }

                //cancel, transfer items back to player inv and exit
                if (event.getSlot() == SellMenuConfig.cancelSlot) {
                    player.closeInventory();
                    return;
                }

                //If player clicks on a border glass pane, do nothing
                if (!inputSlots.contains(event.getSlot())) return;

                //If player clicks on one of the items already in the shop, return to their inventory
                playerInventory.addItem(event.getCurrentItem());
                shopInventory.clear(event.getSlot());
                refreshOutputVisuals(shopInventory, RefinerMenuConfig.outputSlots, player);

            }

        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            if (!EliteMenu.onInventoryClose(event, inventories)) return;
            EliteMenu.cancel(event.getView().getTopInventory(), event.getView().getBottomInventory(), inputSlots);
        }

    }

    private static ArrayList<ItemStack>[] calculateOutput(Inventory inventory, Player player) {
        ArrayList<ItemStack> inputItemStacks = new ArrayList<>();
        for (int slot : inputSlots)
            inputItemStacks.add(inventory.getItem(slot));
        // itemStack[0] = inputArray , itemStack[1] = outputArray
        ArrayList<ItemStack>[] itemStacks = new ArrayList[2];
        itemStacks[0] = new ArrayList<>();
        itemStacks[1] = new ArrayList<>();
        HashMap<Integer, Integer> inputScrapLevelAndAmountHashMap = new HashMap<>();

        for (ItemStack itemStack : inputItemStacks)
            if (itemStack != null) {
                int scrapLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), "EliteScrap");
                if (scrapLevel >= 0) {
                    if (inputScrapLevelAndAmountHashMap.containsKey(scrapLevel))
                        inputScrapLevelAndAmountHashMap.put(scrapLevel, inputScrapLevelAndAmountHashMap.get(scrapLevel) + itemStack.getAmount());
                    else
                        inputScrapLevelAndAmountHashMap.put(scrapLevel, itemStack.getAmount());
                } else
                    itemStacks[0].add(itemStack);
            }

        for (Integer scrapLevel : inputScrapLevelAndAmountHashMap.keySet()) {
            int scrapAmount = inputScrapLevelAndAmountHashMap.get(scrapLevel);
            int newScrapAmount = (int) Math.floor(scrapAmount / 10);
            int scrapRemainderAmount = scrapAmount - newScrapAmount * 10;

            ItemStack scrapRemainderItem = ItemConstructor.constructScrapItem(
                    scrapLevel, player, false);
            scrapRemainderItem.setAmount(scrapRemainderAmount);
            itemStacks[0].add(scrapRemainderItem);

            ItemStack upgradedScrapItem = ItemConstructor.constructScrapItem(
                    scrapLevel + 1, player, false);
            upgradedScrapItem.setAmount(newScrapAmount);
            itemStacks[1].add(upgradedScrapItem);
        }

        return itemStacks;
    }

    private static void refreshOutputVisuals(Inventory inventory, List<Integer> outputSlots, Player player) {
        ArrayList<ItemStack>[] output = calculateOutput(inventory, player);
        for (int i = 0; i < outputSlots.size(); i++)
            if (output[1].size() > i)
                inventory.setItem(outputSlots.get(i), output[1].get(i));
            else
                inventory.setItem(outputSlots.get(i), null);
    }

}
