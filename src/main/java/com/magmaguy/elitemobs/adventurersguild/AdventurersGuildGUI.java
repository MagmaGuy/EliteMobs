package com.magmaguy.elitemobs.adventurersguild;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class AdventurersGuildGUI implements Listener {

    private static ItemStack difficulty = new ItemStack(Material.DIAMOND_SWORD);
    private static ItemStack quest = skullItemInitializer("MHF_Question", "Quests", Arrays.asList("Coming soon!"));

    public static void mainMenu(Player player) {

        Inventory mainMenu = Bukkit.createInventory(player, 27, "Adventurer's guild");

        difficulty = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta difficultyMeta = difficulty.getItemMeta();
        difficultyMeta.setDisplayName("Difficulty");
        difficultyMeta.setLore(Arrays.asList("Press me to select a difficulty!"));
        difficultyMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        difficulty.setItemMeta(difficultyMeta);

        quest = skullItemInitializer("MHF_Question", "Quests", Arrays.asList("Coming soon!"));

        mainMenu.setItem(11, difficulty);
        mainMenu.setItem(15, quest);

        player.openInventory(mainMenu);

    }

    public static void difficultyMenu(Player player) {

        Inventory difficultyMenu = Bukkit.createInventory(player, 27, "Guild rank selector");

        if (!PlayerData.playerMaxGuildRank.containsKey(player.getUniqueId())) {
            PlayerData.playerMaxGuildRank.put(player.getUniqueId(), 10);
            PlayerData.playerMaxGuildRankChanged = true;
        }

        if (!PlayerData.playerSelectedGuildRank.containsKey(player.getUniqueId())) {
            PlayerData.playerSelectedGuildRank.put(player.getUniqueId(), 10);
            PlayerData.playerSelectedGuildRankChanged = true;
        }

        for (int i = 1; i < 21; i++) {

            ItemStack itemStack = null;

            if (i <= PlayerData.playerMaxGuildRank.get(player.getUniqueId()))
                itemStack = difficultyItemStackConstructor(guildRankStatus.UNLOCKED, i, player);
            if (i > PlayerData.playerMaxGuildRank.get(player.getUniqueId()))
                itemStack = difficultyItemStackConstructor(guildRankStatus.LOCKED, i, player);
            if (i == PlayerData.playerSelectedGuildRank.get(player.getUniqueId()))
                itemStack = difficultyItemStackConstructor(guildRankStatus.SELECTED, i, player);
            if (i == PlayerData.playerMaxGuildRank.get(player.getUniqueId()) + 1) {
                itemStack = difficultyItemStackConstructor(guildRankStatus.NEXT_UNLOCK, i, player);

            }

            difficultyMenu.addItem(itemStack);

        }

        player.openInventory(difficultyMenu);

    }

    private static ItemStack difficultyItemStackConstructor(guildRankStatus guildRankStatus, int rank, Player player) {

        ItemStack itemStack = null;

        switch (guildRankStatus) {

            case UNLOCKED:
                itemStack = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS);
                ItemMeta unlockedMeta = itemStack.getItemMeta();
                String lowTierWarning = "";
                if (rank < 10) lowTierWarning = ChatColorConverter.convert("&cElites can't drop better loot!");
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
                        ChatColorConverter.convert("&6Costs " + priceString + " &6" + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME)),
                        ChatColorConverter.convert("&fYou have &a" + PlayerData.playerCurrency.get(player.getUniqueId()) + " &f" + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME)),
                        ChatColorConverter.convert("&f&m-------------------------------"),
                        lootBonus(rank),
                        mobSpawning(rank),
                        difficultyBonus(rank)));
                itemStack.setItemMeta(nextMeta);
                PlayerData.playerMaxGuildRankChanged = true;
                break;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(rankNamer(rank) + " rank");
        itemStack.setItemMeta(itemMeta);

        return itemStack;

    }

    private static String rankNamer(int rank) {

        String name = "";

        if (rank == 1) name = "&8Peaceful Villager";
        if (rank == 2) name = "&8Commoner";
        if (rank == 3) name = "&8Farmer";
        if (rank == 4) name = "&8Bard";
        if (rank == 5) name = "&8Barkeep";
        if (rank == 6) name = "&8Blacksmith";
        if (rank == 7) name = "&8Merchant";
        if (rank == 8) name = "&8Wanderer";
        if (rank == 9) name = "&8Ranger";
        if (rank == 10) name = "&fCasual Adventurer";
        if (rank == 11) name = "&fAdventurer";
        if (rank == 12) name = "&fProfessional Adventurer";
        if (rank == 13) name = "&2Elite Adventurer";
        if (rank == 14) name = "&2Master Adventurer";
        if (rank == 15) name = "&2Raider";
        if (rank == 16) name = "&1Slayer";
        if (rank == 17) name = "&1Exterminator";
        if (rank == 18) name = "&5&lElite Hunter";
        if (rank == 19) name = "&5Hero";
        if (rank == 20) name = "&6&l&oLegend";

        return ChatColorConverter.convert(name);

    }

    private static String mobSpawning(int rank) {

        int mobSpawning = 0;

        if (rank == 1) mobSpawning = 10;
        if (rank == 2) mobSpawning = 20;
        if (rank == 3) mobSpawning = 30;
        if (rank == 4) mobSpawning = 40;
        if (rank == 5) mobSpawning = 50;
        if (rank == 6) mobSpawning = 60;
        if (rank == 7) mobSpawning = 70;
        if (rank == 8) mobSpawning = 80;
        if (rank == 9) mobSpawning = 90;
        if (rank == 10) mobSpawning = 100;
        if (rank == 11) mobSpawning = 120;
        if (rank == 12) mobSpawning = 140;
        if (rank == 13) mobSpawning = 160;
        if (rank == 14) mobSpawning = 180;
        if (rank == 15) mobSpawning = 200;
        if (rank == 16) mobSpawning = 220;
        if (rank == 17) mobSpawning = 240;
        if (rank == 18) mobSpawning = 260;
        if (rank == 19) mobSpawning = 280;
        if (rank == 20) mobSpawning = 300;

        return ChatColorConverter.convert("&fElite Mob spawn rate modifier: &c" + mobSpawning + "%");

    }

    private static String lootBonus(int rank) {

        int lootBonus = 0;

        if (rank == 1) lootBonus = 10;
        if (rank == 2) lootBonus = 20;
        if (rank == 3) lootBonus = 30;
        if (rank == 4) lootBonus = 40;
        if (rank == 5) lootBonus = 50;
        if (rank == 6) lootBonus = 60;
        if (rank == 7) lootBonus = 70;
        if (rank == 8) lootBonus = 80;
        if (rank == 9) lootBonus = 90;
        if (rank == 10) lootBonus = 100;
        if (rank == 11) lootBonus = 120;
        if (rank == 12) lootBonus = 140;
        if (rank == 13) lootBonus = 160;
        if (rank == 14) lootBonus = 180;
        if (rank == 15) lootBonus = 200;
        if (rank == 16) lootBonus = 220;
        if (rank == 17) lootBonus = 240;
        if (rank == 18) lootBonus = 260;
        if (rank == 19) lootBonus = 280;
        if (rank == 20) lootBonus = 300;

        return ChatColorConverter.convert("&fElite Mob loot modifier: &a" + lootBonus + "%");

    }

    private static String difficultyBonus(int rank) {

        int difficultyBonus = 0;

        if (rank == 1) difficultyBonus = 10;
        if (rank == 2) difficultyBonus = 20;
        if (rank == 3) difficultyBonus = 30;
        if (rank == 4) difficultyBonus = 40;
        if (rank == 5) difficultyBonus = 50;
        if (rank == 6) difficultyBonus = 60;
        if (rank == 7) difficultyBonus = 70;
        if (rank == 8) difficultyBonus = 80;
        if (rank == 9) difficultyBonus = 90;
        if (rank == 10) difficultyBonus = 100;
        if (rank == 11) difficultyBonus = 110;
        if (rank == 12) difficultyBonus = 120;
        if (rank == 13) difficultyBonus = 130;
        if (rank == 14) difficultyBonus = 140;
        if (rank == 15) difficultyBonus = 150;
        if (rank == 16) difficultyBonus = 160;
        if (rank == 17) difficultyBonus = 170;
        if (rank == 18) difficultyBonus = 180;
        if (rank == 19) difficultyBonus = 190;
        if (rank == 20) difficultyBonus = 200;

        return ChatColorConverter.convert("&fElite Mob difficulty modifier: &4" + difficultyBonus + "%");

    }

    private enum guildRankStatus {
        UNLOCKED,
        SELECTED,
        NEXT_UNLOCK,
        LOCKED
    }

    private static void questMenu(Player player) {

        player.closeInventory();
        player.sendMessage("Coming soon!");

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

        return (tier - 10) * (tier - 10) * 100;

    }

    @EventHandler
    public void onMainMenuClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equalsIgnoreCase("Adventurer's guild")) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
        if (event.getCurrentItem().equals(difficulty))
            difficultyMenu((Player) event.getWhoClicked());
        if (event.getCurrentItem().equals(quest)) {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().sendMessage("Coming soon!");
        }

        event.setCancelled(true);

    }

    @EventHandler
    public void onRankSelectorClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equalsIgnoreCase("Guild rank selector")) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;

        event.setCancelled(true);

        if (!event.getView().getTitle().equals("Guild rank selector")) {
            event.setCancelled(true);
            return;
        }

        int maxTier = PlayerData.playerMaxGuildRank.get(event.getWhoClicked().getUniqueId());
        int selectedTier = event.getSlot() + 1;

        if (selectedTier < maxTier + 1) {
            PlayerData.playerSelectedGuildRank.put(event.getWhoClicked().getUniqueId(), selectedTier);
            difficultyMenu((Player) event.getWhoClicked());
        }

        if (selectedTier == maxTier + 1) {
            if (EconomyHandler.checkCurrency(event.getWhoClicked().getUniqueId()) < tierPriceCalculator(selectedTier))
                event.getWhoClicked().sendMessage("[EliteMobs] You don't have enough Elite Coins! Sell some Elite Mob loot to [/em shop]!");
            else {
                EconomyHandler.subtractCurrency(event.getWhoClicked().getUniqueId(), tierPriceCalculator(selectedTier));
                PlayerData.playerMaxGuildRank.put(event.getWhoClicked().getUniqueId(), selectedTier);
                event.getWhoClicked().sendMessage(ChatColorConverter.convert("&aYou have unlocked the " + rankNamer(selectedTier) + " &arank for " +
                        tierPriceCalculator(selectedTier) + " " + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME) + ". \n&6Happy hunting!"));
                difficultyMenu((Player) event.getWhoClicked());
                Bukkit.broadcastMessage(ChatColorConverter.convert(
                        ((Player) event.getWhoClicked()).getDisplayName() + " has reached the " + rankNamer(selectedTier) + " &fguild rank!"));
                if (ConfigValues.adventurersGuildConfig.getBoolean(AdventurersGuildConfig.ADD_MAX_HEALTH))
                    event.getWhoClicked().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue((selectedTier - 10) * 2 + 20);
            }
        }

        if (selectedTier > maxTier + 1) {
            event.getWhoClicked().sendMessage("[EliteMobs] You need to unlock other ranks first!");
        }


    }

}
