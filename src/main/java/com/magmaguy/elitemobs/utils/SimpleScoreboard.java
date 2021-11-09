package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class SimpleScoreboard {
    public static Scoreboard temporaryScoreboard(Player player, String displayName, List<String> scoreboardContents, int ticksTimeout) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        int lineCount = Math.min(scoreboardContents.size(), 15);

        Objective objective = scoreboard.registerNewObjective("test", "dummy", displayName);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 0; i < lineCount; i++) {
            Score score = objective.getScore(scoreboardContents.get(i));
            score.setScore(i);
        }

        player.setScoreboard(scoreboard);
        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, (task) -> player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()), ticksTimeout);

        return scoreboard;
    }
}
