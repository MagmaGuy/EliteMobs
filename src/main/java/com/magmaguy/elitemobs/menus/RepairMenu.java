package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.menus.premade.RepairMenuConfig;
import com.magmaguy.elitemobs.items.ItemConsumables;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RepairMenu extends EliteMenu {
    private static final Map<Inventory, RecipeLayout> layouts = new IdentityHashMap<>();
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
        EliteMenu.cancel(player, inventory, player.getInventory(), List.of(layout.item(), layout.consumable()));
        inventory.clear(layout.output());
        layouts.remove(inventory);
    }

    private static void calculateOutput(Inventory repairInventory) {
        RecipeLayout layout = layouts.get(repairInventory);
        if (layout == null) return;
        if (repairInventory.getItem(layout.consumable()) == null || repairInventory.getItem(layout.item()) == null) {
            repairInventory.setItem(layout.output(), null);
            return;
        }

        int scrapLevel = ItemConsumables.repairTier(repairInventory.getItem(layout.consumable()));
        ItemStack outputItem = repairInventory.getItem(layout.item()).clone();
        int baselineRepair = 100;
        int newDamage = baselineRepair * scrapLevel;
        Damageable damageable = (Damageable) outputItem.getItemMeta();
        int damage = Math.min(Math.max((damageable.getDamage() - newDamage), 0), damageable.getDamage());
        damageable.setDamage(damage);
        outputItem.setItemMeta(damageable);
        repairInventory.setItem(layout.output(), outputItem);
    }

    /**
     * Creates a menu for scrapping elitemobs items. Only special Elite Mob items can be scrapped here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructRepairMenu(Player player) {
        RecipeLayout layout = new RecipeLayout(RepairMenuConfig.eliteItemInputSlot, RepairMenuConfig.eliteScrapInputSlot, RepairMenuConfig.outputSlot,
                RepairMenuConfig.eliteItemInputInformationSlot, RepairMenuConfig.eliteScrapInputInformationSlot, RepairMenuConfig.outputInformationSlot,
                RepairMenuConfig.infoSlot, RepairMenuConfig.cancelSlot, RepairMenuConfig.confirmSlot);
        String menuName = RepairMenuConfig.shopName;
        if (DefaultConfig.useResourcePackModels())
            menuName = ChatColor.WHITE + "\uDB83\uDEF1\uDB83\uDE01\uDB83\uDEF5           " + menuName;

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
                repairInventory.setItem(i, RepairMenuConfig.eliteScrapInputInfoButton);
                continue;
            }

            if (i == layout.outputInfo()) {
                repairInventory.setItem(i, RepairMenuConfig.outputInfoButton);
                continue;
            }


            if (i == layout.confirm()) {

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
                //Item is scrap
                if (ItemConsumables.is(currentItem, ItemConsumables.Type.REPAIR_SCRAP) && repairInventory.getItem(layout.consumable()) == null) {
                    int scrapLevel = ItemConsumables.repairTier(currentItem);
                    if (scrapLevel >= 0) {
                        moveOneItemUp(layout.consumable(), event);
                        calculateOutput(repairInventory);
                        return;
                    }
                }

                //Item is elite item
                if (EliteItemManager.isEliteMobsItem(currentItem))
                    if (currentItem.getItemMeta() instanceof Damageable)
                        if (repairInventory.getItem(layout.item()) == null) {
                            repairInventory.setItem(layout.item(), currentItem);
                            playerInventory.clear(event.getSlot());
                            calculateOutput(repairInventory);
                        }

            } else if (isTopMenu(event)) {

                if (currentItem == null) return;

                //return item to inventory
                if (event.getSlot() == layout.consumable() || event.getSlot() == layout.item()) {
                    HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(currentItem);
                    leftOvers.values().forEach(leftOver -> player.getWorld().dropItem(player.getLocation(), leftOver));
                    repairInventory.remove(currentItem);
                    calculateOutput(repairInventory);
                    return;
                }

                //cancel button
                if (event.getSlot() == layout.cancel()) {
                    player.closeInventory();
                    return;
                }

                //confirm button
                if (event.getSlot() == layout.confirm()) {
                    if (repairInventory.getItem(layout.output()) != null) {
                        repairInventory.setItem(layout.item(), null);
                        repairInventory.setItem(layout.consumable(), null);
                        if (repairInventory.getItem(layout.output()) != null) {
                            ItemStack outputItem = repairInventory.getItem(layout.output());
                            HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(outputItem);
                            leftOvers.values().forEach(leftOver -> player.getWorld().dropItem(player.getLocation(), leftOver));
                            repairInventory.remove(outputItem);
                        }
                        repairInventory.setItem(layout.output(), null);
                    }
                }

            }

        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (event.getPlayer() instanceof Player player) close(player, event.getInventory());
        }
    }
}
