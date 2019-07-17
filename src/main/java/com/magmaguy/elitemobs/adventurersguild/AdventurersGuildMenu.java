package com.magmaguy.elitemobs.adventurersguild;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import com.magmaguy.elitemobs.quests.QuestsMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class AdventurersGuildMenu implements Listener {

    private static ItemStack difficulty = new ItemStack(Material.DIAMOND_SWORD);
    private static ItemStack quest;

    public static void mainMenu(Player player) {

        Inventory mainMenu = Bukkit.createInventory(player, 27, "Adventurer's guild");

        difficulty = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta difficultyMeta = difficulty.getItemMeta();
        difficultyMeta.setDisplayName("Difficulty");
        difficultyMeta.setLore(Arrays.asList("Press me to select a difficulty!"));
        difficultyMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        difficulty.setItemMeta(difficultyMeta);

        quest = skullItemInitializer("MHF_Question", "Quests", Arrays.asList("Get your quests here!"));

        mainMenu.setItem(11, difficulty);
        mainMenu.setItem(15, quest);

        player.openInventory(mainMenu);

    }

    public static void difficultyMenu(Player player) {

        Inventory difficultyMenu = Bukkit.createInventory(player, 18, "Guild rank selector");

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

        switch (guildRankStatus) {

            case UNLOCKED:
                itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta unlockedMeta = itemStack.getItemMeta();
                String lowTierWarning = "";
                if (rank < 1) lowTierWarning = ChatColorConverter.convert("&cElites can't drop better loot!");
                else lowTierWarning = ChatColorConverter.convert("&aElites can drop better loot!");
                unlockedMeta.setLore(Arrays.asList(
                        ChatColorConverter.convert("&f&m-------------------------------"),
                        ChatColorConverter.convert("&aThis rank is unlocked!"),
                        ChatColorConverter.convert("&fYou can select it."),
                        lowTierWarning,
                        ChatColorConverter.convert("&f&m-------------------------------"),
                        lootBonus(rank),
                        mobSpawning(rank),
                        difficultyBonus(rank)));
                itemStack.setItemMeta(unlockedMeta);
                break;
            case LOCKED:
                itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta lockedMeta = itemStack.getItemMeta();
                lockedMeta.setLore(Arrays.asList(
                        ChatColorConverter.convert("&f&m-------------------------------"),
                        ChatColorConverter.convert("&cThis rank is locked!"),
                        ChatColorConverter.convert("&cYou need rank " + (PlayerData.playerMaxGuildRank.get(player.getUniqueId()) + 1) + " first!"),
                        ChatColorConverter.convert("&f&m-------------------------------"),
                        lootBonus(rank),
                        mobSpawning(rank),
                        difficultyBonus(rank)));
                itemStack.setItemMeta(lockedMeta);
                break;
            case SELECTED:
                itemStack = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta selectedMeta = itemStack.getItemMeta();
                String tierWarning = "";
                if (rank < 10) tierWarning = ChatColorConverter.convert("&cElites can't drop better loot!");
                else tierWarning = ChatColorConverter.convert("&aElites can drop better loot!");
                selectedMeta.setLore(Arrays.asList(
                        ChatColorConverter.convert("&f&m-------------------------------"),
                        ChatColorConverter.convert("&aThis is your current rank!"),
                        tierWarning,
                        ChatColorConverter.convert("&f&m-------------------------------"),
                        lootBonus(rank),
                        mobSpawning(rank),
                        difficultyBonus(rank)));
                itemStack.setItemMeta(selectedMeta);
                PlayerData.playerSelectedGuildRankChanged = true;
                break;
            case NEXT_UNLOCK:
                itemStack = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
                ItemMeta nextMeta = itemStack.getItemMeta();
                String priceString = "";
                if (!PlayerData.playerCurrency.containsKey(player.getUniqueId()))
                    PlayerData.playerCurrency.put(player.getUniqueId(), 0.0);
                if (tierPriceCalculator(rank) > PlayerData.playerCurrency.get(player.getUniqueId()))
                    priceString = "&c" + tierPriceCalculator(rank);
                else
                    priceString = "&a" + tierPriceCalculator(rank);
                nextMeta.setLore(Arrays.asList(
                        ChatColorConverter.convert("&f&m-------------------------------"),
                        ChatColorConverter.convert("&6This is the next rank you can unlock"),
                        ChatColorConverter.convert("&aSelect it when you're ready!"),
                        ChatColorConverter.convert("&6Costs " + priceString + " &6" + EconomySettingsConfig.currencyName),
                        ChatColorConverter.convert("&fYou have &a" + EconomyHandler.checkCurrency(player.getUniqueId()) + " &f" + EconomySettingsConfig.currencyName),
                        ChatColorConverter.convert("&f&m-------------------------------"),
                        lootBonus(rank),
                        mobSpawning(rank),
                        difficultyBonus(rank)));
                itemStack.setItemMeta(nextMeta);
                PlayerData.playerMaxGuildRankChanged = true;
                break;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(GuildRank.getRankName(rank) + " rank");
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

    private static void questMenu(Player player) {
        QuestsMenu questsMenu = new QuestsMenu();
        questsMenu.initializeMainQuestMenu(player);
    }

    private static ItemStack skullItemInitializer(String mhfValue, String title, List<String> lore) {

        ItemStack item = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short) 3);
        ItemMeta itemMeta = item.getItemMeta();
        SkullMeta skullMeta = (SkullMeta) itemMeta;
        skullMeta.setOwner(mhfValue);
        item.setItemMeta(skullMeta);
        itemMeta.setDisplayName(title);
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);

        return item;

    }

    private static int tierPriceCalculator(int tier) {
        return tier * tier * 100;
    }

    @EventHandler
    public void onMainMenuClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equalsIgnoreCase("Adventurer's guild")) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
        if (event.getCurrentItem().equals(difficulty))
            difficultyMenu((Player) event.getWhoClicked());
        if (event.getSlot() == 15)
            questMenu((Player) event.getWhoClicked());

        event.setCancelled(true);

    }

    @EventHandler
    public void onRankSelectorClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equalsIgnoreCase("Guild rank selector")) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;

        event.setCancelled(true);
        if (event.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            return;
        }

        if (!event.getView().getTitle().equals("Guild rank selector")) {
            return;
        }

        int maxTier = PlayerData.playerMaxGuildRank.get(event.getWhoClicked().getUniqueId());
        int selectedTier = event.getSlot();

        if (selectedTier < maxTier + 1) {
            PlayerData.playerSelectedGuildRank.put(event.getWhoClicked().getUniqueId(), selectedTier);
            difficultyMenu((Player) event.getWhoClicked());
            if (AdventurersGuildConfig.addMaxHealth)
                MaxHealthHandler.adjustMaxHealth((Player) event.getWhoClicked());
        }

        if (selectedTier == maxTier + 1) {
            if (EconomyHandler.checkCurrency(event.getWhoClicked().getUniqueId()) < tierPriceCalculator(selectedTier))
                event.getWhoClicked().sendMessage("[EliteMobs] You don't have enough Elite Coins! Sell some Elite Mob loot to [/em shop]!");
            else {
                EconomyHandler.subtractCurrency(event.getWhoClicked().getUniqueId(), tierPriceCalculator(selectedTier));
                GuildRank.setRank((Player) event.getWhoClicked(), selectedTier);
                GuildRank.setActiveRank((Player) event.getWhoClicked(), selectedTier);
                event.getWhoClicked().sendMessage(ChatColorConverter.convert("&aYou have unlocked the " + GuildRank.getRankName(selectedTier) + " &arank for " +
                        tierPriceCalculator(selectedTier) + " " + EconomySettingsConfig.currencyName + ". \n&6Happy hunting!"));
                difficultyMenu((Player) event.getWhoClicked());
                Bukkit.broadcastMessage(ChatColorConverter.convert(
                        ((Player) event.getWhoClicked()).getDisplayName() + " has reached the " + GuildRank.getRankName(selectedTier) + " &fguild rank!"));
                if (AdventurersGuildConfig.addMaxHealth)
                    MaxHealthHandler.adjustMaxHealth((Player) event.getWhoClicked());
            }
        }

        if (selectedTier > maxTier + 1)
            event.getWhoClicked().sendMessage("[EliteMobs] You need to unlock other ranks first!");

    }

}
