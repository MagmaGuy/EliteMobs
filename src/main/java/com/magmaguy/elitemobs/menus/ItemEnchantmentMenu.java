package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ResourcePackDataConfig;
import com.magmaguy.elitemobs.config.SpecialItemSystemsConfig;
import com.magmaguy.elitemobs.config.menus.premade.ItemEnchantmentMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.instanced.dungeons.EnchantmentDungeonInstance;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.items.upgradesystem.EliteEnchantmentItems;
import com.magmaguy.elitemobs.items.upgradesystem.UpgradeSystem;
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

    private static final double LUCKY_TICKET_MULTIPLIER = SpecialItemSystemsConfig.getLuckyTicketMultiplier();
    private static final double CRITICAL_FAILURE_CHANCE = SpecialItemSystemsConfig.getCriticalFailureChance();
    private static final double CHALLENGE_CHANCE = SpecialItemSystemsConfig.getChallengeChance();

    public ItemEnchantmentMenu(Player player) {
        String name = MENU_NAME;
        if (ResourcePackDataConfig.isDisplayCustomMenuUnicodes())
            name = ChatColor.WHITE + "\uF801\uDB80\uDC2A\uF805           " + MENU_NAME;
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
        EnumMap<Chance, Double> chances = getChanceBreakdown(inventory);
        for (String string : confirmButtonLore)
            newLore.add(string
                    .replace("$price", price(inventory) + "")
                    .replace("$currencyName", EconomySettingsConfig.getCurrencyName())
                    .replace("$successChance", (Round.twoDecimalPlaces(chances.get(Chance.SUCCESS) * 100)) + "")
                    .replace("$criticalFailureChance", (Round.twoDecimalPlaces(chances.get(Chance.CRITICAL_FAILURE) * 100)) + "")
                    .replace("$challengeChance", (Round.twoDecimalPlaces(chances.get(Chance.CHALLENGE) * 100)) + "")
                    .replace("$failureChance", (Round.twoDecimalPlaces(chances.get(Chance.FAILURE) * 100)) + ""));
        itemMeta.setLore(newLore);
        newButton.setItemMeta(itemMeta);
        inventory.setItem(CONFIRM_SLOT, newButton);
    }

    //Order goes  SUCCESS -> CRITICAL_FAILURE -> CHALLENGE -> FAILURE
    private static EnumMap<Chance, Double> getChanceBreakdown(Inventory inventory) {
        EnumMap<Chance, Double> chances = new EnumMap<>(Chance.class);
        boolean luckyTicket = inventory.getItem(LUCKY_TICKET_SLOT) != null;

        double currentTotal = 1d;
        double criticalFailure = CRITICAL_FAILURE_CHANCE;

        double success = successChance(inventory);
        currentTotal -= success;
        chances.put(Chance.SUCCESS, success);
        if (success == 1) {
            chances.put(Chance.SUCCESS, 1D);
            chances.put(Chance.CRITICAL_FAILURE, 0D);
            chances.put(Chance.CHALLENGE, 0D);
            chances.put(Chance.FAILURE, 0D);
            return chances;
        }

        if (luckyTicket) criticalFailure /= 2D;
        criticalFailure *= currentTotal;
        criticalFailure = Round.twoDecimalPlaces(criticalFailure);
        chances.put(Chance.CRITICAL_FAILURE, criticalFailure);

        currentTotal -= criticalFailure;
        double challenge = Round.twoDecimalPlaces(currentTotal * CHALLENGE_CHANCE);

        chances.put(Chance.CHALLENGE, challenge);

        currentTotal -= challenge;
        chances.put(Chance.FAILURE, Round.twoDecimalPlaces(currentTotal));

        return chances;
    }

    private static int price(Inventory inventory) {
        ItemStack itemStack = inventory.getItem(ITEM_SLOT);
        if (itemStack == null) return 0;
        return (int) Math.pow(ItemTagger.getEnchantmentCount(itemStack) + 1D, 4);
    }

    private static double successChance(Inventory inventory) {
        ItemStack itemStack = inventory.getItem(ITEM_SLOT);
        if (itemStack == null) return 0;
        double chance = 100.0 / (ItemTagger.getEnchantmentCount(itemStack) + 1.0) * 4;
        if (inventory.getItem(LUCKY_TICKET_SLOT) != null) chance *= LUCKY_TICKET_MULTIPLIER;
        return Round.twoDecimalPlaces(Math.min(1, chance / 100));
    }

    public static void broadcastEnchantmentMessage(ItemStack upgradedItem, Player upgradingPlayer, String message) {
        if (SpecialItemSystemsConfig.isAnnounceImportantEnchantments() && ItemTagger.getEnchantmentCount(upgradedItem) > 10)
            if (!message.contains("$itemName"))
                Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(
                        ChatMessageType.CHAT, TextComponent.fromLegacyText(
                                ChatColorConverter.convert(message.replace("$player", upgradingPlayer.getDisplayName())))));
            else {
                TextComponent itemName = ShareItem.hoverableItemTextComponent(upgradedItem);
                String[] text = message.replace("$itemName", "itemName").split("itemName");
                BaseComponent[] baseComponent1 = TextComponent.fromLegacyText(ChatColorConverter.convert(text[0].replace("$player", upgradingPlayer.getDisplayName())));
                BaseComponent[] baseComponent2 = TextComponent.fromLegacyText(ChatColorConverter.convert(text[1].replace("$player", upgradingPlayer.getDisplayName())));

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

        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            if (!EliteMenu.isEliteMenu(event, menus)) return;
            event.setCancelled(true);
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
                event.getWhoClicked().sendMessage(ItemEnchantmentMenuConfig.getEnchantmentLimitMessage());
                event.getWhoClicked().closeInventory();
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
            double price = price(event.getView().getTopInventory());
            if (EconomyHandler.checkCurrency(event.getWhoClicked().getUniqueId()) < price) {
                event.getWhoClicked().sendMessage(SpecialItemSystemsConfig.getInsufficientFundsMessage()
                        .replace("$price", Round.twoDecimalPlaces(price) + "")
                        .replace("$currencyName", EconomySettingsConfig.getCurrencyName())
                        .replace("$currentAmount", EconomyHandler.checkCurrency(event.getWhoClicked().getUniqueId()) + "")
                        .replace("$itemName", event.getView().getTopInventory().getItem(ITEM_SLOT).getItemMeta().getDisplayName()));
                event.getWhoClicked().closeInventory();
                return;
            }

            if (event.getView().getTopInventory().getItem(ITEM_SLOT) == null ||
                    event.getView().getTopInventory().getItem(ENCHANTED_BOOK_SLOT) == null) {
                event.getWhoClicked().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cYou must add an elite item and an enchanted book to enchant an item!"));
                return;
            }

            EconomyHandler.subtractCurrency(event.getWhoClicked().getUniqueId(), price);
            event.getWhoClicked().sendMessage(SpecialItemSystemsConfig.getNewFundsMessage()
                    .replace("$price", price + "")
                    .replace("$currencyName", EconomySettingsConfig.getCurrencyName())
                    .replace("$currentAmount", EconomyHandler.checkCurrency(event.getWhoClicked().getUniqueId()) + ""));

            //challenge(event); cool for debugging, I'll remove this later

            EnumMap<Chance, Double> chance = getChanceBreakdown(event.getView().getTopInventory());
            double rolledChance = ThreadLocalRandom.current().nextDouble();
            if (rolledChance < chance.get(Chance.CRITICAL_FAILURE))
                criticalFailure(event);
            else if (rolledChance < chance.get(Chance.CHALLENGE))
                challenge(event);
            else if (rolledChance < chance.get(Chance.FAILURE))
                failure(event);
            else
                success(event);

            event.getView().getTopInventory().clear();
            event.getWhoClicked().closeInventory();
        }

        private void failure(InventoryClickEvent event) {
            event.getWhoClicked().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cFailed enchantment! The enchantment did not bind to the item."));
            moveItemDown(event.getView().getTopInventory(), ITEM_SLOT, event.getWhoClicked());
        }

        private void challenge(InventoryClickEvent event) {
            ItemStack currentItem = event.getView().getTopInventory().getItem(ITEM_SLOT);
            if (!EnchantmentDungeonInstance.setupRandomEnchantedChallengeDungeon((Player) event.getWhoClicked(),
                    UpgradeSystem.upgrade(currentItem.clone(), event.getView().getTopInventory().getItem(ENCHANTED_BOOK_SLOT)),
                    currentItem.clone())) {
                success(event);
                return;
            }
            currentItem.setAmount(currentItem.getAmount() - 1);
            if (currentItem.getAmount() > 0)
                if (moveItemDown(event.getView().getTopInventory(), ITEM_SLOT, event.getWhoClicked(), false)) {
                    event.getWhoClicked().sendMessage(ChatColorConverter.convert(
                            "&8[EliteMobs] &cYour inventory was full so the item you were trying to upgrade has been deleted! It will be restored if your item is not full by the time you leave the instanced dungeon."));
                }

            event.getWhoClicked().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &6Challenge! Defeat the boss to get your upgraded item!"));
            event.getWhoClicked().sendMessage(ChatColorConverter.convert("&cThere's a 10% chance of losing your item if you lose the fight! Leaving the arena counts as losing."));
            broadcastEnchantmentMessage(currentItem, (Player) event.getWhoClicked(), SpecialItemSystemsConfig.getChallengeAnnouncement());
        }

        private void criticalFailure(InventoryClickEvent event) {
            event.getWhoClicked().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Critical failure! You lost the item!"));
            broadcastEnchantmentMessage(event.getView().getTopInventory().getItem(ITEM_SLOT), (Player) event.getWhoClicked(), SpecialItemSystemsConfig.getCriticalFailureAnnouncement());
        }

        private void success(InventoryClickEvent event) {
            ItemStack upgradedItem = UpgradeSystem.upgrade(event.getView().getTopInventory().getItem(ITEM_SLOT),
                    event.getView().getTopInventory().getItem(ENCHANTED_BOOK_SLOT));
            Player player = (Player) event.getWhoClicked();
            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(upgradedItem);
            if (!leftovers.isEmpty())
                event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), upgradedItem);
            event.getWhoClicked().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Successful enchantment!"));
            broadcastEnchantmentMessage(upgradedItem, (Player) event.getWhoClicked(), SpecialItemSystemsConfig.getSuccessAnnouncement());
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (menus.contains(event.getInventory())) {
                menus.remove(event.getInventory());
                EliteMenu.cancel(event.getPlayer(), event.getView().getTopInventory(), event.getView().getBottomInventory(),
                        Arrays.asList(ITEM_SLOT, ENCHANTED_BOOK_SLOT, LUCKY_TICKET_SLOT));
            }
        }

    }
}
