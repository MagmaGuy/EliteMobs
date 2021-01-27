package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.menus.premade.QuestMenuConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class QuestsMenu implements Listener {

    @EventHandler
    public void onRankSelectorClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!inventories.contains(event.getInventory())) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        Integer selectedRank = null;

        for (int i = 0; i < rankSlots.size(); i++) {
            if (rankSlots.get(i) == event.getSlot()) {
                selectedRank = i;
                break;
            }
        }

        //Clicked nothing
        if (selectedRank == null) return;

        EliteQuest eliteQuest = rotatingQuests.get(GuildRank.getActiveGuildRank(player)).get(selectedRank);

        if (!PlayerQuests.hasQuest(player, eliteQuest)) {
            PlayerQuests.addQuest(player, eliteQuest);
            player.sendMessage("You've accepted a quest!");
            player.sendMessage("Objective: kill " + eliteQuest.getQuestObjective().getObjectiveKills() + " level " + eliteQuest.getQuestObjective().getMinimumEliteMobLevel() + "+ " + eliteQuest.getQuestObjective().getEliteMobName());
            player.closeInventory();
        }

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!inventories.contains(event.getInventory())) return;
        inventories.remove(event.getInventory());
    }

    private static final HashSet<Inventory> inventories = new HashSet<>();

    private static final ArrayList<Integer> rankSlots = new ArrayList<>(Arrays.asList(10, 12, 14, 16));

    public static Inventory initializeQuestsMenu(Player player) {
        Inventory questsMenu = Bukkit.createInventory(player, 18, QuestMenuConfig.menuName);
        questsMenu = populateInventory(questsMenu, player);
        inventories.add(questsMenu);
        player.openInventory(questsMenu);
        return questsMenu;
    }

    private static Inventory populateInventory(Inventory inventory, Player player) {
        for (int i = 0; i < rankSlots.size(); i++)
            inventory.setItem(rankSlots.get(i), rotatingQuests.get(GuildRank.getActiveGuildRank(player)).get(i).generateQuestItemStack(player));
        return inventory;
    }

    private static final HashMap<Integer, ArrayList<EliteQuest>> rotatingQuests = new HashMap();

    //refreshes quests every hour
    public static void questRefresher() {
        new BukkitRunnable() {
            @Override
            public void run() {
                populateQuests();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 60 * 60 * 20);
    }

    private static void populateQuests() {
        for (int i = 0; i < 21; i++) {
            ArrayList<EliteQuest> questList = new ArrayList();
            for (int j = 0; j < 4; j++) {
                questList.add(new EliteQuest(i));
            }
            rotatingQuests.put(i, questList);
        }
    }

}
