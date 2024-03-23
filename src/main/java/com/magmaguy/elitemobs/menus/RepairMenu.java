package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.config.menus.premade.RepairMenuConfig;
import com.magmaguy.elitemobs.items.customenchantments.RepairEnchantment;
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

        int scrapLevel = RepairEnchantment.getRepairLevel(repairInventory.getItem(RepairMenuConfig.eliteScrapInputSlot));
        ItemStack outputItem = repairInventory.getItem(RepairMenuConfig.eliteItemInputSlot).clone();
        int baselineRepair = 100;
        int newDamage = baselineRepair * scrapLevel;
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

        Inventory repairInventory = Bukkit.createInventory(player, 45, menuName);

        for (int i = 0; i < repairInventory.getSize(); i++) {
            if (RepairMenuConfig.cancelSlots.contains(i)) {
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


            if (RepairMenuConfig.confirmSlots.contains(i)) {

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
            Inventory repairInventory = event.getView().getTopInventory();
            Inventory playerInventory = event.getView().getBottomInventory();

            if (currentItem == null) return;

            if (isBottomMenu(event)) {
                //Item is scrap
                if (RepairEnchantment.isRepairItem(currentItem) && repairInventory.getItem(scrapItemInputSlot) == null) {
                    int scrapLevel = RepairEnchantment.getRepairLevel(currentItem);
                    if (scrapLevel >= 0) {
                        moveOneItemUp(scrapItemInputSlot, event);
                        calculateOutput(repairInventory);
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

                if (currentItem == null) return;

                //return item to inventory
                if (event.getSlot() == scrapItemInputSlot || event.getSlot() == eliteItemInputSlot) {
                    player.getWorld().dropItem(player.getLocation(), currentItem);
                    repairInventory.remove(currentItem);
                    calculateOutput(repairInventory);
                    return;
                }

                //cancel button
                if (RepairMenuConfig.cancelSlots.contains(event.getSlot())) {
                    player.closeInventory();
                    return;
                }

                //confirm button
                if (RepairMenuConfig.confirmSlots.contains(event.getSlot())) {
                    if (repairInventory.getItem(outputSlot) != null) {
                        repairInventory.setItem(RepairMenuConfig.eliteItemInputSlot, null);
                        repairInventory.setItem(RepairMenuConfig.eliteScrapInputSlot, null);
                        if (repairInventory.getItem(outputSlot) != null) {
                            player.getWorld().dropItem(player.getLocation(), repairInventory.getItem(outputSlot));
                            repairInventory.remove(repairInventory.getItem(outputSlot));
                        }
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
