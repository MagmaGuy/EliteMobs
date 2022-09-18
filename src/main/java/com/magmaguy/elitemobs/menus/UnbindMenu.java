package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.config.menus.premade.ProceduralShopMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.UnbinderMenuConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.UnbindEnchantment;
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

public class UnbindMenu extends EliteMenu {
    private static final int eliteItemInputSlot = UnbinderMenuConfig.getEliteItemInputSlot();
    private static final int scrapItemInputSlot = UnbinderMenuConfig.getEliteUnbindInputSlot();
    private static final int outputSlot = UnbinderMenuConfig.getOutputSlot();
    private static final int eliteItemInformationInputSlot = UnbinderMenuConfig.getEliteItemInputInformationSlot();
    private static final int eliteScrapInformationInputSlot = UnbinderMenuConfig.getEliteScrapInputInformationSlot();
    private static final int informationOutputSlot = UnbinderMenuConfig.getOutputInformationSlot();
    public static Set<Inventory> inventories = new HashSet<>();

    private static void calculateOutput(Inventory UnbinderInventory) {
        if (UnbinderInventory.getItem(UnbinderMenuConfig.getEliteUnbindInputSlot()) == null || UnbinderInventory.getItem(UnbinderMenuConfig.getEliteItemInputSlot()) == null) {
            UnbinderInventory.setItem(UnbinderMenuConfig.getOutputSlot(), null);
            return;
        }
        ItemStack outputItem = UnbinderInventory.getItem(UnbinderMenuConfig.getEliteItemInputSlot()).clone();
        UnbinderInventory.setItem(outputSlot, UnbindEnchantment.unbindItem(outputItem));
    }

    /**
     * Creates a menu for scrapping elitemobs items. Only special Elite Mob items can be scrapped here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructUnbinderMenu(Player player) {
        String menuName = UnbinderMenuConfig.getShopName();
        if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes())
            menuName = ChatColor.WHITE + "\uF801\uDB80\uDC9B\uF805          " + menuName;
        Inventory UnbinderInventory = Bukkit.createInventory(player, 54, menuName);

        for (int i = 0; i < UnbinderInventory.getSize(); i++) {

            if (i == UnbinderMenuConfig.getInfoSlot()) {
                ItemStack infoButton = UnbinderMenuConfig.getInfoButton();
                if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes()) {
                    infoButton.setType(Material.PAPER);
                    ItemMeta itemMeta = infoButton.getItemMeta();
                    itemMeta.setCustomModelData(MetadataHandler.signatureID);
                    infoButton.setItemMeta(itemMeta);
                }
                UnbinderInventory.setItem(i, infoButton);
                continue;
            }

            if (i == UnbinderMenuConfig.getCancelSlot()) {
                UnbinderInventory.setItem(i, UnbinderMenuConfig.getCancelButton());
                continue;
            }

            if (i == eliteItemInformationInputSlot) {
                UnbinderInventory.setItem(i, UnbinderMenuConfig.getEliteItemInputInfoButton());
                continue;
            }

            if (i == eliteScrapInformationInputSlot) {
                UnbinderInventory.setItem(i, UnbinderMenuConfig.getEliteUnbindInputInfoButton());
                continue;
            }

            if (i == informationOutputSlot) {
                UnbinderInventory.setItem(i, UnbinderMenuConfig.getOutputInfoButton());
                continue;
            }


            if (i == UnbinderMenuConfig.getConfirmSlot()) {

                ItemStack clonedConfirmButton = UnbinderMenuConfig.getConfirmButton().clone();

                List<String> lore = new ArrayList<>();
                for (String string : UnbinderMenuConfig.getConfirmButton().getItemMeta().getLore())
                    lore.add(string);
                UnbinderMenuConfig.getConfirmButton().getItemMeta().setLore(lore);
                ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
                clonedMeta.setLore(lore);
                clonedConfirmButton.setItemMeta(clonedMeta);
                UnbinderInventory.setItem(i, clonedConfirmButton);
                continue;

            }


            if (i == UnbinderMenuConfig.getEliteItemInputSlot() || i == UnbinderMenuConfig.getEliteUnbindInputSlot() || i == UnbinderMenuConfig.getOutputSlot())
                continue;
            if (DefaultConfig.isUseGlassToFillMenuEmptySpace())
                UnbinderInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        player.openInventory(UnbinderInventory);
        createEliteMenu(UnbinderInventory, inventories);
    }

    public static class UnbinderMenuEvents implements Listener {
        @EventHandler
        public void onInteract(InventoryClickEvent event) {
            if (!isEliteMenu(event, inventories)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            int clickedSlot = event.getSlot();
            Inventory unbinderInventory = event.getView().getTopInventory();
            Inventory playerInventory = event.getView().getBottomInventory();

            boolean inventoryHasFreeSlots = false;
            for (ItemStack iteratedStack : player.getInventory().getStorageContents())
                if (iteratedStack == null) {
                    inventoryHasFreeSlots = true;
                    break;
                }

            if (isBottomMenu(event)) {
                //Item is unbind scroll
                if (ItemTagger.hasEnchantment(currentItem.getItemMeta(), UnbindEnchantment.key)) {
                    if (unbinderInventory.getItem(scrapItemInputSlot) == null) {
                        unbinderInventory.setItem(scrapItemInputSlot, currentItem);
                        playerInventory.clear(clickedSlot);
                        calculateOutput(unbinderInventory);
                    }
                    return;
                }

                //Item is elite item
                if (EliteItemManager.isEliteMobsItem(currentItem))
                    if (currentItem.getItemMeta() instanceof Damageable)
                        if (unbinderInventory.getItem(eliteItemInputSlot) == null) {
                            unbinderInventory.setItem(eliteItemInputSlot, currentItem);
                            playerInventory.remove(currentItem);
                            calculateOutput(unbinderInventory);
                        }

            } else if (isTopMenu(event)) {

                if (!inventoryHasFreeSlots) {
                    player.sendMessage(ProceduralShopMenuConfig.messageFullInventory);
                    player.closeInventory();
                }

                //return item to inventory
                if (event.getSlot() == scrapItemInputSlot || event.getSlot() == eliteItemInputSlot) {
                    playerInventory.addItem(currentItem);
                    unbinderInventory.remove(currentItem);
                    calculateOutput(unbinderInventory);
                    return;
                }

                //cancel button
                if (event.getSlot() == UnbinderMenuConfig.getCancelSlot()) {
                    player.closeInventory();
                    return;
                }

                //confirm button
                if (event.getSlot() == UnbinderMenuConfig.getConfirmSlot()) {
                    if (unbinderInventory.getItem(outputSlot) != null) {
                        unbinderInventory.setItem(UnbinderMenuConfig.getEliteItemInputSlot(), null);
                        unbinderInventory.setItem(UnbinderMenuConfig.getEliteUnbindInputSlot(), null);
                        playerInventory.addItem(unbinderInventory.getItem(outputSlot));
                        unbinderInventory.remove(unbinderInventory.getItem(outputSlot));
                        unbinderInventory.setItem(outputSlot, null);
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
