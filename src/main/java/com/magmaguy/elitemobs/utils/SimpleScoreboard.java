package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.ScoreboardUtil;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class SimpleScoreboard {

    public static Scoreboard lazyScoreboard(Player player, String displayName, List<String> scoreboardContents) {
        return ScoreboardUtil.lazyScoreboard(MetadataHandler.PLUGIN, player, displayName, scoreboardContents);
    }

    public static Scoreboard temporaryScoreboard(Player player, String displayName, List<String> scoreboardContents, int ticksTimeout) {
        return ScoreboardUtil.temporaryScoreboard(MetadataHandler.PLUGIN, player, displayName, scoreboardContents, ticksTimeout);
    }
}
