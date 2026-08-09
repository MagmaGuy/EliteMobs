package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public class SimpleScoreboard {
    private static final String SIDEBAR_OBJECTIVE = "em_quest_sb";
    private static final int MAX_SIDEBAR_LINES = 15;
    private static final int LEGACY_SCOREBOARD_ENTRY_LIMIT = 40;
    private static final Map<UUID, Scoreboard> previousScoreboards = new ConcurrentHashMap<>();
    private static final Set<Scoreboard> managedScoreboards = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<Scoreboard, Set<String>> managedSidebarEntries = new WeakHashMap<>();

    public static Scoreboard lazyScoreboard(Player player, String displayName, List<String> scoreboardContents) {
        Scoreboard scoreboard = createManagedScoreboard(player);
        Objective objective = ScoreboardUtil.registerSidebarObjective(MetadataHandler.PLUGIN, scoreboard, SIDEBAR_OBJECTIVE, displayName);
        setSidebarLines(objective, scoreboard, scoreboardContents);
        player.setScoreboard(scoreboard);
        return scoreboard;
    }

    /** Reuses the current EliteMobs scoreboard when possible, avoiding a new board allocation for periodic UI updates. */
    public static Scoreboard updateScoreboard(Player player, String displayName, List<String> scoreboardContents) {
        Scoreboard scoreboard = player.getScoreboard();
        if (!isManagedScoreboard(scoreboard)) return lazyScoreboard(player, displayName, scoreboardContents);
        Objective existing = scoreboard.getObjective(SIDEBAR_OBJECTIVE);
        Objective objective;
        if (existing == null || !managedSidebarEntries.containsKey(scoreboard)) {
            if (existing != null) existing.unregister();
            objective = ScoreboardUtil.registerSidebarObjective(
                    MetadataHandler.PLUGIN, scoreboard, SIDEBAR_OBJECTIVE, displayName);
        } else {
            // Party health changes frequently. Keep the same internal objective instead of
            // unregistering it every refresh, which makes clients visibly blink the sidebar.
            clearSidebarLines(scoreboard);
            existing.setDisplayName(displayName);
            existing.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective = existing;
        }
        setSidebarLines(objective, scoreboard, scoreboardContents);
        return scoreboard;
    }

    /** Lets periodic UI owners avoid rebuilding an unchanged EliteMobs sidebar objective. */
    public static boolean hasManagedSidebar(Player player) {
        if (player == null || !isManagedScoreboard(player.getScoreboard())) return false;
        Objective objective = player.getScoreboard().getObjective(SIDEBAR_OBJECTIVE);
        return objective != null && objective.equals(player.getScoreboard().getObjective(DisplaySlot.SIDEBAR));
    }

    public static Scoreboard temporaryScoreboard(Player player, String displayName, List<String> scoreboardContents, int ticksTimeout) {
        Scoreboard scoreboard = lazyScoreboard(player, displayName, scoreboardContents);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    previousScoreboards.remove(player.getUniqueId());
                    return;
                }
                if (player.getScoreboard().equals(scoreboard))
                    clearScoreboard(player);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticksTimeout);

        return scoreboard;
    }

    public static Scoreboard blankScoreboard(Player player) {
        Scoreboard scoreboard = createManagedScoreboard(player);
        player.setScoreboard(scoreboard);
        return scoreboard;
    }

    public static void clearScoreboard(Player player) {
        Scoreboard previousScoreboard = previousScoreboards.remove(player.getUniqueId());
        if (!player.isOnline()) return;

        managedSidebarEntries.remove(player.getScoreboard());

        if (previousScoreboard != null) {
            player.setScoreboard(previousScoreboard);
            return;
        }

        if (isManagedScoreboard(player.getScoreboard()))
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private static Scoreboard createManagedScoreboard(Player player) {
        rememberPreviousScoreboard(player);

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        copyTeams(getSourceScoreboard(player), scoreboard);
        managedScoreboards.add(scoreboard);
        return scoreboard;
    }

    private static void rememberPreviousScoreboard(Player player) {
        if (previousScoreboards.containsKey(player.getUniqueId())) return;

        Scoreboard scoreboard = player.getScoreboard();
        if (isManagedScoreboard(scoreboard)) return;

        previousScoreboards.put(player.getUniqueId(), scoreboard);
    }

    private static Scoreboard getSourceScoreboard(Player player) {
        Scoreboard previousScoreboard = previousScoreboards.get(player.getUniqueId());
        return previousScoreboard == null ? player.getScoreboard() : previousScoreboard;
    }

    private static boolean isManagedScoreboard(Scoreboard scoreboard) {
        return scoreboard != null &&
                (managedScoreboards.contains(scoreboard) || scoreboard.getObjective(SIDEBAR_OBJECTIVE) != null);
    }

    private static void setSidebarLines(Objective objective, Scoreboard scoreboard, List<String> scoreboardContents) {
        if (scoreboardContents == null) return;
        int lineCount = Math.min(scoreboardContents.size(), MAX_SIDEBAR_LINES);
        Set<String> entries = new HashSet<>();
        for (int i = 0; i < lineCount; i++) {
            String entry = trimScoreboardEntry(scoreboardContents.get(i));
            Score score = objective.getScore(entry);
            score.setScore(i);
            entries.add(entry);
        }
        managedSidebarEntries.put(scoreboard, entries);
    }

    private static void clearSidebarLines(Scoreboard scoreboard) {
        Set<String> entries = managedSidebarEntries.remove(scoreboard);
        if (entries == null) return;
        for (String entry : entries) {
            Map<Objective, Integer> otherScores = new HashMap<>();
            for (Objective objective : scoreboard.getObjectives()) {
                if (SIDEBAR_OBJECTIVE.equals(objective.getName())) continue;
                Score score = objective.getScore(entry);
                if (score.isScoreSet()) otherScores.put(objective, score.getScore());
            }
            scoreboard.resetScores(entry);
            otherScores.forEach((objective, value) -> objective.getScore(entry).setScore(value));
        }
    }

    private static String trimScoreboardEntry(String entry) {
        if (entry == null) return "";
        if (entry.length() <= LEGACY_SCOREBOARD_ENTRY_LIMIT) return entry;
        return entry.substring(0, LEGACY_SCOREBOARD_ENTRY_LIMIT - 1);
    }

    private static void copyTeams(Scoreboard sourceScoreboard, Scoreboard targetScoreboard) {
        if (sourceScoreboard == null || targetScoreboard == null) return;

        for (Team sourceTeam : sourceScoreboard.getTeams()) {
            Team targetTeam = targetScoreboard.getTeam(sourceTeam.getName());
            if (targetTeam == null)
                targetTeam = targetScoreboard.registerNewTeam(sourceTeam.getName());

            copyTeamSettings(sourceTeam, targetTeam);
            for (String entry : sourceTeam.getEntries())
                targetTeam.addEntry(entry);
        }
    }

    private static void copyTeamSettings(Team sourceTeam, Team targetTeam) {
        targetTeam.setDisplayName(sourceTeam.getDisplayName());
        targetTeam.setPrefix(sourceTeam.getPrefix());
        targetTeam.setSuffix(sourceTeam.getSuffix());
        ChatColor teamColor = sourceTeam.getColor();
        // RESET is the Bukkit default for teams without an explicit color. CraftBukkit 26.2
        // cannot translate RESET to an NMS TextColor, so leave the new team's default intact.
        if (teamColor != null && teamColor.isColor()) targetTeam.setColor(teamColor);
        targetTeam.setAllowFriendlyFire(sourceTeam.allowFriendlyFire());
        targetTeam.setCanSeeFriendlyInvisibles(sourceTeam.canSeeFriendlyInvisibles());

        copyTeamOption(sourceTeam, targetTeam, Team.Option.NAME_TAG_VISIBILITY);
        copyTeamOption(sourceTeam, targetTeam, Team.Option.DEATH_MESSAGE_VISIBILITY);
        copyTeamOption(sourceTeam, targetTeam, Team.Option.COLLISION_RULE);
    }

    private static void copyTeamOption(Team sourceTeam, Team targetTeam, Team.Option option) {
        targetTeam.setOption(option, sourceTeam.getOption(option));
    }
}
