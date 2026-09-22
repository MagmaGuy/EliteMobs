package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public class SimpleScoreboard {
    private static final String SIDEBAR_OBJECTIVE = "em_quest_sb";
    private static final Map<UUID, Scoreboard> previousScoreboards = new ConcurrentHashMap<>();
    private static final Set<Scoreboard> managedScoreboards = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<UUID, Scoreboard> ownedScoreboards = new HashMap<>();
    private static final Map<UUID, BukkitTask> expiryTasks = new HashMap<>();

    public static Scoreboard lazyScoreboard(Player player, String displayName, List<String> scoreboardContents) {
        cancelExpiry(player);
        Scoreboard scoreboard = createManagedScoreboard(player);
        Objective objective = ScoreboardUtil.registerSidebarObjective(MetadataHandler.PLUGIN, scoreboard, SIDEBAR_OBJECTIVE, displayName);
        ScoreboardUtil.setSidebarLines(objective, scoreboardContents);
        player.setScoreboard(scoreboard);
        ownedScoreboards.put(player.getUniqueId(), scoreboard);
        return scoreboard;
    }

    /** Reuses the current EliteMobs scoreboard when possible, avoiding a new board allocation for periodic UI updates. */
    public static Scoreboard updateScoreboard(Player player, String displayName, List<String> scoreboardContents) {
        cancelExpiry(player);
        Scoreboard scoreboard = player.getScoreboard();
        if (ownedScoreboards.get(player.getUniqueId()) != scoreboard) return lazyScoreboard(player, displayName, scoreboardContents);
        Objective existing = scoreboard.getObjective(SIDEBAR_OBJECTIVE);
        Objective objective;
        if (existing == null) {
            objective = ScoreboardUtil.registerSidebarObjective(
                    MetadataHandler.PLUGIN, scoreboard, SIDEBAR_OBJECTIVE, displayName);
        } else {
            // Party health changes frequently. Keep the same internal objective instead of
            // unregistering it every refresh, which makes clients visibly blink the sidebar.
            if (!existing.getDisplayName().equals(displayName)) existing.setDisplayName(displayName);
            existing.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective = existing;
        }
        ScoreboardUtil.setSidebarLines(objective, scoreboardContents);
        return scoreboard;
    }

    /** Lets periodic UI owners avoid rebuilding an unchanged EliteMobs sidebar objective. */
    public static boolean hasManagedSidebar(Player player) {
        if (player == null || ownedScoreboards.get(player.getUniqueId()) != player.getScoreboard()) return false;
        Objective objective = player.getScoreboard().getObjective(SIDEBAR_OBJECTIVE);
        return objective != null && objective.equals(player.getScoreboard().getObjective(DisplaySlot.SIDEBAR));
    }

    public static Scoreboard temporaryScoreboard(Player player, String displayName, List<String> scoreboardContents, int ticksTimeout) {
        Scoreboard scoreboard = updateScoreboard(player, displayName, scoreboardContents);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                clearScoreboard(player);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticksTimeout);
        expiryTasks.put(player.getUniqueId(), task);

        return scoreboard;
    }

    public static Scoreboard blankScoreboard(Player player) {
        cancelExpiry(player);
        Scoreboard scoreboard = createManagedScoreboard(player);
        player.setScoreboard(scoreboard);
        ownedScoreboards.put(player.getUniqueId(), scoreboard);
        return scoreboard;
    }

    public static void clearScoreboard(Player player) {
        cancelExpiry(player);
        Scoreboard owned = ownedScoreboards.remove(player.getUniqueId());
        Scoreboard previousScoreboard = previousScoreboards.remove(player.getUniqueId());
        if (!player.isOnline() || owned != player.getScoreboard()) return;

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
        Scoreboard scoreboard = player.getScoreboard();
        if (ownedScoreboards.get(player.getUniqueId()) == scoreboard) return;

        previousScoreboards.put(player.getUniqueId(), scoreboard);
    }

    private static Scoreboard getSourceScoreboard(Player player) {
        Scoreboard previousScoreboard = previousScoreboards.get(player.getUniqueId());
        return previousScoreboard == null ? player.getScoreboard() : previousScoreboard;
    }

    private static boolean isManagedScoreboard(Scoreboard scoreboard) {
        return scoreboard != null && managedScoreboards.contains(scoreboard);
    }

    private static void cancelExpiry(Player player) {
        BukkitTask task = expiryTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    public static void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) clearScoreboard(player);
        expiryTasks.values().forEach(BukkitTask::cancel);
        expiryTasks.clear();
        ownedScoreboards.clear();
        previousScoreboards.clear();
        managedScoreboards.clear();
    }

    public static class Events implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            clearScoreboard(event.getPlayer());
        }
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
