package com.magmaguy.elitemobs.adventurersguild;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.GuildRankMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuildRankMenuHandler implements Listener {

    @EventHandler
    public void onRankSelectorClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(GuildRankMenuConfig.menuName)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;

        event.setCancelled(true);
        if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            return;
        }

        int maxTier = PlayerData.playerMaxGuildRank.get(event.getWhoClicked().getUniqueId());
        int selectedTier = event.getSlot();

        if (selectedTier < maxTier + 1) {
            PlayerData.playerSelectedGuildRank.put(event.getWhoClicked().getUniqueId(), selectedTier);
            initializeGuildRankMenu((Player) event.getWhoClicked());
            if (AdventurersGuildConfig.addMaxHealth)
                MaxHealthHandler.adjustMaxHealth((Player) event.getWhoClicked());
        }

        if (selectedTier == maxTier + 1) {
            if (EconomyHandler.checkCurrency(event.getWhoClicked().getUniqueId()) < tierPriceCalculator(selectedTier))
                event.getWhoClicked().sendMessage(GuildRankMenuConfig.notEnoughCurrencyMessage
                        .replace("$neededAmount", tierPriceCalculator(selectedTier) + "")
                        .replace("$currentAmount", EconomyHandler.checkCurrency(event.getWhoClicked().getUniqueId()) + "")
                        .replace("$currencyName", EconomySettingsConfig.currencyName));
            else {
                EconomyHandler.subtractCurrency(event.getWhoClicked().getUniqueId(), tierPriceCalculator(selectedTier));
                GuildRank.setRank((Player) event.getWhoClicked(), selectedTier);
                GuildRank.setActiveRank((Player) event.getWhoClicked(), selectedTier);
                event.getWhoClicked().sendMessage(GuildRankMenuConfig.unlockMessage
                        .replace("$rankName", GuildRank.getRankName(selectedTier))
                        .replace("$price", tierPriceCalculator(selectedTier) + "")
                        .replace("$currencyName", EconomySettingsConfig.currencyName));
                initializeGuildRankMenu((Player) event.getWhoClicked());
                Bukkit.broadcastMessage(GuildRankMenuConfig.broadcastMessage
                        .replace("$player", ((Player) event.getWhoClicked()).getDisplayName())
                        .replace("$rankName", GuildRank.getRankName(selectedTier)));
                if (AdventurersGuildConfig.addMaxHealth)
                    MaxHealthHandler.adjustMaxHealth((Player) event.getWhoClicked());
            }
        }

        if (selectedTier > maxTier + 1)
            event.getWhoClicked().sendMessage(GuildRankMenuConfig.failedMessage);

    }

    public static void initializeGuildRankMenu(Player player) {

        Inventory difficultyMenu = Bukkit.createInventory(player, 18, GuildRankMenuConfig.menuName);

        if (!PlayerData.playerMaxGuildRank.containsKey(player.getUniqueId())) {
            PlayerData.playerMaxGuildRank.put(player.getUniqueId(), 1);
            PlayerData.playerMaxGuildRankChanged = true;
        }

        if (!PlayerData.playerSelectedGuildRank.containsKey(player.getUniqueId())) {
            PlayerData.playerSelectedGuildRank.put(player.getUniqueId(), 1);
            PlayerData.playerSelectedGuildRankChanged = true;
        }

        for (int i = 0; i < 12; i++) {

            ItemStack itemStack = null;

            if (i <= PlayerData.playerMaxGuildRank.get(player.getUniqueId()))
                itemStack = difficultyItemStackConstructor(guildRankStatus.UNLOCKED, i, player);
            if (i > PlayerData.playerMaxGuildRank.get(player.getUniqueId()))
                itemStack = difficultyItemStackConstructor(guildRankStatus.LOCKED, i, player);
            if (i == PlayerData.playerSelectedGuildRank.get(player.getUniqueId()))
                itemStack = difficultyItemStackConstructor(guildRankStatus.SELECTED, i, player);
            if (i == PlayerData.playerMaxGuildRank.get(player.getUniqueId()) + 1)
                itemStack = difficultyItemStackConstructor(guildRankStatus.NEXT_UNLOCK, i, player);

            difficultyMenu.addItem(itemStack);

        }

        player.openInventory(difficultyMenu);

    }

    private static ItemStack difficultyItemStackConstructor(guildRankStatus guildRankStatus, int rank, Player player) {

        ItemStack itemStack = null;

        String lowTierWarning = "";
        if (rank < 1) lowTierWarning = GuildRankMenuConfig.lowTierWarning;
        else lowTierWarning = GuildRankMenuConfig.normalTierWarning;

        String priceString = "";
        if (tierPriceCalculator(rank) > EconomyHandler.checkCurrency(player.getUniqueId()))
            priceString = "&c" + tierPriceCalculator(rank);
        else
            priceString = "&a" + tierPriceCalculator(rank);

        switch (guildRankStatus) {
            case UNLOCKED:
                itemStack = constructButtonItemStack(rank, lowTierWarning, GuildRankMenuConfig.unlockedButton.clone(), player, priceString);
                break;
            case LOCKED:
                itemStack = constructButtonItemStack(rank, lowTierWarning, GuildRankMenuConfig.lockedButton.clone(), player, priceString);
                break;
            case SELECTED:
                itemStack = constructButtonItemStack(rank, lowTierWarning, GuildRankMenuConfig.currentButton.clone(), player, priceString);
                break;
            case NEXT_UNLOCK:
                itemStack = constructButtonItemStack(rank, lowTierWarning, GuildRankMenuConfig.nextButton.clone(), player, priceString);
                break;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(GuildRank.getRankName(rank) + " rank");
        itemStack.setItemMeta(itemMeta);

        return itemStack;

    }

    private static ItemStack constructButtonItemStack(int rank, String lowTierWarning, ItemStack itemStack, Player player, String priceString) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> newLore = new ArrayList<>();
        for (String string : itemMeta.getLore())
            switch (string) {
                case "$tierWarning":
                    newLore.add(lowTierWarning);
                    break;
                case "$lootBonus":
                    newLore.add(lootBonus(rank));
                    break;
                case "$mobSpawning":
                    newLore.add(mobSpawning(rank));
                    break;
                case "$difficultyBonus":
                    newLore.add(difficultyBonus(rank));
                    break;
                default:
                    if (string.contains("$previousRank")) {
                        newLore.add(string.replace("$previousRank",
                                (GuildRank.getRankName(PlayerData.playerMaxGuildRank.get(player.getUniqueId()) + 1)) + ""));
                        continue;
                    }
                    if (string.contains("$price")) {
                        newLore.add(
                                ChatColorConverter.convert(
                                        string.replace("$price", priceString)
                                                .replace("$currencyName", EconomySettingsConfig.currencyName)));
                        continue;
                    }
                    if (string.contains("$currentCurrency")) {
                        newLore.add(string
                                .replace("$currentCurrency", EconomyHandler.checkCurrency(player.getUniqueId()) + "")
                                .replace("$currencyName", EconomySettingsConfig.currencyName));
                        continue;
                    }
                    newLore.add(string);
                    break;
            }
        itemMeta.setLore(newLore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static String mobSpawning(int rank) {

        int mobSpawning = 0;

        if (rank == 0) mobSpawning = 10;
        if (rank == 1) mobSpawning = 100;
        if (rank == 2) mobSpawning = 120;
        if (rank == 3) mobSpawning = 140;
        if (rank == 4) mobSpawning = 160;
        if (rank == 5) mobSpawning = 180;
        if (rank == 6) mobSpawning = 200;
        if (rank == 7) mobSpawning = 220;
        if (rank == 8) mobSpawning = 240;
        if (rank == 9) mobSpawning = 260;
        if (rank == 10) mobSpawning = 280;
        if (rank == 11) mobSpawning = 300;

        return ChatColorConverter.convert("&fElite Mob spawn rate modifier: &c" + mobSpawning + "%");

    }

    private static String lootBonus(int rank) {

        int lootBonus = 0;

        if (rank == 0) lootBonus = 10;
        if (rank == 1) lootBonus = 100;
        if (rank == 2) lootBonus = 120;
        if (rank == 3) lootBonus = 140;
        if (rank == 4) lootBonus = 160;
        if (rank == 5) lootBonus = 180;
        if (rank == 6) lootBonus = 200;
        if (rank == 7) lootBonus = 220;
        if (rank == 8) lootBonus = 240;
        if (rank == 9) lootBonus = 260;
        if (rank == 10) lootBonus = 280;
        if (rank == 11) lootBonus = 300;

        return ChatColorConverter.convert("&fElite Mob loot modifier: &a" + lootBonus + "%");

    }

    private static String difficultyBonus(int rank) {

        int difficultyBonus = 0;

        if (rank == 0) difficultyBonus = 10;
        if (rank == 1) difficultyBonus = 100;
        if (rank == 2) difficultyBonus = 110;
        if (rank == 3) difficultyBonus = 120;
        if (rank == 4) difficultyBonus = 130;
        if (rank == 5) difficultyBonus = 140;
        if (rank == 6) difficultyBonus = 150;
        if (rank == 7) difficultyBonus = 160;
        if (rank == 8) difficultyBonus = 170;
        if (rank == 9) difficultyBonus = 180;
        if (rank == 10) difficultyBonus = 190;
        if (rank == 11) difficultyBonus = 200;

        return ChatColorConverter.convert("&fElite Mob difficulty modifier: &4" + difficultyBonus + "%");

    }

    private enum guildRankStatus {
        UNLOCKED,
        SELECTED,
        NEXT_UNLOCK,
        LOCKED
    }

    private static int tierPriceCalculator(int tier) {
        /*Logic:
        tier * 10 = max mob tier
        max mob tier / 2 = loot shower payout for killing 1 em at max level for that player
        payout * 1000 = amount of elite mobs to kill before going up a rank
         */
        double eliteMobsToKillBeforeGuildRankup = 200 + 1000 * (tier - 1) * 0.1;
        return (int) ((tier - 1) * 10 / 2 * eliteMobsToKillBeforeGuildRankup);
    }

}
