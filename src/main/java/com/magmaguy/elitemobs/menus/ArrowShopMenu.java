package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.menus.premade.ArrowShopMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.magmacore.util.ChatColorConverter;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.*;

/**
 * Arrow Shop menu for purchasing arrows with Elite Coins.
 */
public class ArrowShopMenu {

    private static final Map<Integer, ArrowItem> slotToArrow = new HashMap<>();

    static {
        // Initialize arrow items with their slots
        slotToArrow.put(10, new ArrowItem(Material.ARROW,
                ArrowShopMenuConfig.getArrowSmallStackAmount(),
                ArrowShopMenuConfig.getArrowSmallStackPrice(),
                null, "&fArrows"));

        slotToArrow.put(11, new ArrowItem(Material.ARROW,
                ArrowShopMenuConfig.getArrowLargeStackAmount(),
                ArrowShopMenuConfig.getArrowLargeStackPrice(),
                null, "&fArrows"));

        slotToArrow.put(12, new ArrowItem(Material.SPECTRAL_ARROW,
                ArrowShopMenuConfig.getSpectralArrowAmount(),
                ArrowShopMenuConfig.getSpectralArrowPrice(),
                null, "&eSpectral Arrows"));

        slotToArrow.put(14, new ArrowItem(Material.TIPPED_ARROW,
                ArrowShopMenuConfig.getTippedArrowPoisonAmount(),
                ArrowShopMenuConfig.getTippedArrowPoisonPrice(),
                PotionType.POISON, "&2Arrow of Poison"));

        slotToArrow.put(15, new ArrowItem(Material.TIPPED_ARROW,
                ArrowShopMenuConfig.getTippedArrowSlownessAmount(),
                ArrowShopMenuConfig.getTippedArrowSlownessPrice(),
                PotionType.SLOWNESS, "&8Arrow of Slowness"));

        slotToArrow.put(16, new ArrowItem(Material.TIPPED_ARROW,
                ArrowShopMenuConfig.getTippedArrowWeaknessAmount(),
                ArrowShopMenuConfig.getTippedArrowWeaknessPrice(),
                PotionType.WEAKNESS, "&7Arrow of Weakness"));

        slotToArrow.put(21, new ArrowItem(Material.TIPPED_ARROW,
                ArrowShopMenuConfig.getTippedArrowHarmingAmount(),
                ArrowShopMenuConfig.getTippedArrowHarmingPrice(),
                PotionType.HARMING, "&4Arrow of Harming"));

        slotToArrow.put(23, new ArrowItem(Material.TIPPED_ARROW,
                ArrowShopMenuConfig.getTippedArrowHealingAmount(),
                ArrowShopMenuConfig.getTippedArrowHealingPrice(),
                PotionType.HEALING, "&dArrow of Healing"));
    }

    /**
     * Opens the arrow shop for a player.
     */
    public static void openArrowShop(Player player) {
        String menuName = ChatColorConverter.convert(ArrowShopMenuConfig.getShopName());
        if (DefaultConfig.useResourcePackModels()) {
            menuName = ChatColor.WHITE + "\uDB83\uDEF1\uDB83\uDE06\uDB83\uDEF5          " + menuName;
        }

        Inventory inventory = Bukkit.createInventory(player, 27, menuName);

        // Populate the shop with arrow items
        for (Map.Entry<Integer, ArrowItem> entry : slotToArrow.entrySet()) {
            inventory.setItem(entry.getKey(), createShopItem(entry.getValue()));
        }

        player.openInventory(inventory);
        ArrowShopMenuEvents.menus.add(inventory);
    }

    /**
     * Creates a shop item with price info in the lore.
     */
    private static ItemStack createShopItem(ArrowItem arrowItem) {
        ItemStack item;

        if (arrowItem.potionType != null) {
            item = new ItemStack(arrowItem.material, arrowItem.amount);
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            meta.setBasePotionType(arrowItem.potionType);
            meta.setDisplayName(ChatColorConverter.convert(arrowItem.displayName));
            meta.setLore(List.of(
                    "",
                    ChatColorConverter.convert("&7Amount: &f" + arrowItem.amount),
                    ChatColorConverter.convert("&7Price: &6" + arrowItem.price + " &7Elite Coins"),
                    "",
                    ChatColorConverter.convert("&eClick to purchase!")
            ));
            item.setItemMeta(meta);
        } else {
            item = new ItemStack(arrowItem.material, arrowItem.amount);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColorConverter.convert(arrowItem.displayName));
            meta.setLore(List.of(
                    "",
                    ChatColorConverter.convert("&7Amount: &f" + arrowItem.amount),
                    ChatColorConverter.convert("&7Price: &6" + arrowItem.price + " &7Elite Coins"),
                    "",
                    ChatColorConverter.convert("&eClick to purchase!")
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Creates the actual arrow item to give to the player (without shop lore).
     */
    private static ItemStack createArrowItem(ArrowItem arrowItem) {
        ItemStack item;

        if (arrowItem.potionType != null) {
            item = new ItemStack(arrowItem.material, arrowItem.amount);
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            meta.setBasePotionType(arrowItem.potionType);
            item.setItemMeta(meta);
        } else {
            item = new ItemStack(arrowItem.material, arrowItem.amount);
        }

        return item;
    }

    public static void shutdown() {
        ArrowShopMenuEvents.menus.clear();
    }

    /**
     * Data class for arrow items in the shop.
     */
    private record ArrowItem(Material material, int amount, int price, PotionType potionType, String displayName) {
    }

    /**
     * Event handler for the arrow shop menu.
     */
    public static class ArrowShopMenuEvents implements Listener {
        static final Set<Inventory> menus = new HashSet<>();

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!menus.contains(event.getInventory())) return;
            if (event.getClickedInventory() == null || !event.getClickedInventory().getType().equals(InventoryType.CHEST)) {
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player player)) return;

            ArrowItem arrowItem = slotToArrow.get(event.getSlot());
            if (arrowItem == null) return;

            // Check if player has enough money
            double playerBalance = EconomyHandler.checkCurrency(player.getUniqueId());
            if (playerBalance < arrowItem.price) {
                player.sendMessage(ChatColorConverter.convert(
                        ArrowShopMenuConfig.getInsufficientFundsMessage()
                                .replace("%price%", String.valueOf(arrowItem.price))
                ));
                return;
            }

            // Check if player has inventory space
            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(ChatColorConverter.convert(ArrowShopMenuConfig.getInventoryFullMessage()));
                return;
            }

            // Process purchase
            EconomyHandler.subtractCurrency(player.getUniqueId(), arrowItem.price);
            player.getInventory().addItem(createArrowItem(arrowItem));

            player.sendMessage(ChatColorConverter.convert(
                    ArrowShopMenuConfig.getPurchaseSuccessMessage()
                            .replace("%amount%", String.valueOf(arrowItem.amount))
                            .replace("%item%", arrowItem.displayName.replace("&", "\u00A7"))
                            .replace("%price%", String.valueOf(arrowItem.price))
            ));
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            menus.remove(event.getInventory());
        }
    }
}
