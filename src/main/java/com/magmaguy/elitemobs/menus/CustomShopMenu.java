package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.BuyOrSellMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.CustomShopMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Created by MagmaGuy on 20/06/2017.
 */
public class CustomShopMenu {

    public static Set<UUID> cooldownPlayers = new HashSet<>();

    public static void shutdown() {
        cooldownPlayers.clear();
        CustomShopMenu.CustomShopMenuEvents.menus.clear();
    }

    /**
     * Creates a new instance of a BuyOrSellMenu
     *
     * @param player Player for whom the new menu will show up
     */
    public static void customShopInitializer(Player player) {

        if (!EconomySettingsConfig.isEnableEconomy()) return;
        BuyOrSellMenu.constructBuyOrSellMenu(player, BuyOrSellMenuConfig.BUY_CUSTOM_ITEM);

    }

    /**
     * Creates a new instance of a custom shop
     *
     * @param player Player for whom the new menu will show up
     */
    public static void customShopConstructor(Player player) {
        String menuName = CustomShopMenuConfig.shopName;
        if (DefaultConfig.useResourcePackModels())
            menuName = ChatColor.WHITE + "\uDB83\uDEF1\uDB83\uDE06\uDB83\uDEF5          " + menuName;
        Inventory shopInventory = Bukkit.createInventory(player, 54, menuName);
        populateShop(shopInventory, player);
        player.openInventory(shopInventory);
        CustomShopMenuEvents.menus.add(shopInventory);
    }

    /**
     * Preliminary shop fill
     *
     * @param shopInventory Inventory to be filled
     */
    private static void populateShop(Inventory shopInventory, Player player) {

        ItemStack rerollButton = CustomShopMenuConfig.rerollItem;
        if (DefaultConfig.useResourcePackModels()) {
            rerollButton.setType(Material.PAPER);
            CustomModelAdder.addCustomModel(rerollButton, CustomModelsConfig.goldenQuestionMark);
        }

        shopInventory.setItem(CustomShopMenuConfig.rerollSlot, rerollButton);
        shopContents(shopInventory, player);

    }

    /**
     * Fills with the items to be sold in the shop
     *
     * @param shopInventory Inventory to be filled
     */
    private static void shopContents(Inventory shopInventory, Player player) {

        //Anything after 8 is populated
        Random random = new Random();

        for (int i : CustomShopMenuConfig.storeSlots) {

            int itemEntryIndex = random.nextInt(CustomItem.getCustomItemStackShopList().size());

            ItemStack itemStack = CustomItem.getCustomItemStackShopList().get(itemEntryIndex).clone();
            SoulbindEnchantment.addEnchantment(itemStack, player);
            new EliteItemLore(itemStack, true);

            shopInventory.setItem(i, itemStack);

        }

    }

    public static class CustomShopMenuEvents implements Listener {
        public static HashSet<Inventory> menus = new HashSet<>();

        @EventHandler
        public void onClick(InventoryClickEvent event) {

            if (!EliteMenu.isEliteMenu(event, menus)) return;
            event.setCancelled(true);
            if (!SharedShopElements.itemNullPointerPrevention(event)) return;

            //reroll loot button
            if (event.getCurrentItem().getItemMeta().getDisplayName().equals(CustomShopMenuConfig.rerollItem.getItemMeta().getDisplayName())) {

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
            String itemDisplayName = event.getCurrentItem().getItemMeta().getDisplayName();

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

                    player.sendMessage(CustomShopMenuConfig.messageFullInventory);
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
