package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class QuestProgressionBar {

    public static void sendQuestProgression(Player player, QuestObjective questObjective) {

        BarStyle barStyle;

        switch (questObjective.getObjectiveKills()) {
            case 6:
                barStyle = BarStyle.SEGMENTED_6;
                break;
            case 10:
                barStyle = BarStyle.SEGMENTED_10;
                break;
            case 12:
                barStyle = BarStyle.SEGMENTED_12;
                break;
            case 20:
                barStyle = BarStyle.SEGMENTED_20;
                break;
            default:
                barStyle = BarStyle.SOLID;
        }

        BossBar bossBar = Bukkit.createBossBar(questObjective.objectiveString(), BarColor.GREEN, barStyle, BarFlag.PLAY_BOSS_MUSIC);
        bossBar.setProgress((double) questObjective.getCurrentKills() / (double) questObjective.getObjectiveKills());
        bossBar.addPlayer(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                bossBar.removePlayer(player);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3);

    }

}
