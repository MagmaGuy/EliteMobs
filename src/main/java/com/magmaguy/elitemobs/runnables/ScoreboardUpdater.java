package com.magmaguy.elitemobs.runnables;

import com.magmaguy.elitemobs.scoreboard.ScoreboardHandler;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardUpdater extends BukkitRunnable {

    ScoreboardHandler scoreboardHandler = new ScoreboardHandler();

    @Override
    public void run() {

        scoreboardHandler.scanSight();

    }

}
