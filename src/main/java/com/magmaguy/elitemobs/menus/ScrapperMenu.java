package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.config.menus.premade.ScrapperMenuConfig;
import com.magmaguy.elitemobs.items.customenchantments.RepairEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class ScrapperMenu extends EliteMenu {
    private static final List<Integer> validSlots = ScrapperMenuConfig.storeSlots;

    public static Set<Inventory> inventories = new HashSet<>();

    /**
     * Creates a menu for scrapping elitemobs items. Only special Elite Mob items can be scrapped here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructScrapMenu(Player player) {

        String menuName = ScrapperMenuConfig.shopName;
        if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes())
            menuName = ChatColor.WHITE + "\uF801\uDB80\uDC2B\uF805         " + menuName;

        Inventory scrapInventory = Bukkit.createInventory(player, 54, menuName);

        for (int i = 0; i < 54; i++) {
            if (ScrapperMenuConfig.cancelSlots.contains(i)) {
                scrapInventory.setItem(i, ScrapperMenuConfig.cancelButton);
                continue;
            }

            if (ScrapperMenuConfig.confirmSlots.contains(i)) {

                ItemStack clonedConfirmButton = ScrapperMenuConfig.confirmButton.clone();

                List<String> lore = new ArrayList<>();
                for (String string : ScrapperMenuConfig.confirmButton.getItemMeta().getLore())
                    lore.add(string.replace("$chance", ScrapperMenuConfig.scrapChance * 100 + ""));
                ScrapperMenuConfig.confirmButton.getItemMeta().setLore(lore);
                ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
                clonedMeta.setLore(lore);
                clonedConfirmButton.setItemMeta(clonedMeta);
                scrapInventory.setItem(i, clonedConfirmButton);
                continue;

            }

            if (validSlots.contains(i))
                continue;

            if (DefaultConfig.isUseGlassToFillMenuEmptySpace())
                scrapInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        player.openInventory(scrapInventory);
        createEliteMenu(scrapInventory, inventories);

    }

    public static class ScrapperMenuEvents implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            if (!isEliteMenu(event, inventories)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            Inventory shopInventory = event.getView().getTopInventory();
            Inventory playerInventory = event.getView().getBottomInventory();

            if (currentItem == null) return;

            if (EliteMenu.isBottomMenu(event)) {
                //CASE: If the player clicked on something in their inventory to put it on the shop

                //Check if it's an elitemobs item. The soulbind check only says if the player would be able to pick it up, and vanilla items can be picked up
                if (!EliteItemManager.isEliteMobsItem(event.getCurrentItem())) {
                    event.getWhoClicked().sendMessage(ChatColorConverter.convert(EconomySettingsConfig.getShopSaleInstructions()));
                    return;
                }

                //If the item isn't soulbound to the player, it can't be sold by that player
                if (!SoulbindEnchantment.isValidSoulbindUser(currentItem.getItemMeta(), player)) {
                    player.sendMessage(ChatColorConverter.convert(EconomySettingsConfig.getShopSaleOthersItems()));
                    return;
                }

                //Do transfer
                for (int slot : ScrapperMenuConfig.storeSlots)
                    if (shopInventory.getItem(slot) == null) {
                        shopInventory.setItem(slot, currentItem);
                        playerInventory.clear(event.getSlot());
                        break;
                    }

            } else if (EliteMenu.isTopMenu(event)) {

                //CASE: Player clicked on the shop


                //sell items in shop
                if (ScrapperMenuConfig.confirmSlots.contains(event.getSlot())) {
                    int successes = 0;
                    int failures = 0;
                    for (Integer validSlot : validSlots) {
                        ItemStack itemStack = shopInventory.getItem(validSlot);
                        if (itemStack == null)
                            continue;

                        ItemStack scrapItem = RepairEnchantment.generateScrap(itemStack, player, null);

                        for (int i = 0; i < itemStack.getAmount(); i++) {
                            if (ThreadLocalRandom.current().nextDouble() > ScrapperMenuConfig.scrapChance) {
                                failures++;
                                continue;
                            }
                            player.getWorld().dropItem(player.getLocation(), scrapItem);
                            successes++;
                        }
                        itemStack.setAmount(0);
                    }
                    if (successes > 0)
                        player.sendMessage(ChatColorConverter.convert(ItemSettingsConfig.getScrapSucceededMessage().replace("$amount", successes + "")));
                    if (failures > 0)
                        player.sendMessage(ChatColorConverter.convert(ItemSettingsConfig.getScrapFailedMessage().replace("$amount", failures + "")));

                    return;
                }

                //cancel, transfer items back to player inv and exit
                if (ScrapperMenuConfig.cancelSlots.contains(event.getSlot())) {
                    player.closeInventory();
                    return;
                }

                //If player clicks on a border glass pane, do nothing
                if (!validSlots.contains(event.getSlot())) return;


                //If player clicks on one of the items already in the shop, return to their inventory
                moveItemDown(event.getView().getTopInventory(), event.getSlot(), player);
            }

        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (inventories.contains(event.getInventory())) {
                inventories.remove(event.getInventory());
                EliteMenu.cancel(event.getPlayer(), event.getView().getTopInventory(), event.getView().getBottomInventory(), validSlots);
            }
        }

    }

}
