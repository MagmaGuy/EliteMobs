package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.config.menus.premade.ProceduralShopMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.ScrapperMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.SmeltMenuConfig;
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

public class SmeltMenu extends EliteMenu {

    private static final List<Integer> inputSlots = SmeltMenuConfig.inputSlots;
    private static final List<Integer> outputSlots = SmeltMenuConfig.outputSlots;
    private static final int informationInputSlot = SmeltMenuConfig.inputInformationSlot;
    private static final int informationOutputSlot = SmeltMenuConfig.outputInformationSlot;
    public static Set<Inventory> inventories = new HashSet<>();

    private static ArrayList<ItemStack>[] calculateOutput(ArrayList<ItemStack> inputItemStacks, Player player) {
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
            int itemUpgradeOrbAmount = (int) Math.floor(scrapAmount / 25);
            int scrapRemainderAmount = scrapAmount - itemUpgradeOrbAmount * 25;

            ItemStack scrapRemainderItem = ItemConstructor.constructScrapItem(
                    scrapLevel, player, false);
            scrapRemainderItem.setAmount(scrapRemainderAmount);
            itemStacks[0].add(scrapRemainderItem);

            ItemStack upgradeItem = ItemConstructor.constructUpgradeItem(scrapLevel, player, false);
            upgradeItem.setAmount(itemUpgradeOrbAmount);
            itemStacks[1].add(upgradeItem);
        }

        return itemStacks;
    }

    /**
     * Creates a menu for scrapping elitemobs items. Only special Elite Mob items can be scrapped here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructSmeltMenu(Player player) {
        String menuName = SmeltMenuConfig.shopName;
        if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes())
            menuName = ChatColor.WHITE + "\uF801\uDB80\uDC4B\uF805         " + menuName;
        Inventory smeltInventory = Bukkit.createInventory(player, 54, menuName);

        for (int i = 0; i < 54; i++) {

            if (i == ScrapperMenuConfig.infoSlot) {
                ItemStack infoButton = SmeltMenuConfig.infoButton;
                if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes()) {
                    infoButton.setType(Material.PAPER);
                    ItemMeta itemMeta = infoButton.getItemMeta();
                    itemMeta.setCustomModelData(MetadataHandler.signatureID);
                    infoButton.setItemMeta(itemMeta);
                }

                smeltInventory.setItem(i, infoButton);
                continue;
            }

            if (i == ScrapperMenuConfig.cancelSlot) {
                smeltInventory.setItem(i, SmeltMenuConfig.cancelButton);
                continue;
            }

            if (i == informationInputSlot) {
                smeltInventory.setItem(i, SmeltMenuConfig.inputInfoButton);
                continue;
            }

            if (i == informationOutputSlot) {
                smeltInventory.setItem(i, SmeltMenuConfig.outputInfoButton);
                continue;
            }


            if (i == ScrapperMenuConfig.confirmSlot) {

                ItemStack clonedConfirmButton = SmeltMenuConfig.confirmButton.clone();

                List<String> lore = new ArrayList<>();
                for (String string : SmeltMenuConfig.confirmButton.getItemMeta().getLore())
                    lore.add(string);
                SmeltMenuConfig.confirmButton.getItemMeta().setLore(lore);
                ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
                clonedMeta.setLore(lore);
                clonedConfirmButton.setItemMeta(clonedMeta);
                smeltInventory.setItem(i, clonedConfirmButton);
                continue;

            }

            if (inputSlots.contains(i) || outputSlots.contains(i))
                continue;

            if (DefaultConfig.isUseGlassToFillMenuEmptySpace())
                smeltInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        player.openInventory(smeltInventory);
        createEliteMenu(smeltInventory, inventories);
    }

    public static class SmeltMenuEvents implements Listener {
        @EventHandler
        public void onInventoryInteract(InventoryClickEvent event) {

            if (!isEliteMenu(event, inventories)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            Inventory smeltInventory = event.getView().getTopInventory();
            Inventory playerInventory = event.getView().getBottomInventory();

            boolean inventoryHasFreeSlots = false;
            for (ItemStack iteratedStack : player.getInventory().getStorageContents())
                if (iteratedStack == null) {
                    inventoryHasFreeSlots = true;
                    break;
                }

            if (isBottomMenu(event)) {
                if (!ItemTagger.hasEnchantment(currentItem.getItemMeta(), "EliteScrap")) return;
                boolean inputIsFull = true;
                for (int slot : inputSlots) {
                    if (smeltInventory.getItem(slot) == null) {
                        smeltInventory.setItem(slot, currentItem);
                        playerInventory.remove(currentItem);
                        inputIsFull = false;
                        break;
                    }
                }

                if (inputIsFull) return;

                ArrayList<ItemStack> inputItemStacks = new ArrayList<>();
                for (int slot : inputSlots)
                    inputItemStacks.add(smeltInventory.getItem(slot));
                ArrayList<ItemStack>[] output = calculateOutput(inputItemStacks, player);
                for (int i = 0; i < outputSlots.size(); i++)
                    if (output[1].size() > i)
                        smeltInventory.setItem(outputSlots.get(i), output[1].get(i));
                    else
                        smeltInventory.setItem(outputSlots.get(i), null);

            } else if (isTopMenu(event)) {

                if (!inventoryHasFreeSlots) {
                    player.sendMessage(ProceduralShopMenuConfig.messageFullInventory);
                    player.closeInventory();
                }

                if (event.getSlot() == SmeltMenuConfig.cancelSlot) {
                    player.closeInventory();
                    return;
                }

                if (event.getSlot() == SmeltMenuConfig.confirmSlot) {
                    ArrayList<ItemStack> inputItemStacks = new ArrayList<>();
                    for (int slot : inputSlots)
                        inputItemStacks.add(smeltInventory.getItem(slot));

                    ArrayList<ItemStack>[] output = calculateOutput(inputItemStacks, player);

                    for (ItemStack itemStack : output[0])
                        player.getInventory().addItem(itemStack);
                    for (int slot : inputSlots)
                        smeltInventory.setItem(slot, null);

                    for (ItemStack itemStack : output[1])
                        player.getInventory().addItem(itemStack);
                    for (int slot : outputSlots)
                        smeltInventory.setItem(slot, null);

                    return;
                }

                if (currentItem == null) return;
                if (currentItem.getType().equals(Material.AIR)) return;
                if (currentItem.getAmount() == 0) return;
                if (inputSlots.contains(event.getSlot())) {
                    playerInventory.addItem(currentItem);
                    smeltInventory.remove(currentItem);
                    ArrayList<ItemStack> inputItemStacks = new ArrayList<>();
                    for (int slot : inputSlots)
                        inputItemStacks.add(smeltInventory.getItem(slot));
                    ArrayList<ItemStack>[] output = calculateOutput(inputItemStacks, player);
                    for (int i = 0; i < outputSlots.size(); i++)
                        if (output[1].size() > i)
                            smeltInventory.setItem(outputSlots.get(i), output[1].get(i));
                        else
                            smeltInventory.setItem(outputSlots.get(i), null);
                }
            }

        }

        @EventHandler
        public void onInventoryCloseEvent(InventoryCloseEvent event) {
            if (inventories.contains(event.getInventory())) {
                inventories.remove(event.getInventory());
                EliteMenu.cancel(event.getPlayer(), event.getView().getTopInventory(), event.getView().getBottomInventory(), SmeltMenu.inputSlots);
            }
        }
    }

}
