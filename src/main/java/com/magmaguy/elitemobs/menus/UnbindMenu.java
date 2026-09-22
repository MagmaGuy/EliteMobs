package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.menus.premade.UnbinderMenuConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

    private static void calculateOutput(Inventory unbinderInventory) {
        RecipeLayout layout = layouts.get(unbinderInventory);
        if (layout == null) return;
        if (unbinderInventory.getItem(layout.consumable()) == null ||
                unbinderInventory.getItem(layout.item()) == null) {
            unbinderInventory.setItem(layout.output(), null);
            return;
        }
        ItemStack outputItem = unbinderInventory.getItem(layout.item()).clone();
        unbinderInventory.setItem(layout.output(), ItemConsumables.unbind(outputItem));
    }

    /**
     * Creates a menu for scrapping elitemobs items. Only special Elite Mob items can be scrapped here.
     *
     * @param player Player for whom the inventory will be created
     */
    public void constructUnbinderMenu(Player player) {
        RecipeLayout layout = new RecipeLayout(UnbinderMenuConfig.getEliteItemInputSlot(), UnbinderMenuConfig.getEliteUnbindInputSlot(), UnbinderMenuConfig.getOutputSlot(),
                UnbinderMenuConfig.getEliteItemInputInformationSlot(), UnbinderMenuConfig.getEliteScrapInputInformationSlot(), UnbinderMenuConfig.getOutputInformationSlot(),
                UnbinderMenuConfig.getInfoSlot(), UnbinderMenuConfig.getCancelSlot(), UnbinderMenuConfig.getConfirmSlot());
        String menuName = UnbinderMenuConfig.getShopName();
        if (DefaultConfig.useResourcePackModels())
            menuName = ChatColor.WHITE + "\uDB83\uDEF1\uDB83\uDE09\uDB83\uDEF5          " + menuName;
        Inventory unbinderInventory = Bukkit.createInventory(player, 54, menuName);

        for (int i = 0; i < unbinderInventory.getSize(); i++) {

            if (i == layout.info()) {
                ItemStack infoButton = UnbinderMenuConfig.getInfoButton().clone();
                if (DefaultConfig.useResourcePackModels()) {
                    infoButton.setType(Material.PAPER);
                    ItemMeta itemMeta = infoButton.getItemMeta();
                    if (!VersionChecker.serverVersionOlderThan(21, 4))
                        itemMeta.setItemModel(NamespacedKey.fromString("elitemobs:ui/goldenquestionmark"));
                    infoButton.setItemMeta(itemMeta);
                }
                unbinderInventory.setItem(i, infoButton);
                continue;
            }

            if (i == layout.cancel()) {
                unbinderInventory.setItem(i, UnbinderMenuConfig.getCancelButton());
                continue;
            }

            if (i == layout.itemInfo()) {
                unbinderInventory.setItem(i, UnbinderMenuConfig.getEliteItemInputInfoButton());
                continue;
            }

            if (i == layout.consumableInfo()) {
                unbinderInventory.setItem(i, UnbinderMenuConfig.getEliteUnbindInputInfoButton());
                continue;
            }

            if (i == layout.outputInfo()) {
                unbinderInventory.setItem(i, UnbinderMenuConfig.getOutputInfoButton());
                continue;
            }


            if (i == layout.confirm()) {

                ItemStack clonedConfirmButton = UnbinderMenuConfig.getConfirmButton().clone();

                List<String> lore = new ArrayList<>();
                for (String string : UnbinderMenuConfig.getConfirmButton().getItemMeta().getLore())
                    lore.add(string);
                UnbinderMenuConfig.getConfirmButton().getItemMeta().setLore(lore);
                ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
                clonedMeta.setLore(lore);
                clonedConfirmButton.setItemMeta(clonedMeta);
                unbinderInventory.setItem(i, clonedConfirmButton);
                continue;

            }


            if (i == layout.item() || i == layout.consumable() || i == layout.output())
                continue;
            if (DefaultConfig.isUseGlassToFillMenuEmptySpace())
                unbinderInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));

        }

        layouts.put(unbinderInventory, layout);
        createEliteMenu(unbinderInventory, inventories);
        try {
            player.openInventory(unbinderInventory);
        } finally {
            if (player.getOpenInventory().getTopInventory() != unbinderInventory) close(player, unbinderInventory);
        }
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
            RecipeLayout layout = layouts.get(unbinderInventory);
            if (layout == null) return;
            Inventory playerInventory = event.getView().getBottomInventory();

            if (currentItem == null) return;

            if (isBottomMenu(event)) {
                //Item is unbind scroll
                if (ItemConsumables.is(currentItem, ItemConsumables.Type.UNBIND) && SoulbindEnchantment.isValidSoulbindUser(currentItem.getItemMeta(), player)) {
                    if (unbinderInventory.getItem(layout.consumable()) == null) {
                        moveOneItemUp(layout.consumable(), event);
                        calculateOutput(unbinderInventory);
                    }
                    return;
                }

                //Item is elite item
                if (EliteItemManager.isEliteMobsItem(currentItem))
                    if (currentItem.getItemMeta() instanceof Damageable)
                        if (unbinderInventory.getItem(layout.item()) == null) {
                            unbinderInventory.setItem(layout.item(), currentItem);
                            playerInventory.clear(event.getSlot());
                            calculateOutput(unbinderInventory);
                        }

            } else if (isTopMenu(event)) {

                if (currentItem == null) return;

                //return item to inventory
                if (event.getSlot() == layout.consumable() || event.getSlot() == layout.item()) {
                    moveItemDown(event.getView().getTopInventory(), clickedSlot, player);
                    calculateOutput(unbinderInventory);
                    return;
                }

                //cancel button
                if (event.getSlot() == layout.cancel()) {
                    player.closeInventory();
                    return;
                }

                //confirm button
                if (event.getSlot() == layout.confirm()) {
                    if (unbinderInventory.getItem(layout.output()) != null) {
                        unbinderInventory.setItem(layout.item(), null);
                        unbinderInventory.setItem(layout.consumable(), null);
                        moveItemDown(unbinderInventory, layout.output(), player);
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
