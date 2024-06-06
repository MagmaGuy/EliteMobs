package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.config.menus.premade.BuyOrSellMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.ProceduralShopMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class ProceduralShopMenu {

    private static final List<Integer> validSlots = ProceduralShopMenuConfig.storeSlots;
    public static Set<UUID> cooldownPlayers = new HashSet<>();

    public static void shutdown() {
        cooldownPlayers.clear();
        ProceduralShopMenuEvents.menus.clear();
    }

    public static void shopInitializer(Player player) {

        if (!EconomySettingsConfig.isEnableEconomy()) return;
        BuyOrSellMenu.constructBuyOrSellMenu(player, BuyOrSellMenuConfig.BUY_PROCEDURAL_ITEM);

    }

    public static void shopConstructor(Player player) {
        String menuName = ProceduralShopMenuConfig.shopName;
        if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes())
            menuName = ChatColor.WHITE + "\uF801\uDB80\uDC8B\uF805          " + menuName;
        Inventory shopInventory = Bukkit.createInventory(player, 54, menuName);
        populateShop(shopInventory, player);
        player.openInventory(shopInventory);
        ProceduralShopMenuEvents.menus.add(shopInventory);

    }

    private static void populateShop(Inventory shopInventory, Player player) {

        ItemStack rerollButton = ProceduralShopMenuConfig.rerollItem;
        if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes()) {
            rerollButton.setType(Material.PAPER);
            ItemMeta itemMeta = rerollButton.getItemMeta();
            itemMeta.setCustomModelData(MetadataHandler.signatureID);
            rerollButton.setItemMeta(itemMeta);
        }

        shopInventory.setItem(ProceduralShopMenuConfig.rerollSlot, rerollButton);
        shopContents(shopInventory, player);

    }

    private static void shopContents(Inventory shopInventory, Player player) {

        Random random = new Random();

        for (int i : validSlots) {

            int balancedMax = ProceduralShopMenuConfig.maxTier - ProceduralShopMenuConfig.minTier;
            int balancedMin = ProceduralShopMenuConfig.minTier;

            int randomTier = random.nextInt(balancedMax) + balancedMin;

            ItemStack itemStack = ItemConstructor.constructItem(randomTier, null, player, true);
            //SoulbindEnchantment.addEnchantment(itemStack, player);
            new EliteItemLore(itemStack, true);

            shopInventory.setItem(i, itemStack);

        }

    }

    public static class ProceduralShopMenuEvents implements Listener {
        public static Set<Inventory> menus = new HashSet<>();

        @EventHandler
        public void onClick(InventoryClickEvent event) {

            if (!EliteMenu.isEliteMenu(event, menus)) return;
            if (event.getClickedInventory() == null || !event.getClickedInventory().getType().equals(InventoryType.CHEST)) {
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            if (!SharedShopElements.itemNullPointerPrevention(event)) return;

            //reroll loot button
            if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ProceduralShopMenuConfig.rerollItem.getItemMeta().getDisplayName())) {
                if (cooldownPlayers.contains(event.getWhoClicked().getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }

                cooldownPlayers.add(event.getWhoClicked().getUniqueId());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        cooldownPlayers.remove(event.getWhoClicked().getUniqueId());
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 20 * 2L);

                populateShop(event.getInventory(), Bukkit.getPlayer(event.getWhoClicked().getUniqueId()));
                event.setCancelled(true);
                return;
            }

            if (!ItemTagger.isEliteItem(event.getCurrentItem())) {
                event.setCancelled(true);
                return;
            }

            Player player = (Player) event.getWhoClicked();
            ItemStack itemStack = event.getCurrentItem();
            String itemDisplayName = itemStack.getItemMeta().getDisplayName();

            double itemValue = ItemWorthCalculator.determineItemWorth(itemStack, player);

            boolean inventoryHasFreeSlots = false;
            for (ItemStack iteratedStack : player.getInventory().getStorageContents())
                if (iteratedStack == null) {
                    inventoryHasFreeSlots = true;
                    break;
                }

            //These slots are for buying items
            if (EliteMenu.isTopMenu(event)) {

                if (!inventoryHasFreeSlots) {

                    player.sendMessage(ProceduralShopMenuConfig.messageFullInventory);
                    player.closeInventory();

                } else if (EconomyHandler.checkCurrency(player.getUniqueId()) >= itemValue) {
                    //player has enough money
                    EconomyHandler.subtractCurrency(player.getUniqueId(), itemValue);
                    new EliteItemLore(itemStack, false);
                    player.getInventory().addItem(itemStack);
                    populateShop(event.getInventory(), Bukkit.getPlayer(event.getWhoClicked().getUniqueId()));
                    SharedShopElements.buyMessage(player, itemDisplayName, itemValue);

                } else {

                    player.closeInventory();
                    menus.remove(player);
                    SharedShopElements.insufficientFundsMessage(player, itemValue);

                }

            }

        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            menus.remove(event.getInventory());
        }

    }

}
