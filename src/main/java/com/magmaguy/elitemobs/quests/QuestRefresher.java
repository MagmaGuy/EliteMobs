package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class QuestRefresher {

    private static HashMap<Integer, QuestTierMenu> questTierInventories = new HashMap<>();

    public static void addQuestTierInventories(int questTier, QuestTierMenu questTierMenu) {
        questTierInventories.put(questTier, questTierMenu);
    }

    public static QuestTierMenu getQuestTierInventory(int questTier) {
        return questTierInventories.get(questTier);
    }

    public static void generateNewQuestMenus() {

        new BukkitRunnable() {

            @Override
            public void run() {

                for (int i = 1; i < 11; i++)
                    addQuestTierInventories(i, new QuestTierMenu(i));

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 60 * 30);

    }

}
