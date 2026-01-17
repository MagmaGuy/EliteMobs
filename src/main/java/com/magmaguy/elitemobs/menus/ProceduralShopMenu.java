package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.BuyOrSellMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.ProceduralShopMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.GearRestrictionHandler;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.items.itemconstructor.MaterialGenerator;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
        if (DefaultConfig.useResourcePackModels())
            menuName = ChatColor.WHITE + "\uDB83\uDEF1\uDB83\uDE06\uDB83\uDEF5          " + menuName;
        Inventory shopInventory = Bukkit.createInventory(player, 54, menuName);
        populateShop(shopInventory, player);
        player.openInventory(shopInventory);
        ProceduralShopMenuEvents.menus.add(shopInventory);

    }

    private static void populateShop(Inventory shopInventory, Player player) {

        ItemStack rerollButton = ProceduralShopMenuConfig.rerollItem;
        if (DefaultConfig.useResourcePackModels()) {
            rerollButton.setType(Material.PAPER);
            CustomModelAdder.addCustomModel(rerollButton, CustomModelsConfig.goldenQuestionMark);
        }

        shopInventory.setItem(ProceduralShopMenuConfig.rerollSlot, rerollButton);
        shopContents(shopInventory, player);

    }

    private static void shopContents(Inventory shopInventory, Player player) {
        for (int i : validSlots) {
            // Generate a random material first
            Material material = MaterialGenerator.generateRandomMaterial();
            if (material == null) continue;

            // Determine the skill type for this material
            SkillType skillType = SkillType.fromMaterialIncludingArmor(material);

            // Calculate item level based on player's skill level for this item type
            int itemLevel = calculateItemLevel(player, skillType);

            // Construct item with the specific material and calculated level
            ItemStack itemStack = ItemConstructor.constructItemWithMaterial(material, itemLevel, player, true);
            if (itemStack == null) continue;

            new EliteItemLore(itemStack, true);
            shopInventory.setItem(i, itemStack);
        }
    }

    /**
     * Calculates the appropriate item level for a shop item based on the player's skill level.
     * Items are generated between (skillLevel - 5) and skillLevel, with min 1 and max 100.
     *
     * @param player    The player viewing the shop
     * @param skillType The skill type of the item (may be null for non-elite materials)
     * @return The calculated item level
     */
    private static int calculateItemLevel(Player player, SkillType skillType) {
        int playerSkillLevel;

        if (skillType != null) {
            // Get player's skill level for this specific skill type
            long skillXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
            playerSkillLevel = SkillXPCalculator.levelFromTotalXP(skillXP);
        } else {
            // For items without a skill type, use the player's average skill level
            playerSkillLevel = getAverageSkillLevel(player);
        }

        // Calculate level range: (skillLevel - 5) to skillLevel
        int minLevel = Math.max(1, playerSkillLevel - 5);
        int maxLevel = Math.min(100, playerSkillLevel);

        // Ensure minLevel doesn't exceed maxLevel
        if (minLevel > maxLevel) minLevel = maxLevel;

        // Random level within the range
        if (minLevel == maxLevel) return minLevel;
        return ThreadLocalRandom.current().nextInt(minLevel, maxLevel + 1);
    }

    /**
     * Gets the average skill level across all skill types for a player.
     * Used as fallback for items without a specific skill type.
     */
    private static int getAverageSkillLevel(Player player) {
        long totalXP = 0;
        int count = 0;
        for (SkillType type : SkillType.values()) {
            totalXP += PlayerData.getSkillXP(player.getUniqueId(), type);
            count++;
        }
        if (count == 0) return 1;
        long averageXP = totalXP / count;
        return Math.max(1, SkillXPCalculator.levelFromTotalXP(averageXP));
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
                    // Check gear restriction before allowing purchase
                    if (!GearRestrictionHandler.canEquip(player, itemStack)) {
                        GearRestrictionHandler.sendRestrictionMessage(player, itemStack);
                        return;
                    }

                    // Player has enough money and meets skill requirements
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
