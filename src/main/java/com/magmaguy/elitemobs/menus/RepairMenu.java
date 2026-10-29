package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.RepairMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.items.ItemDurability;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
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
    private static final Map<Inventory, RecipeLayout> layouts = new IdentityHashMap<>();
    private static final Set<Inventory> repairing = Collections.newSetFromMap(new IdentityHashMap<>());
    public static Set<Inventory> inventories = new HashSet<>();

    public static void shutdown() {
        for (Inventory inventory : List.copyOf(inventories)) {
            if (!(inventory.getHolder() instanceof Player player)) continue;
            close(player, inventory);
            if (player.getOpenInventory().getTopInventory() == inventory) player.closeInventory();
        }
    }

    private static void close(Player player, Inventory inventory) {
        RecipeLayout layout = layouts.get(inventory);
        if (layout == null || !inventories.remove(inventory)) return;
        // The cost and output slots only display; the item slot is the one input the player owns.
        EliteMenu.cancel(player, inventory, player.getInventory(), List.of(layout.item()));
        inventory.clear(layout.consumable());
        inventory.clear(layout.output());
        layouts.remove(inventory);
    }

    /**
     * Elite coins to fully repair an item: repairCostPerItemLevel x item level x share of durability missing,
     * rounded up. Returns -1 when there is nothing to repair and 0 when repairs are free.
     */
    public static int repairCost(ItemStack item) {
        if (item == null || !(item.getItemMeta() instanceof Damageable damageable)) return -1;
        int maximum = ItemDurability.maximum(item);
        if (maximum <= 0 || damageable.getDamage() <= 0) return -1;
        double perLevel = ItemSettingsConfig.getRepairCostPerItemLevel();
        if (!EconomySettingsConfig.isEnableEconomy() || !(perLevel > 0)) return 0;
        double missing = Math.min(1D, damageable.getDamage() / (double) maximum);
        double cost = Math.ceil(perLevel * EliteItemManager.getRoundedItemLevel(item) * missing);
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1D, cost));
    }

    private static double balance(Player player) {
        return EconomyHandler.isReady(player.getUniqueId()) ? EconomyHandler.checkCurrency(player.getUniqueId()) : 0;
    }

    private static void calculateOutput(Inventory repairInventory, Player player) {
        RecipeLayout layout = layouts.get(repairInventory);
        if (layout == null) return;
        ItemStack item = repairInventory.getItem(layout.item());
        int cost = repairCost(item);
        if (cost < 0) {
            repairInventory.setItem(layout.consumable(), null);
            repairInventory.setItem(layout.output(), null);
            return;
        }
        repairInventory.setItem(layout.consumable(), costButton(cost, balance(player)));
        repairInventory.setItem(layout.output(), repaired(item));
    }

    private static ItemStack repaired(ItemStack item) {
        ItemStack outputItem = item.clone();
        Damageable damageable = (Damageable) outputItem.getItemMeta();
        damageable.setDamage(0);
        outputItem.setItemMeta(damageable);
        return outputItem;
    }

    private static ItemStack costButton(int cost, double balance) {
        ItemStack button = RepairMenuConfig.repairCostButton.clone();
        ItemMeta meta = button.getItemMeta();
        if (meta == null) return button;
        if (meta.hasDisplayName()) meta.setDisplayName(fillCost(meta.getDisplayName(), cost, balance));
        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : meta.getLore()) lore.add(fillCost(line, cost, balance));
            meta.setLore(lore);
        }
        button.setItemMeta(meta);
        return button;
    }

    private static String fillCost(String text, int cost, double balance) {
        return text.replace("$price", String.valueOf(cost))
                .replace("$currencyName", EconomySettingsConfig.getCurrencyName())
                .replace("$currentAmount", EconomyHandler.formatCurrency(balance));
    }

    private static String itemName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() ? meta.getDisplayName() : item.getType().name();
    }

    private static void give(Player player, ItemStack item) {
        player.getInventory().addItem(item).values()
                .forEach(leftOver -> player.getWorld().dropItem(player.getLocation(), leftOver));
    }

    /** Repairs now cost coins, so scrap players still hold is paid out at its tier value. */
    private static void exchangeScrap(Player player, Inventory playerInventory, int slot, ItemStack scrap) {
        if (!EconomyHandler.isReady(player.getUniqueId())) {
            player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
            return;
        }
        ItemStack held = scrap.clone();
        int value = ItemConsumables.scrapCoinValue(held) * held.getAmount();
        // Own the stack before entering a provider which may reenter menu callbacks.
        playerInventory.clear(slot);
        boolean accepted;
        try {
            accepted = EconomyHandler.tryCredit(player.getUniqueId(), value);
        } catch (RuntimeException failure) {
            Logger.warn("Unconfirmed scrap exchange credit; retained scrap for " + player.getUniqueId()
                    + "; proceeds=" + value + "; item=" + held + "; " + failure);
            player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
            return;
        }
        if (!accepted) {
            if (playerInventory.getItem(slot) == null) playerInventory.setItem(slot, held);
            else give(player, held);
            player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
            return;
        }
        player.sendMessage(ItemSettingsConfig.getScrapExchangedMessage()
                .replace("$amount", String.valueOf(held.getAmount()))
                .replace("$price", String.valueOf(value))
                .replace("$currencyName", EconomySettingsConfig.getCurrencyName()));
    }

    private static void repair(Player player, Inventory repairInventory, RecipeLayout layout) {
        if (!repairing.add(repairInventory)) return;
        try {
            ItemStack item = repairInventory.getItem(layout.item());
            int cost = repairCost(item);
            if (cost < 0) return;
            UUID user = player.getUniqueId();
            if (cost > 0 && !EconomyHandler.isReady(user)) {
                player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
                return;
            }
            ItemStack original = item.clone();
            // Own the item before entering a provider which may reenter menu callbacks.
            repairInventory.setItem(layout.item(), null);
            repairInventory.setItem(layout.consumable(), null);
            repairInventory.setItem(layout.output(), null);
            boolean paid;
            try {
                paid = cost == 0 || EconomyHandler.tryWithdraw(user, cost);
            } catch (RuntimeException failure) {
                // The charge may or may not have gone through, so the item goes back unrepaired.
                Logger.warn("Repair payment needs inspection for " + user + "; price=" + cost
                        + "; item returned unrepaired=" + original + "; failure=" + failure);
                give(player, original);
                player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
                return;
            }
            if (!paid) {
                player.sendMessage(fillCost(ItemSettingsConfig.getRepairInsufficientFundsMessage(), cost, balance(player))
                        .replace("$itemName", itemName(original)));
                if (inventories.contains(repairInventory) && player.getOpenInventory().getTopInventory() == repairInventory
                        && repairInventory.getItem(layout.item()) == null) {
                    repairInventory.setItem(layout.item(), original);
                    calculateOutput(repairInventory, player);
                } else give(player, original);
                return;
            }
            give(player, repaired(original));
            if (cost > 0)
                player.sendMessage(fillCost(ItemSettingsConfig.getRepairPaidMessage(), cost, balance(player))
                        .replace("$itemName", itemName(original)));
        } finally {
            repairing.remove(repairInventory);
        }
    }

    /**
     * Creates the repair menu. Elite items are repaired for elite coins.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructRepairMenu(Player player) {
        RecipeLayout layout = new RecipeLayout(RepairMenuConfig.eliteItemInputSlot, RepairMenuConfig.repairCostSlot, RepairMenuConfig.outputSlot,
                RepairMenuConfig.eliteItemInputInformationSlot, RepairMenuConfig.repairCostInformationSlot, RepairMenuConfig.outputInformationSlot,
                RepairMenuConfig.infoSlot, RepairMenuConfig.cancelSlot, RepairMenuConfig.confirmSlot);
        String menuName = RepairMenuConfig.shopName;
        if (DefaultConfig.useResourcePackModels())
            menuName = ChatColor.WHITE + "󰻱󰸁󰻵           " + menuName;

        Inventory repairInventory = Bukkit.createInventory(player, 54, menuName);

        for (int i = 0; i < repairInventory.getSize(); i++) {

            if (i == layout.info()) {
                ItemStack infoButton = RepairMenuConfig.infoButton.clone();
                if (DefaultConfig.useResourcePackModels()) {
                    infoButton.setType(Material.PAPER);
                    ItemMeta itemMeta = infoButton.getItemMeta();
                    itemMeta.setCustomModelData(MetadataHandler.signatureID);
                    infoButton.setItemMeta(itemMeta);
                }
                repairInventory.setItem(i, infoButton);
                continue;
            }

            if (i == layout.cancel()) {
                repairInventory.setItem(i, RepairMenuConfig.cancelButton);
                continue;
            }

            if (i == layout.itemInfo()) {
                repairInventory.setItem(i, RepairMenuConfig.eliteItemInputInfoButton);
                continue;
            }

            if (i == layout.consumableInfo()) {
                repairInventory.setItem(i, RepairMenuConfig.repairCostInfoButton);
                continue;
            }

            if (i == layout.outputInfo()) {
                repairInventory.setItem(i, RepairMenuConfig.outputInfoButton);
                continue;
            }


            if (i == layout.confirm()) {
                repairInventory.setItem(i, RepairMenuConfig.confirmButton.clone());
                continue;
            }


            if (i == layout.item() || i == layout.consumable() || i == layout.output())
                continue;

            if (DefaultConfig.isUseGlassToFillMenuEmptySpace())
                repairInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        layouts.put(repairInventory, layout);
        createEliteMenu(repairInventory, inventories);
        try {
            player.openInventory(repairInventory);
        } finally {
            if (player.getOpenInventory().getTopInventory() != repairInventory) close(player, repairInventory);
        }
    }

    public static class RepairMenuEvents implements Listener {
        @EventHandler
        public void onInteract(InventoryClickEvent event) {
            if (!isEliteMenu(event, inventories)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            Inventory repairInventory = event.getView().getTopInventory();
            RecipeLayout layout = layouts.get(repairInventory);
            if (layout == null) return;
            Inventory playerInventory = event.getView().getBottomInventory();

            if (currentItem == null) return;

            if (isBottomMenu(event)) {
                //Item is old scrap, which is now worth coins
                if (ItemConsumables.scrapTier(currentItem) > 0) {
                    exchangeScrap(player, playerInventory, event.getSlot(), currentItem);
                    calculateOutput(repairInventory, player);
                    return;
                }

                //Item is elite item
                if (EliteItemManager.isEliteMobsItem(currentItem))
                    if (currentItem.getItemMeta() instanceof Damageable)
                        if (repairInventory.getItem(layout.item()) == null) {
                            repairInventory.setItem(layout.item(), currentItem);
                            playerInventory.clear(event.getSlot());
                            calculateOutput(repairInventory, player);
                        }

            } else if (isTopMenu(event)) {

                //return item to inventory
                if (event.getSlot() == layout.item()) {
                    moveItemDown(repairInventory, layout.item(), player);
                    calculateOutput(repairInventory, player);
                    return;
                }

                //cancel button
                if (event.getSlot() == layout.cancel()) {
                    player.closeInventory();
                    return;
                }

                //confirm button
                if (event.getSlot() == layout.confirm())
                    repair(player, repairInventory, layout);

            }

        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (event.getPlayer() instanceof Player player) close(player, event.getInventory());
        }
    }
}
