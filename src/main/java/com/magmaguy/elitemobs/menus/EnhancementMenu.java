package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.api.EliteMobsItemDetector;
import com.magmaguy.elitemobs.config.menus.premade.EnhancementMenuConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
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

public class EnhancementMenu extends EliteMenu {
    public static HashMap<Player, Inventory> inventories = new HashMap<>();
    private static final int eliteItemInputSlot = EnhancementMenuConfig.eliteItemInputSlot;
    private static final int scrapItemInputSlot = EnhancementMenuConfig.eliteUpgradeOrbInputSlot;
    private static final int outputSlot = EnhancementMenuConfig.outputSlot;
    private static final int eliteItemInformationInputSlot = EnhancementMenuConfig.eliteItemInputInformationSlot;
    private static final int eliteScrapInformationInputSlot = EnhancementMenuConfig.eliteUpgradeOrbInputInformationSlot;
    private static final int informationOutputSlot = EnhancementMenuConfig.outputInformationSlot;

    /**
     * Creates a menu for scrapping elitemobs items. Only special Elite Mob items can be scrapped here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructEnhancementMenu(Player player) {
        Inventory EnhancementInventory = Bukkit.createInventory(player, 54, EnhancementMenuConfig.shopName);

        for (int i = 0; i < EnhancementInventory.getSize(); i++) {

            if (i == EnhancementMenuConfig.infoSlot) {
                EnhancementInventory.setItem(i, EnhancementMenuConfig.infoButton);
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
            EnhancementInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        player.openInventory(EnhancementInventory);
        createEliteMenu(player, EnhancementInventory, inventories);
    }

    public static class EnhancementMenuEvents implements Listener {
        @EventHandler
        public void onInteract(InventoryClickEvent event) {
            if (!isEliteMenu(event, inventories)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            Inventory EnhancementInventory = event.getView().getTopInventory();
            Inventory playerInventory = event.getView().getBottomInventory();

            if (isBottomMenu(event)) {

                //Item is upgrade orb
                if (ItemTagger.hasEnchantment(currentItem.getItemMeta(), "EliteUpgradeItem")) {
                    int upgradeOrbLevel = ItemTagger.getEnchantment(currentItem.getItemMeta(), "EliteUpgradeItem");
                    if (upgradeOrbLevel >= 0) {
                        if (EnhancementInventory.getItem(scrapItemInputSlot) == null) {
                            EnhancementInventory.setItem(scrapItemInputSlot, currentItem);
                            playerInventory.remove(currentItem);
                            calculateOutput(EnhancementInventory);
                        }
                        return;
                    }
                }

                //Item is elite combat item
                if (EliteMobsItemDetector.isEliteMobsItem(currentItem))
                    if (ItemTierFinder.getMainCombatEnchantment(currentItem.getType()) != null)
                        if (EnhancementInventory.getItem(eliteItemInputSlot) == null) {
                            EnhancementInventory.setItem(eliteItemInputSlot, currentItem);
                            playerInventory.remove(currentItem);
                            calculateOutput(EnhancementInventory);
                        }

            } else if (isTopMenu(event)) {

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
        public void onInventoryClose(InventoryCloseEvent event) {
            if (!EliteMenu.onInventoryClose(event, inventories)) return;
            EliteMenu.cancel(event.getView().getTopInventory(), event.getView().getBottomInventory(), Arrays.asList(eliteItemInputSlot, scrapItemInputSlot));
        }

    }

    private static void calculateOutput(Inventory EnhancementInventory) {
        if (EnhancementInventory.getItem(EnhancementMenuConfig.eliteUpgradeOrbInputSlot) == null || EnhancementInventory.getItem(EnhancementMenuConfig.eliteItemInputSlot) == null) {
            EnhancementInventory.setItem(EnhancementMenuConfig.outputSlot, null);
            return;
        }
        int enhancementScore = ItemTagger.getEnchantment(EnhancementInventory.getItem(EnhancementMenuConfig.eliteUpgradeOrbInputSlot).getItemMeta(), "EliteUpgradeItem");
        int itemScore = ItemTierFinder.findBattleTier(EnhancementInventory.getItem(EnhancementMenuConfig.eliteItemInputSlot));

        if (enhancementScore != itemScore + 1) {
            EnhancementInventory.setItem(EnhancementMenuConfig.outputSlot, null);
            return;
        }

        ItemStack outputItem = EnhancementInventory.getItem(EnhancementMenuConfig.eliteItemInputSlot).clone();
        Enchantment mainCombatEnchantment = ItemTierFinder.getMainCombatEnchantment(outputItem.getType());
        NamespacedKey enchantmentKey = mainCombatEnchantment.getKey();
        ItemMeta itemMeta = outputItem.getItemMeta();
        int enchantmentLevel = ItemTagger.getEnchantment(outputItem.getItemMeta(), enchantmentKey) + 1;
        if (outputItem.getEnchantmentLevel(mainCombatEnchantment) < mainCombatEnchantment.getMaxLevel())
            outputItem.addEnchantment(mainCombatEnchantment, enchantmentLevel);
        ItemTagger.registerEnchantment(itemMeta, enchantmentKey, enchantmentLevel);
        outputItem.setItemMeta(itemMeta);

        new EliteItemLore(outputItem, false);

        EnhancementInventory.setItem(outputSlot, outputItem);
    }

}
