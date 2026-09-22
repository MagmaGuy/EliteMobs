package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.SellMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.magmacore.util.ItemStackGenerator;
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

public class SellMenu extends EliteMenu implements Listener {

    private record SaleSession(StoreLayout layout, ItemStack confirmButton) {}
    private static final java.util.Map<Inventory, SaleSession> sessions = new java.util.IdentityHashMap<>();

    public static Set<Inventory> inventories = new HashSet<>();
    private static final Set<Inventory> selling = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    // Ambiguous external credits keep their stock out of automatic return/retry paths.
    private static final java.util.Map<java.util.UUID, List<HeldSale>> uncertainSales = new java.util.HashMap<>();
    private record HeldSale(ItemStack item, double proceeds) {}

    public static void shutdown() {
        for (Inventory inventory : List.copyOf(inventories)) {
            if (!(inventory.getHolder() instanceof Player player)) continue;
            close(player, inventory);
            if (player.getOpenInventory().getTopInventory() == inventory) player.closeInventory();
        }
        if (!uncertainSales.isEmpty())
            com.magmaguy.magmacore.util.Logger.warn("Unconfirmed sale stock retained for manual reconciliation before restart: " + uncertainSales);
    }

    private static List<Integer> slotsFor(Inventory inventory) {
        SaleSession session = sessions.get(inventory);
        return session == null ? List.of() : session.layout.inputs();
    }

    private static void close(Player player, Inventory inventory) {
        if (!inventories.remove(inventory)) return;
        SaleSession session = sessions.remove(inventory);
        if (session == null) return;
        EliteMenu.cancel(player, inventory, player.getInventory(), session.layout.inputs());
    }

    private static double calculateShopValue(Inventory shopInventory, Player player) {
        double itemWorth = 0;
        for (Integer validSlot : slotsFor(shopInventory)) {
            ItemStack itemStack = shopInventory.getItem(validSlot);
            if (itemStack == null) continue;
            itemWorth += (ItemWorthCalculator.determineResaleWorth(itemStack, player) * itemStack.getAmount());
        }
        return itemWorth;
    }

    private static ItemStack updateConfirmButton(SaleSession session, double itemWorth) {
        ItemStack clonedConfirmButton = session.confirmButton.clone();

        List<String> lore = new ArrayList<>();
        for (String string : clonedConfirmButton.getItemMeta().getLore())
            lore.add(string
                    .replace("$currency_amount", EconomyHandler.formatCurrency(itemWorth))
                    .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));

        ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
        clonedMeta.setLore(lore);
        clonedConfirmButton.setItemMeta(clonedMeta);
        return clonedConfirmButton;
    }

    /**
     * Creates a menu for selling elitemobs items. Only special Elite Mob items can be sold here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructSellMenu(Player player) {

        StoreLayout layout = new StoreLayout(SellMenuConfig.storeSlots, SellMenuConfig.infoSlot,
                SellMenuConfig.cancelSlot, SellMenuConfig.confirmSlot);
        SaleSession session = new SaleSession(layout, SellMenuConfig.confirmButton.clone());
        String menuName = SellMenuConfig.shopName;
        if (DefaultConfig.useResourcePackModels())
            menuName = ChatColor.WHITE + "\uDB83\uDEF1\uDB83\uDE05\uDB83\uDEF5          " + menuName;

        Inventory sellInventory = Bukkit.createInventory(player, 54, menuName);

        for (int i = 0; i < 54; i++) {

            if (i == layout.info()) {
                sellInventory.setItem(i, SellMenuConfig.infoButton);
                continue;
            }

            if (i == layout.cancel()) {
                sellInventory.setItem(i, SellMenuConfig.cancelButton);
                continue;
            }

            if (i == layout.confirm()) {

                ItemStack clonedConfirmButton = SellMenuConfig.confirmButton.clone();

                List<String> lore = new ArrayList<>();
                for (String string : SellMenuConfig.confirmButton.getItemMeta().getLore())
                    lore.add(string
                            .replace("$currency_amount", EconomyHandler.formatCurrency(0))
                            .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));
                SellMenuConfig.confirmButton.getItemMeta().setLore(lore);
                ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
                clonedMeta.setLore(lore);
                clonedConfirmButton.setItemMeta(clonedMeta);
                sellInventory.setItem(i, clonedConfirmButton);
                continue;

            }

            if (layout.inputs().contains(i))
                continue;

            if (DefaultConfig.isUseGlassToFillMenuEmptySpace())
                sellInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        sessions.put(sellInventory, session);
        createEliteMenu(sellInventory, inventories);
        try {
            player.openInventory(sellInventory);
        } finally {
            if (player.getOpenInventory().getTopInventory() != sellInventory) close(player, sellInventory);
        }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!isEliteMenu(event, inventories)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem();
        Inventory shopInventory = event.getView().getTopInventory();
        SaleSession session = sessions.get(shopInventory);
        if (session == null) return;
        StoreLayout layout = session.layout;
        Inventory playerInventory = event.getView().getBottomInventory();

        if (!SharedShopElements.itemNullPointerPrevention(event)) return;

        if (isBottomMenu(event)) {
            //CASE: If the player clicked on something in their inventory to put it on the shop

            //Check if it's an elitemobs item. The soulbind check only says if the player would be able to pick it up, and vanilla items can be picked up
            if (!EliteItemManager.isEliteMobsItem(event.getCurrentItem())) {
                event.getWhoClicked().sendMessage(EconomySettingsConfig.getShopSaleInstructions());
                return;
            }

            //If the item isn't soulbound to the player, it can't be sold by that player
            if (!SoulbindEnchantment.isValidSoulbindUser(currentItem.getItemMeta(), player)) {
                player.sendMessage(EconomySettingsConfig.getShopSaleOthersItems());
                return;
            }

            //If the shop is full, don't let the player put stuff in it
            int firstEmptySlot = -1;
            for (int i : slotsFor(shopInventory))
                if (shopInventory.getItem(i) == null) {
                    firstEmptySlot = i;
                    break;
                }
            if (firstEmptySlot == -1) return;

            //Do transfer
            shopInventory.setItem(firstEmptySlot, currentItem);
            playerInventory.clear(event.getSlot());

            //Update worth of things to be sold, now using cached prices
            event.getInventory().setItem(layout.confirm(), updateConfirmButton(session, calculateShopValue(shopInventory, player)));

        } else {
            //CASE: Player clicked on the shop

            //Signature item, does nothing
            if (event.getSlot() == layout.info())
                return;

            //sell items in shop
            if (event.getSlot() == layout.confirm()) {

                if (!EconomyHandler.isReady(player.getUniqueId())) {
                    player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
                    return;
                }
                if (!selling.add(shopInventory)) return;
                double totalItemValue = 0;
                int sold = 0;
                try {
                    for (Integer validSlot : slotsFor(shopInventory)) {
                        ItemStack itemStack = shopInventory.getItem(validSlot);
                        if (itemStack == null) continue;
                        ItemStack held = itemStack.clone();
                        double itemValue = ItemWorthCalculator.determineResaleWorth(held, player) * held.getAmount();
                        if (!Double.isFinite(itemValue) || itemValue < 0) {
                            player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
                            break;
                        }
                        // Own the stock before entering a provider which may reenter menu callbacks.
                        shopInventory.clear(validSlot);
                        boolean accepted;
                        try {
                            accepted = EconomyHandler.tryCredit(player.getUniqueId(), itemValue);
                        } catch (RuntimeException failure) {
                            uncertainSales.computeIfAbsent(player.getUniqueId(), ignored -> new ArrayList<>())
                                    .add(new HeldSale(held, itemValue));
                            com.magmaguy.magmacore.util.Logger.warn("Unconfirmed sale credit; retained stock for "
                                    + player.getUniqueId() + "; proceeds=" + itemValue + "; item=" + held + "; " + failure);
                            player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
                            break;
                        }
                        if (!accepted) {
                            if (inventories.contains(shopInventory) && player.getOpenInventory().getTopInventory() == shopInventory
                                    && shopInventory.getItem(validSlot) == null) shopInventory.setItem(validSlot, held);
                            else returnUnsold(player, held);
                            player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
                            break;
                        }
                        ++sold;
                        totalItemValue += itemValue;
                        // A provider callback may have closed the menu and returned all other stock.
                        if (!inventories.contains(shopInventory)) break;
                    }
                } finally {
                    selling.remove(shopInventory);
                }
                if (sold > 0) {
                    player.sendMessage(EconomySettingsConfig.getShopBatchSellMessage()
                            .replace("$currency_amount", EconomyHandler.formatCurrency(totalItemValue))
                            .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));
                    player.sendMessage(EconomySettingsConfig.getShopCurrentBalance()
                            .replace("$currency_amount", EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId())))
                            .replace("$currency_name", EconomySettingsConfig.getCurrencyName()));
                }
                if (inventories.contains(shopInventory))
                    shopInventory.setItem(layout.confirm(), updateConfirmButton(session, calculateShopValue(shopInventory, player)));
                return;
            }

            //cancel, transfer items back to player inv and exit
            if (event.getSlot() == layout.cancel()) {
                event.getWhoClicked().closeInventory();
                return;
            }

            //If player clicks on a border glass pane, do nothing
            if (!slotsFor(shopInventory).contains(event.getSlot())) return;


            //If player clicks on one of the items already in the shop, return to their inventory
            moveItemDown(shopInventory, event.getSlot(), player);

            event.getInventory().setItem(layout.confirm(), updateConfirmButton(session, calculateShopValue(shopInventory, player)));

        }
    }


    private static void returnUnsold(Player player, ItemStack item) {
        for (ItemStack overflow : player.getInventory().addItem(item).values()) {
            var dropped = player.getWorld().dropItem(player.getLocation(), overflow);
            dropped.setOwner(player.getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) close(player, event.getInventory());
    }

}
