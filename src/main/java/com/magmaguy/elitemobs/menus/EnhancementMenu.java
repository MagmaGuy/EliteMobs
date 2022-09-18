package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.config.menus.premade.EnhancementMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.ProceduralShopMenuConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EnhancementMenu extends EliteMenu {
    private static final int eliteItemInputSlot = EnhancementMenuConfig.eliteItemInputSlot;
    private static final int scrapItemInputSlot = EnhancementMenuConfig.eliteUpgradeOrbInputSlot;
    private static final int outputSlot = EnhancementMenuConfig.outputSlot;
    private static final int eliteItemInformationInputSlot = EnhancementMenuConfig.eliteItemInputInformationSlot;
    private static final int eliteScrapInformationInputSlot = EnhancementMenuConfig.eliteUpgradeOrbInputInformationSlot;
    private static final int informationOutputSlot = EnhancementMenuConfig.outputInformationSlot;
    public static Set<Inventory> inventories = new HashSet<>();

    private static void calculateOutput(Inventory EnhancementInventory) {
        if (EnhancementInventory.getItem(EnhancementMenuConfig.eliteUpgradeOrbInputSlot) == null || EnhancementInventory.getItem(EnhancementMenuConfig.eliteItemInputSlot) == null) {
            EnhancementInventory.setItem(EnhancementMenuConfig.outputSlot, null);
            return;
        }
        int enhancementScore = ItemTagger.getEnchantment(EnhancementInventory.getItem(EnhancementMenuConfig.eliteUpgradeOrbInputSlot).getItemMeta(), "EliteUpgradeItem");
        int itemScore = EliteItemManager.getRoundedItemLevel(EnhancementInventory.getItem(EnhancementMenuConfig.eliteItemInputSlot));

        if (enhancementScore <= itemScore) {
            EnhancementInventory.setItem(EnhancementMenuConfig.outputSlot, null);
            return;
        }

        ItemStack outputItem = EnhancementInventory.getItem(EnhancementMenuConfig.eliteItemInputSlot).clone();
        ItemTagger.setEliteDamageAttribute(outputItem, EliteItemManager.getDamageAtNextItemLevel(outputItem));

        new EliteItemLore(outputItem, false);

        EnhancementInventory.setItem(outputSlot, outputItem);
    }

    /**
     * Creates a menu for scrapping elitemobs items. Only special Elite Mob items can be scrapped here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructEnhancementMenu(Player player) {
        String menuName = EnhancementMenuConfig.shopName;
        if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes())
            menuName = ChatColor.WHITE + "\uF801\uDB80\uDC2A\uF805       " + menuName;
        Inventory EnhancementInventory = Bukkit.createInventory(player, 54, menuName);

        for (int i = 0; i < EnhancementInventory.getSize(); i++) {

            if (i == EnhancementMenuConfig.infoSlot) {
                ItemStack infoButton = EnhancementMenuConfig.infoButton;
                if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes()) {
                    infoButton.setType(Material.PAPER);
                    ItemMeta itemMeta = infoButton.getItemMeta();
                    itemMeta.setCustomModelData(MetadataHandler.signatureID);
                    infoButton.setItemMeta(itemMeta);
                }
                EnhancementInventory.setItem(i, infoButton);
                continue;
            }

            if (i == EnhancementMenuConfig.cancelSlot) {
                EnhancementInventory.setItem(i, EnhancementMenuConfig.cancelButton);
                continue;
            }

            if (i == eliteItemInformationInputSlot) {
                EnhancementInventory.setItem(i, EnhancementMenuConfig.eliteItemInputInfoButton);
                continue;
            }

            if (i == eliteScrapInformationInputSlot) {
                EnhancementInventory.setItem(i, EnhancementMenuConfig.eliteUpgradeOrbInputInfoButton);
                continue;
            }

            if (i == informationOutputSlot) {
                EnhancementInventory.setItem(i, EnhancementMenuConfig.outputInfoButton);
                continue;
            }


            if (i == EnhancementMenuConfig.confirmSlot) {

                ItemStack clonedConfirmButton = EnhancementMenuConfig.confirmButton.clone();

                List<String> lore = new ArrayList<>();
                for (String string : EnhancementMenuConfig.confirmButton.getItemMeta().getLore())
                    lore.add(string);
                EnhancementMenuConfig.confirmButton.getItemMeta().setLore(lore);
                ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
                clonedMeta.setLore(lore);
                clonedConfirmButton.setItemMeta(clonedMeta);
                EnhancementInventory.setItem(i, clonedConfirmButton);
                continue;

            }


            if (i == EnhancementMenuConfig.eliteItemInputSlot || i == EnhancementMenuConfig.eliteUpgradeOrbInputSlot || i == EnhancementMenuConfig.outputSlot)
                continue;
            if (DefaultConfig.isUseGlassToFillMenuEmptySpace())
                EnhancementInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        player.openInventory(EnhancementInventory);
        createEliteMenu(EnhancementInventory, inventories);
    }

    public static class EnhancementMenuEvents implements Listener {
        @EventHandler
        public void onInteract(InventoryClickEvent event) {
            if (!isEliteMenu(event, inventories)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            int clickedSlot = event.getSlot();
            Inventory EnhancementInventory = event.getView().getTopInventory();
            Inventory playerInventory = event.getView().getBottomInventory();

            boolean inventoryHasFreeSlots = false;
            for (ItemStack iteratedStack : player.getInventory().getStorageContents())
                if (iteratedStack == null) {
                    inventoryHasFreeSlots = true;
                    break;
                }

            if (isBottomMenu(event)) {

                //Item is upgrade orb
                if (ItemTagger.hasEnchantment(currentItem.getItemMeta(), "EliteUpgradeItem")) {
                    int upgradeOrbLevel = ItemTagger.getEnchantment(currentItem.getItemMeta(), "EliteUpgradeItem");
                    if (upgradeOrbLevel >= 0) {
                        if (EnhancementInventory.getItem(scrapItemInputSlot) == null) {
                            EnhancementInventory.setItem(scrapItemInputSlot, currentItem);
                            playerInventory.clear(clickedSlot);
                            calculateOutput(EnhancementInventory);
                        }
                        return;
                    }
                }

                //Item is elite combat item
                if (EliteItemManager.isEliteMobsItem(currentItem))
                    if (EliteItemManager.isWeapon(currentItem))
                        if (EnhancementInventory.getItem(eliteItemInputSlot) == null) {
                            EnhancementInventory.setItem(eliteItemInputSlot, currentItem);
                            playerInventory.remove(currentItem);
                            calculateOutput(EnhancementInventory);
                        }


            } else if (isTopMenu(event)) {

                if (!inventoryHasFreeSlots) {
                    player.sendMessage(ProceduralShopMenuConfig.messageFullInventory);
                    player.closeInventory();
                }

                //return item to inventory
                if (event.getSlot() == scrapItemInputSlot || event.getSlot() == eliteItemInputSlot) {
                    playerInventory.addItem(currentItem);
                    EnhancementInventory.remove(currentItem);
                    calculateOutput(EnhancementInventory);
                    return;
                }

                //cancel button
                if (event.getSlot() == EnhancementMenuConfig.cancelSlot) {
                    player.closeInventory();
                    return;
                }

                //confirm button
                if (event.getSlot() == EnhancementMenuConfig.confirmSlot) {
                    if (EnhancementInventory.getItem(outputSlot) != null) {
                        EnhancementInventory.setItem(EnhancementMenuConfig.eliteItemInputSlot, null);
                        EnhancementInventory.setItem(EnhancementMenuConfig.eliteUpgradeOrbInputSlot, null);
                        playerInventory.addItem(EnhancementInventory.getItem(outputSlot));
                        EnhancementInventory.remove(EnhancementInventory.getItem(outputSlot));
                        EnhancementInventory.setItem(outputSlot, null);
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
