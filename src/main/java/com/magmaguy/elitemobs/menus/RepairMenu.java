package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.config.menus.premade.ProceduralShopMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.RepairMenuConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RepairMenu extends EliteMenu {
    private static final int eliteItemInputSlot = RepairMenuConfig.eliteItemInputSlot;
    private static final int scrapItemInputSlot = RepairMenuConfig.eliteScrapInputSlot;
    private static final int outputSlot = RepairMenuConfig.outputSlot;
    private static final int eliteItemInformationInputSlot = RepairMenuConfig.eliteItemInputInformationSlot;
    private static final int eliteScrapInformationInputSlot = RepairMenuConfig.eliteScrapInputInformationSlot;
    private static final int informationOutputSlot = RepairMenuConfig.outputInformationSlot;
    public static Set<Inventory> inventories = new HashSet<>();

    private static void calculateOutput(Inventory repairInventory) {
        if (repairInventory.getItem(RepairMenuConfig.eliteScrapInputSlot) == null || repairInventory.getItem(RepairMenuConfig.eliteItemInputSlot) == null) {
            repairInventory.setItem(RepairMenuConfig.outputSlot, null);
            return;
        }

        int scrapLevel = ItemTagger.getEnchantment(repairInventory.getItem(RepairMenuConfig.eliteScrapInputSlot).getItemMeta(), "EliteScrap");
        ItemStack outputItem = repairInventory.getItem(RepairMenuConfig.eliteItemInputSlot).clone();
        int itemLevel = EliteItemManager.getRoundedItemLevel(outputItem);
        int baselineRepair = 300;
        int levelDifferenceModifier = (scrapLevel - itemLevel) * 10;
        int newDamage = baselineRepair + levelDifferenceModifier;
        Damageable damageable = (Damageable) outputItem.getItemMeta();
        int damage = Math.min(damageable.getDamage() - newDamage, damageable.getDamage());
        damageable.setDamage(damage);
        outputItem.setItemMeta(damageable);
        repairInventory.setItem(outputSlot, outputItem);
    }

    /**
     * Creates a menu for scrapping elitemobs items. Only special Elite Mob items can be scrapped here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructRepairMenu(Player player) {
        String menuName = RepairMenuConfig.shopName;
        if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes())
            menuName = ChatColor.WHITE + "\uF801\uDB80\uDC2A\uF805           " + menuName;

        Inventory repairInventory = Bukkit.createInventory(player, 54, menuName);

        for (int i = 0; i < repairInventory.getSize(); i++) {

            if (i == RepairMenuConfig.infoSlot) {
                ItemStack infoButton = RepairMenuConfig.infoButton;
                if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes()) {
                    infoButton.setType(Material.PAPER);
                    ItemMeta itemMeta = infoButton.getItemMeta();
                    itemMeta.setCustomModelData(MetadataHandler.signatureID);
                    infoButton.setItemMeta(itemMeta);
                }
                repairInventory.setItem(i, infoButton);
                continue;
            }

            if (i == RepairMenuConfig.cancelSlot) {
                repairInventory.setItem(i, RepairMenuConfig.cancelButton);
                continue;
            }

            if (i == eliteItemInformationInputSlot) {
                repairInventory.setItem(i, RepairMenuConfig.eliteItemInputInfoButton);
                continue;
            }

            if (i == eliteScrapInformationInputSlot) {
                repairInventory.setItem(i, RepairMenuConfig.eliteScrapInputInfoButton);
                continue;
            }

            if (i == informationOutputSlot) {
                repairInventory.setItem(i, RepairMenuConfig.outputInfoButton);
                continue;
            }


            if (i == RepairMenuConfig.confirmSlot) {

                ItemStack clonedConfirmButton = RepairMenuConfig.confirmButton.clone();

                List<String> lore = new ArrayList<>();
                for (String string : RepairMenuConfig.confirmButton.getItemMeta().getLore())
                    lore.add(string);
                RepairMenuConfig.confirmButton.getItemMeta().setLore(lore);
                ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
                clonedMeta.setLore(lore);
                clonedConfirmButton.setItemMeta(clonedMeta);
                repairInventory.setItem(i, clonedConfirmButton);
                continue;

            }


            if (i == RepairMenuConfig.eliteItemInputSlot || i == RepairMenuConfig.eliteScrapInputSlot || i == RepairMenuConfig.outputSlot)
                continue;

            if (DefaultConfig.isUseGlassToFillMenuEmptySpace())
                repairInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        player.openInventory(repairInventory);
        createEliteMenu(repairInventory, inventories);
    }

    public static class RepairMenuEvents implements Listener {
        @EventHandler
        public void onInteract(InventoryClickEvent event) {
            if (!isEliteMenu(event, inventories)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            int clickedSlot = event.getSlot();
            Inventory repairInventory = event.getView().getTopInventory();
            Inventory playerInventory = event.getView().getBottomInventory();

            boolean inventoryHasFreeSlots = false;
            for (ItemStack iteratedStack : player.getInventory().getStorageContents())
                if (iteratedStack == null) {
                    inventoryHasFreeSlots = true;
                    break;
                }


            if (isBottomMenu(event)) {
                //Item is scrap
                if (ItemTagger.hasEnchantment(currentItem.getItemMeta(), "EliteScrap")) {
                    int scrapLevel = ItemTagger.getEnchantment(currentItem.getItemMeta(), "EliteScrap");
                    if (scrapLevel >= 0) {
                        if (repairInventory.getItem(scrapItemInputSlot) == null) {
                            repairInventory.setItem(scrapItemInputSlot, currentItem);
                            playerInventory.clear(clickedSlot);
                            calculateOutput(repairInventory);
                        }
                        return;
                    }
                }

                //Item is elite item
                if (EliteItemManager.isEliteMobsItem(currentItem))
                    if (currentItem.getItemMeta() instanceof Damageable)
                        if (repairInventory.getItem(eliteItemInputSlot) == null) {
                            repairInventory.setItem(eliteItemInputSlot, currentItem);
                            playerInventory.remove(currentItem);
                            calculateOutput(repairInventory);
                        }

            } else if (isTopMenu(event)) {

                if (!inventoryHasFreeSlots) {
                    player.sendMessage(ProceduralShopMenuConfig.messageFullInventory);
                    player.closeInventory();
                }

                //return item to inventory
                if (event.getSlot() == scrapItemInputSlot || event.getSlot() == eliteItemInputSlot) {
                    playerInventory.addItem(currentItem);
                    repairInventory.remove(currentItem);
                    calculateOutput(repairInventory);
                    return;
                }

                //cancel button
                if (event.getSlot() == RepairMenuConfig.cancelSlot) {
                    player.closeInventory();
                    return;
                }

                //confirm button
                if (event.getSlot() == RepairMenuConfig.confirmSlot) {
                    if (repairInventory.getItem(outputSlot) != null) {
                        repairInventory.setItem(RepairMenuConfig.eliteItemInputSlot, null);
                        repairInventory.setItem(RepairMenuConfig.eliteScrapInputSlot, null);
                        playerInventory.addItem(repairInventory.getItem(outputSlot));
                        repairInventory.remove(repairInventory.getItem(outputSlot));
                        repairInventory.setItem(outputSlot, null);
                    }
                }

            }

        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (inventories.contains(event.getInventory())) {
                inventories.remove(event.getInventory());
                EliteMenu.cancel(event.getPlayer(), event.getView().getTopInventory(), event.getView().getBottomInventory(), Arrays.asList(eliteItemInputSlot, scrapItemInputSlot));
            }
        }

    }

}
