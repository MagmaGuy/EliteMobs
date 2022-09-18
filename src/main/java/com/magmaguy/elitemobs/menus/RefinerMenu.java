package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.config.menus.premade.ProceduralShopMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.RefinerMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.SellMenuConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class RefinerMenu extends EliteMenu {

    protected static final Set<Inventory> inventories = new HashSet<>();
    private static final List<Integer> inputSlots = RefinerMenuConfig.getInputSlots();

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
            int newScrapAmount = (int) (scrapAmount / 10d);
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

    /**
     * Creates a menu for scrapping elitemobs items. Only special Elite Mob items can be scrapped here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructRefinerMenu(Player player) {
        String menuName = RefinerMenuConfig.getShopName();
        if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes())
            menuName = ChatColor.WHITE + "\uF801\uDB80\uDC3B\uF805         " + menuName;
        Inventory refinerInventory = Bukkit.createInventory(player, 54, menuName);

        for (int i = 0; i < 54; i++) {

            if (i == RefinerMenuConfig.getInfoSlot()) {
                ItemStack infoButton = RefinerMenuConfig.getInfoButton();
                if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes()) {
                    infoButton.setType(Material.PAPER);
                    ItemMeta itemMeta = infoButton.getItemMeta();
                    itemMeta.setCustomModelData(MetadataHandler.signatureID);
                    infoButton.setItemMeta(itemMeta);
                }
                refinerInventory.setItem(i, infoButton);
                continue;
            }

            if (i == RefinerMenuConfig.getCancelSlot()) {
                refinerInventory.setItem(i, RefinerMenuConfig.getCancelButton());
                continue;
            }

            if (i == RefinerMenuConfig.getInputInformationSlot()) {
                refinerInventory.setItem(i, RefinerMenuConfig.getInputInfoButton());
                continue;
            }

            if (i == RefinerMenuConfig.getOutputInformationSlot()) {
                refinerInventory.setItem(i, RefinerMenuConfig.getOutputInfoButton());
                continue;
            }


            if (i == RefinerMenuConfig.getConfirmSlot()) {

                ItemStack clonedConfirmButton = RefinerMenuConfig.getConfirmButton().clone();

                List<String> lore = new ArrayList<>();
                for (String string : RefinerMenuConfig.getConfirmButton().getItemMeta().getLore())
                    lore.add(string);
                RefinerMenuConfig.getConfirmButton().getItemMeta().setLore(lore);
                ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
                clonedMeta.setLore(lore);
                clonedConfirmButton.setItemMeta(clonedMeta);
                refinerInventory.setItem(i, clonedConfirmButton);
                continue;

            }

            if (inputSlots.contains(i) || RefinerMenuConfig.getOutputSlots().contains(i))
                continue;

            if (DefaultConfig.isUseGlassToFillMenuEmptySpace())
                refinerInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        player.openInventory(refinerInventory);
        createEliteMenu(refinerInventory, inventories);
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

            boolean inventoryHasFreeSlots = false;
            for (ItemStack iteratedStack : player.getInventory().getStorageContents())
                if (iteratedStack == null) {
                    inventoryHasFreeSlots = true;
                    break;
                }

            if (EliteMenu.isBottomMenu(event)) {
                //CASE: If the player clicked on something in their inventory to put it on the shop

                if (!ItemTagger.hasEnchantment(currentItem.getItemMeta(), "EliteScrap")) return;

                boolean isFull = true;

                //If the shop is full, don't let the player put stuff in it
                for (int i : RefinerMenuConfig.getInputSlots())
                    if (shopInventory.getItem(i) == null)
                        isFull = false;

                if (isFull) return;

                //Do transfer
                for (int slot : RefinerMenu.inputSlots)
                    if (shopInventory.getItem(slot) == null) {
                        shopInventory.setItem(slot, currentItem);
                        playerInventory.clear(event.getSlot());
                        break;
                    }

                refreshOutputVisuals(shopInventory, RefinerMenuConfig.getOutputSlots(), player);

            } else if (EliteMenu.isTopMenu(event)) {
                if (!inventoryHasFreeSlots) {
                    player.sendMessage(ProceduralShopMenuConfig.messageFullInventory);
                    player.closeInventory();
                }

                //CASE: Player clicked on the shop

                //Signature item, does nothing
                if (currentItem.equals(SellMenuConfig.infoButton))
                    return;

                //upgrade items in shop
                if (event.getSlot() == SellMenuConfig.confirmSlot) {

                    for (int i = 0; i < 2; i++)
                        for (ItemStack itemStack : calculateOutput(shopInventory, player)[i])
                            player.getInventory().addItem(itemStack);

                    for (int slot : RefinerMenuConfig.getInputSlots())
                        shopInventory.setItem(slot, null);
                    for (int slot : RefinerMenuConfig.getOutputSlots())
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
                refreshOutputVisuals(shopInventory, RefinerMenuConfig.getOutputSlots(), player);

            }

        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (inventories.contains(event.getInventory())) {
                inventories.remove(event.getInventory());
                EliteMenu.cancel(event.getPlayer(), event.getView().getTopInventory(), event.getView().getBottomInventory(), inputSlots);
            }
        }

    }

}
