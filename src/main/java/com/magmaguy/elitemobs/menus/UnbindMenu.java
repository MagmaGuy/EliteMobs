package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.api.EliteMobsItemDetector;
import com.magmaguy.elitemobs.config.menus.premade.UnbinderMenuConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.UnbindEnchantment;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UnbindMenu extends EliteMenu {
    public static HashMap<Player, Inventory> inventories = new HashMap<>();
    private static final int eliteItemInputSlot = UnbinderMenuConfig.eliteItemInputSlot;
    private static final int scrapItemInputSlot = UnbinderMenuConfig.eliteUnbindInputSlot;
    private static final int outputSlot = UnbinderMenuConfig.outputSlot;
    private static final int eliteItemInformationInputSlot = UnbinderMenuConfig.eliteItemInputInformationSlot;
    private static final int eliteScrapInformationInputSlot = UnbinderMenuConfig.eliteScrapInputInformationSlot;
    private static final int informationOutputSlot = UnbinderMenuConfig.outputInformationSlot;

    /**
     * Creates a menu for scrapping elitemobs items. Only special Elite Mob items can be scrapped here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructUnbinderMenu(Player player) {
        Inventory UnbinderInventory = Bukkit.createInventory(player, 54, UnbinderMenuConfig.shopName);

        for (int i = 0; i < UnbinderInventory.getSize(); i++) {

            if (i == UnbinderMenuConfig.infoSlot) {
                UnbinderInventory.setItem(i, UnbinderMenuConfig.infoButton);
                continue;
            }

            if (i == UnbinderMenuConfig.cancelSlot) {
                UnbinderInventory.setItem(i, UnbinderMenuConfig.cancelButton);
                continue;
            }

            if (i == eliteItemInformationInputSlot) {
                UnbinderInventory.setItem(i, UnbinderMenuConfig.eliteItemInputInfoButton);
                continue;
            }

            if (i == eliteScrapInformationInputSlot) {
                UnbinderInventory.setItem(i, UnbinderMenuConfig.eliteUnbindInputInfoButton);
                continue;
            }

            if (i == informationOutputSlot) {
                UnbinderInventory.setItem(i, UnbinderMenuConfig.outputInfoButton);
                continue;
            }


            if (i == UnbinderMenuConfig.confirmSlot) {

                ItemStack clonedConfirmButton = UnbinderMenuConfig.confirmButton.clone();

                List<String> lore = new ArrayList<>();
                for (String string : UnbinderMenuConfig.confirmButton.getItemMeta().getLore())
                    lore.add(string);
                UnbinderMenuConfig.confirmButton.getItemMeta().setLore(lore);
                ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
                clonedMeta.setLore(lore);
                clonedConfirmButton.setItemMeta(clonedMeta);
                UnbinderInventory.setItem(i, clonedConfirmButton);
                continue;

            }


            if (i == UnbinderMenuConfig.eliteItemInputSlot || i == UnbinderMenuConfig.eliteUnbindInputSlot || i == UnbinderMenuConfig.outputSlot)
                continue;
            UnbinderInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        player.openInventory(UnbinderInventory);
        createEliteMenu(player, UnbinderInventory, inventories);
    }

    public static class UnbinderMenuEvents implements Listener {
        @EventHandler
        public void onInteract(InventoryClickEvent event) {
            if (!isEliteMenu(event, inventories)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            Inventory unbinderInventory = event.getView().getTopInventory();
            Inventory playerInventory = event.getView().getBottomInventory();

            if (isBottomMenu(event)) {
                //Item is unbind scroll
                if (ItemTagger.hasEnchantment(currentItem.getItemMeta(), UnbindEnchantment.key)) {
                    if (unbinderInventory.getItem(scrapItemInputSlot) == null) {
                        unbinderInventory.setItem(scrapItemInputSlot, currentItem);
                        playerInventory.remove(currentItem);
                        calculateOutput(unbinderInventory);
                    }
                    return;
                }

                //Item is elite item
                if (EliteMobsItemDetector.isEliteMobsItem(currentItem))
                    if (currentItem.getItemMeta() instanceof Damageable)
                        if (unbinderInventory.getItem(eliteItemInputSlot) == null) {
                            unbinderInventory.setItem(eliteItemInputSlot, currentItem);
                            playerInventory.remove(currentItem);
                            calculateOutput(unbinderInventory);
                        }

            } else if (isTopMenu(event)) {

                //return item to inventory
                if (event.getSlot() == scrapItemInputSlot || event.getSlot() == eliteItemInputSlot) {
                    playerInventory.addItem(currentItem);
                    unbinderInventory.remove(currentItem);
                    calculateOutput(unbinderInventory);
                    return;
                }

                //cancel button
                if (event.getSlot() == UnbinderMenuConfig.cancelSlot) {
                    player.closeInventory();
                    return;
                }

                //confirm button
                if (event.getSlot() == UnbinderMenuConfig.confirmSlot) {
                    if (unbinderInventory.getItem(outputSlot) != null) {
                        unbinderInventory.setItem(UnbinderMenuConfig.eliteItemInputSlot, null);
                        unbinderInventory.setItem(UnbinderMenuConfig.eliteUnbindInputSlot, null);
                        playerInventory.addItem(unbinderInventory.getItem(outputSlot));
                        unbinderInventory.remove(unbinderInventory.getItem(outputSlot));
                        unbinderInventory.setItem(outputSlot, null);
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

    private static void calculateOutput(Inventory UnbinderInventory) {
        if (UnbinderInventory.getItem(UnbinderMenuConfig.eliteUnbindInputSlot) == null || UnbinderInventory.getItem(UnbinderMenuConfig.eliteItemInputSlot) == null) {
            UnbinderInventory.setItem(UnbinderMenuConfig.outputSlot, null);
            return;
        }
        ItemStack outputItem = UnbinderInventory.getItem(UnbinderMenuConfig.eliteItemInputSlot).clone();
        UnbinderInventory.setItem(outputSlot, UnbindEnchantment.unbindItem(outputItem));
    }

}
