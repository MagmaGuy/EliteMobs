package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class QuestRefresher {

    private static HashMap<Integer, QuestTierInventory> questTierInventories = new HashMap<>();

    public static void addQuestTierInventories(int level, QuestTierInventory questTierInventory) {
        questTierInventories.put(level, questTierInventory);
    }

    public static QuestTierInventory getQuestTierInventory(int tier) {
        return questTierInventories.get(tier);
    }

    public static void generateNewQuestMenus() {

        new BukkitRunnable() {

            @Override
            public void run() {

                for (int i = 1; i < 11; i++)
                    questTierInventories.put(i, new QuestTierInventory(i));

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 60 * 30);

    }

}
