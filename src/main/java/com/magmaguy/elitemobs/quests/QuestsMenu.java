package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.MenuUtils;
import com.magmaguy.elitemobs.utils.ObfuscatedStringHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;

public class QuestsMenu implements Listener {

    private static final String MAIN_MENU_KEY = ObfuscatedStringHandler.obfuscateString("/////");
    private static final String MAIN_MENU_NAME = "EliteMobs Quests" + MAIN_MENU_KEY;

    /**
     * Opens the main quest menu for a player. This contains all the ranks.
     *
     * @param player Player for whom the quest menu will open
     */
    public void initializeMainQuestMenu(Player player) {

        Inventory inventory = Bukkit.createInventory(player, 18, MAIN_MENU_NAME);

        for (int i = 11; i < 21; i++) {

            int itemIndex = i;
            if (i == 20)
                itemIndex = 24;

            if (GuildRank.isWithinActiveRank(player, i)) {
                inventory.setItem(itemIndex - 11,
                        ItemStackGenerator.generateItemStack(Material.GREEN_STAINED_GLASS_PANE,
                                "&aAccept a " + GuildRank.getRankName(i) + " &aquest!",
                                Arrays.asList("&aAccept a " + GuildRank.getRankName(i) + " &aquest and", "&aget special rewards!")));

            } else if (GuildRank.isWithinRank(player, i)) {
                inventory.setItem(itemIndex - 11, ItemStackGenerator.generateItemStack(Material.YELLOW_STAINED_GLASS_PANE,
                        "&eYou can get a " + GuildRank.getRankName(i) + " &equest!",
                        Arrays.asList("&eDo /ag and set your", "&eguild rank to " + GuildRank.getRankName(i), "&eto accept these quests!")));

            } else {
                inventory.setItem(itemIndex - 11, ItemStackGenerator.generateItemStack(Material.RED_STAINED_GLASS_PANE,
                        "&cYou can't get a " + GuildRank.getRankName(i) + " &cquest yet!",
                        Arrays.asList("&cYou must first unlock the", GuildRank.getRankName(i) + " &cguild rank at /ag", "&cto accept these quests!")));

            }

        }

        player.openInventory(inventory);

    }

    @EventHandler
    public void onMainQuestClick(InventoryClickEvent event) {
        if (!MenuUtils.isValidMenu(event, MAIN_MENU_NAME)) return;
        event.setCancelled(true);
        if (event.getInventory().getType().equals(InventoryType.PLAYER)) return;
        if (!event.getCurrentItem().getType().equals(Material.GREEN_STAINED_GLASS_PANE)) return;

        initializeTierQuestMenu((Player) event.getWhoClicked(), event.getSlot());
    }

    /**
     * Opens the tier quest menu for a player. This lists currently available quests.
     *
     * @param player   Player for whom the menu will open
     * @param menuSlot Menu slot, used to guess the tier of the quest
     */
    public void initializeTierQuestMenu(Player player, int menuSlot) {

        int questTier = menuSlot + 1;
        if (menuSlot == 13) questTier = 10;

        player.openInventory(QuestRefresher.getQuestTierInventory(questTier).getInventory(player));

    }

    @EventHandler
    public void onTierQuestClick(InventoryClickEvent event) throws CloneNotSupportedException {
        if (!event.getView().getTitle().contains(QuestTierMenu.TIER_MENU_NAME)) return;
        if (!MenuUtils.isValidMenu(event)) return;
        event.setCancelled(true);
        if (event.getInventory().getType().equals(InventoryType.PLAYER)) return;

        int tier = 0;

        for (int i = 11; i < 21; i++)
            if (event.getView().getTitle().contains(GuildRank.getRankName(i))) {
                tier = i;
                break;
            }

        QuestTierMenu questTierMenu = QuestRefresher.getQuestTierInventory(tier - 10);
        PlayerQuest playerQuest = questTierMenu.getPlayerQuests().get(event.getSlot() / 2 - 1);
        PlayerQuest.addPlayerInQuests((Player) event.getWhoClicked(), playerQuest.clone());

        event.getWhoClicked().sendMessage("[EM] You have accepted a quest!");
        event.getWhoClicked().closeInventory();

    }

}
