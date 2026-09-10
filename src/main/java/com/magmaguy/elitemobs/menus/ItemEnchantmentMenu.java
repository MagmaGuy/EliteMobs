package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.SpecialItemSystemsConfig;
import com.magmaguy.elitemobs.config.menus.premade.ItemEnchantmentMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.instanced.dungeons.EnchantmentDungeonInstance;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.items.upgradesystem.EliteEnchantmentItems;
import com.magmaguy.elitemobs.items.upgradesystem.UpgradeSystem;
import com.magmaguy.elitemobs.items.upgradesystem.EnchantmentAcquisition;
import com.magmaguy.elitemobs.items.upgradesystem.EnchantmentProgression;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Round;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ItemEnchantmentMenu extends EliteMenu {

    private static final String MENU_NAME = ItemEnchantmentMenuConfig.getMenuName();
    private static final int CANCEL_SLOT = ItemEnchantmentMenuConfig.getCancelSlot();
    private static final ItemStack cancelButton = ItemEnchantmentMenuConfig.getCancelButton();
    private static final int CONFIRM_SLOT = ItemEnchantmentMenuConfig.getConfirmSlot();
    private static final ItemStack confirmButton = ItemEnchantmentMenuConfig.getConfirmButton();
    private static final int INFO_SLOT = ItemEnchantmentMenuConfig.getInfoSlot();
    private static final ItemStack infoButton = ItemEnchantmentMenuConfig.getInfoButton();
    private static final int ITEM_SLOT = ItemEnchantmentMenuConfig.getItemSlot();
    private static final int ENCHANTED_BOOK_SLOT = ItemEnchantmentMenuConfig.getEnchantedBookSlot();
    private static final int ITEM_INFO_SLOT = ItemEnchantmentMenuConfig.getItemInfoSlot();
    private static final ItemStack itemInfoItemButton = ItemEnchantmentMenuConfig.getItemInfoButton();
    private static final int ENCHANTED_BOOK_INFO_SLOT = ItemEnchantmentMenuConfig.getEnchantedBookInfoSlot();
    private static final ItemStack enchantedBookInfoButton = ItemEnchantmentMenuConfig.getEnchantedBookInfoButton();
    private static final List<String> confirmButtonLore = confirmButton.getItemMeta().getLore();
    private static final int LUCKY_TICKET_SLOT = ItemEnchantmentMenuConfig.getLuckyTicketSlot();
    private static final int LUCKY_TICKET_INFO_SLOT = ItemEnchantmentMenuConfig.getLuckyTicketInfoSlot();
    private static final ItemStack luckyTicketInfoButton = ItemEnchantmentMenuConfig.getLuckyTicketInfoButton();

    public ItemEnchantmentMenu(Player player) {
        String name = MENU_NAME;
        if (DefaultConfig.useResourcePackModels())
            name = ChatColor.WHITE + "\uDB83\uDEF1\uDB83\uDE01\uDB83\uDEF5           " + MENU_NAME;
        Inventory inventory = Bukkit.createInventory(player, 54, name);
        ItemEnchantMenuEvents.menus.add(inventory);

        inventory.setItem(INFO_SLOT, infoButton);
        inventory.setItem(ENCHANTED_BOOK_INFO_SLOT, enchantedBookInfoButton);
        inventory.setItem(ITEM_INFO_SLOT, itemInfoItemButton);
        inventory.setItem(CANCEL_SLOT, cancelButton);
        inventory.setItem(CONFIRM_SLOT, confirmButton);
        inventory.setItem(LUCKY_TICKET_INFO_SLOT, luckyTicketInfoButton);

        updateConfirmButton(inventory);

        player.openInventory(inventory);
    }

    private static void updateConfirmButton(Inventory inventory) {
        ItemStack newButton = confirmButton.clone();
        ItemMeta itemMeta = newButton.getItemMeta();
        List<String> newLore = new ArrayList<>();
        try {
            var item = inventory.getItem(ITEM_SLOT);
            var quote = EnchantmentProgression.quote(item, inventory.getItem(LUCKY_TICKET_SLOT) != null);
            for (String string : confirmButtonLore)
                newLore.add(string
                        .replace("$price", EconomyHandler.formatCurrency(item == null ? 0 : quote.price()))
                        .replace("$currencyName", EconomySettingsConfig.getCurrencyName())
                        .replace("$successChance", Double.toString(Round.twoDecimalPlaces(quote.success() * 100)))
                        .replace("$criticalFailureChance", Double.toString(Round.twoDecimalPlaces(quote.criticalFailure() * 100)))
                        .replace("$challengeChance", Double.toString(Round.twoDecimalPlaces(quote.challenge() * 100)))
                        .replace("$failureChance", Double.toString(Round.twoDecimalPlaces(quote.failure() * 100))));
        } catch (RuntimeException invalid) {
            newLore.add(ChatColor.RED + "This item or the enchantment settings are invalid.");
        }
        itemMeta.setLore(newLore);
        newButton.setItemMeta(itemMeta);
        inventory.setItem(CONFIRM_SLOT, newButton);
    }

    public static void broadcastEnchantmentMessage(ItemStack upgradedItem, Player upgradingPlayer, String message) {
        if (SpecialItemSystemsConfig.isAnnounceImportantEnchantments() && EnchantmentProgression.weight(upgradedItem) > 10)
            if (!message.contains("$itemName"))
                Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(
                        ChatMessageType.CHAT, TextComponent.fromLegacyText(
                                ChatColorConverter.convert(message.replace("$playerName", upgradingPlayer.getName()).replace("$player", upgradingPlayer.getDisplayName())))));
            else {
                TextComponent itemName = ShareItem.hoverableItemTextComponent(upgradedItem);
                String[] text = message.replace("$itemName", "itemName").split("itemName");
                BaseComponent[] baseComponent1 = TextComponent.fromLegacyText(ChatColorConverter.convert(text[0].replace("$playerName", upgradingPlayer.getName()).replace("$player", upgradingPlayer.getDisplayName())));
                BaseComponent[] baseComponent2 = TextComponent.fromLegacyText(ChatColorConverter.convert(text[1].replace("$playerName", upgradingPlayer.getName()).replace("$player", upgradingPlayer.getDisplayName())));

                ComponentBuilder componentBuilder = new ComponentBuilder();
                componentBuilder.append(baseComponent1);
                componentBuilder.append(itemName);
                componentBuilder.append(baseComponent2);

                Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.CHAT, componentBuilder.create()));
            }
    }

    private enum Chance {
        SUCCESS,
        CHALLENGE,
        FAILURE,
        CRITICAL_FAILURE
    }

    public static class ItemEnchantMenuEvents implements Listener {
        private static final Set<Inventory> menus = new HashSet<>();
        private static final Set<Inventory> processing = new HashSet<>();
        private final java.util.function.DoubleSupplier outcomeRandom;

        public ItemEnchantMenuEvents() {
            this(() -> ThreadLocalRandom.current().nextDouble());
        }

        ItemEnchantMenuEvents(java.util.function.DoubleSupplier outcomeRandom) {
            this.outcomeRandom = Objects.requireNonNull(outcomeRandom);
        }

        public static void shutdown() {
            for (Inventory inventory : new ArrayList<>(menus)) {
                menus.remove(inventory);
                for (var viewer : new ArrayList<>(inventory.getViewers())) {
                    EliteMenu.cancel(viewer, inventory, viewer.getInventory(), List.of(ITEM_SLOT, ENCHANTED_BOOK_SLOT, LUCKY_TICKET_SLOT));
                    viewer.closeInventory();
                }
            }
            menus.clear();
            processing.clear();
        }

        @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            if (!EliteMenu.isEliteMenu(event, menus)) return;
            event.setCancelled(true);
            if (processing.contains(event.getView().getTopInventory())) return;
            if (!SharedShopElements.itemNullPointerPrevention(event)) return;

            if (isTopMenu(event)) {
                handleTopInventory(event);
            } else {
                handleBottomInventory(event);
            }

            if (event.getInventory().getItem(ENCHANTED_BOOK_SLOT) != null &&
                    event.getInventory().getItem(ITEM_SLOT) != null &&
                    !UpgradeSystem.isValidUpgrade(event.getView().getTopInventory().getItem(ITEM_SLOT),
                            event.getView().getTopInventory().getItem(ENCHANTED_BOOK_SLOT))) {
                event.getWhoClicked().sendMessage(UpgradeSystem.isCompatibleBook(
                        event.getInventory().getItem(ITEM_SLOT), event.getInventory().getItem(ENCHANTED_BOOK_SLOT))
                        ? ItemEnchantmentMenuConfig.getEnchantmentLimitMessage()
                        : ItemEnchantmentMenuConfig.getIncompatibleEnchantmentMessage());
                event.getWhoClicked().closeInventory();
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event) {
            if (!menus.contains(event.getView().getTopInventory())) return;
            int topInventorySize = event.getView().getTopInventory().getSize();
            for (int rawSlot : event.getRawSlots())
                if (rawSlot < topInventorySize) {
                    event.setCancelled(true);
                    return;
                }
        }

        private void handleTopInventory(InventoryClickEvent event) {
            int clickedSlot = event.getSlot();

            if (clickedSlot == CANCEL_SLOT) event.getWhoClicked().closeInventory();
            else if (clickedSlot == CONFIRM_SLOT) confirm(event);
            else if (clickedSlot == ITEM_SLOT || clickedSlot == ENCHANTED_BOOK_SLOT || clickedSlot == LUCKY_TICKET_SLOT) {
                moveItemDown(event.getView().getTopInventory(), clickedSlot, event.getWhoClicked());
                event.getClickedInventory().clear(clickedSlot);
                if (clickedSlot == ITEM_SLOT && event.getView().getTopInventory().getItem(ENCHANTED_BOOK_SLOT) != null)
                    moveItemDown(event.getView().getTopInventory(), ENCHANTED_BOOK_SLOT, event.getWhoClicked());
                updateConfirmButton(event.getInventory());
            }

        }

        private void handleBottomInventory(InventoryClickEvent event) {
            if (EliteEnchantmentItems.isEliteEnchantmentBook(event.getCurrentItem())) {
                if (event.getInventory().getItem(ENCHANTED_BOOK_SLOT) == null)
                    moveOneItemUp(ENCHANTED_BOOK_SLOT, event);
                else
                //Make sure enchant books themselves can't be enchanted
                {
                }
            } else if (EliteEnchantmentItems.isEliteLuckyTicket(event.getCurrentItem()) &&
                    event.getView().getTopInventory().getItem(LUCKY_TICKET_SLOT) == null) {
                moveOneItemUp(LUCKY_TICKET_SLOT, event);
                updateConfirmButton(event.getInventory());
            } else if (ItemTagger.isEliteItem(event.getCurrentItem()) &&
                    event.getInventory().getItem(ITEM_SLOT) == null) {
                moveOneItemUp(ITEM_SLOT, event);
                updateConfirmButton(event.getInventory());
            }
        }

        private void confirm(InventoryClickEvent event) {
            Inventory inventory = event.getView().getTopInventory();
            if (!processing.add(inventory)) return;
            EnchantmentAcquisition attempt = null;
            Player player = (Player) event.getWhoClicked();
            try {
                ItemStack input = inventory.getItem(ITEM_SLOT);
                ItemStack book = inventory.getItem(ENCHANTED_BOOK_SLOT);
                if (input == null || book == null) {
                    player.sendMessage(ItemEnchantmentMenuConfig.getMissingItemsMessage());
                    return;
                }
                attempt = new EnchantmentAcquisition(player, input, book, inventory.getItem(LUCKY_TICKET_SLOT));
                if (!attempt.purchase(inventory, ITEM_SLOT, ENCHANTED_BOOK_SLOT, LUCKY_TICKET_SLOT,
                        () -> menus.contains(inventory) && player.getOpenInventory().getTopInventory() == inventory)) {
                    player.sendMessage(ChatColor.RED + "The enchantment purchase was not completed.");
                    return;
                }
                // Inputs are now exclusively owned by the attempt; close events cannot return them twice.
                player.closeInventory();
                switch (rollChance(attempt.quote())) {
                    case SUCCESS -> {
                        attempt.success();
                        player.sendMessage(DefaultConfig.getEnchantmentChallengeSuccessMessage());
                        broadcastEnchantmentMessage(attempt.upgraded(), player, SpecialItemSystemsConfig.getSuccessAnnouncement());
                    }
                    case CRITICAL_FAILURE -> {
                        attempt.criticalFailure();
                        player.sendMessage(DefaultConfig.getEnchantmentChallengeCriticalFailureMessage());
                        broadcastEnchantmentMessage(attempt.original(), player, SpecialItemSystemsConfig.getCriticalFailureAnnouncement());
                    }
                    case FAILURE -> {
                        attempt.failure();
                        player.sendMessage(DefaultConfig.getEnchantmentChallengeFailureMessage());
                    }
                    case CHALLENGE -> {
                        if (!EnchantmentDungeonInstance.setupRandomEnchantedChallengeDungeon(player, attempt)) {
                            attempt.success();
                            player.sendMessage(DefaultConfig.getEnchantmentChallengeSuccessMessage());
                            broadcastEnchantmentMessage(attempt.upgraded(), player, SpecialItemSystemsConfig.getSuccessAnnouncement());
                        } else if (attempt.isOwned()) {
                            player.sendMessage(DefaultConfig.getEnchantmentChallengeStartMessage());
                            player.sendMessage(DefaultConfig.getEnchantmentChallengeConsequencesMessage());
                            broadcastEnchantmentMessage(attempt.original(), player, SpecialItemSystemsConfig.getChallengeAnnouncement());
                        }
                    }
                }
            } catch (RuntimeException invalid) {
                if (attempt != null) attempt.abort("enchantment operation failed: " + invalid);
                player.sendMessage(ChatColor.RED + "The enchantment attempt could not be completed.");
                com.magmaguy.elitemobs.MetadataHandler.PLUGIN.getLogger().warning("Enchantment attempt failed: " + invalid);
            } finally {
                processing.remove(inventory);
            }
        }

        private Chance rollChance(EnchantmentProgression.Quote quote) {
            double rolled = outcomeRandom.getAsDouble();
            if (!Double.isFinite(rolled) || rolled < 0D || rolled >= 1D)
                throw new IllegalStateException("Invalid enchantment outcome sample");
            double threshold = quote.success();
            if (rolled < threshold) return Chance.SUCCESS;
            threshold += quote.criticalFailure();
            if (rolled < threshold) return Chance.CRITICAL_FAILURE;
            threshold += quote.challenge();
            if (rolled < threshold) return Chance.CHALLENGE;
            return Chance.FAILURE;
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (menus.contains(event.getInventory())) {
                menus.remove(event.getInventory());
                EliteMenu.cancel(event.getPlayer(), event.getView().getTopInventory(), event.getView().getBottomInventory(),
                        new ArrayList<>(List.of(ITEM_SLOT, ENCHANTED_BOOK_SLOT, LUCKY_TICKET_SLOT)));
            }
        }

    }
}
