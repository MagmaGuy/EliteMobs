package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GameClock {

    private static final Map<Integer, ScheduledEntry> scheduledEntries = new LinkedHashMap<>();
    private static BukkitTask clockTask = null;
    private static long currentTick = 0L;
    private static int nextTaskId = 1;

    private GameClock() {
    }

    public static void initialize() {
        if (clockTask != null) {
            return;
        }
        clockTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, GameClock::tick, 1L, 1L);
    }

    public static void shutdown() {
        if (clockTask != null) {
            clockTask.cancel();
            clockTask = null;
        }
        scheduledEntries.clear();
        currentTick = 0L;
        nextTaskId = 1;
    }

    public static long getCurrentTick() {
        return currentTick;
    }

    public static int scheduleLater(long delayTicks, Runnable runnable) {
        return scheduleRepeating(delayTicks, 0L, runnable);
    }

    public static int scheduleRepeating(long initialDelayTicks, long intervalTicks, Runnable runnable) {
        initialize();
        int taskId = nextTaskId++;
        long normalizedInitialDelay = Math.max(1L, initialDelayTicks);
        long normalizedInterval = Math.max(0L, intervalTicks);
        scheduledEntries.put(taskId, new ScheduledEntry(taskId, runnable, currentTick + normalizedInitialDelay, normalizedInterval));
        return taskId;
    }

    public static void cancel(int taskId) {
        scheduledEntries.remove(taskId);
    }

    private static void tick() {
        currentTick++;
        ArrayList<ScheduledEntry> dueEntries = new ArrayList<>();
        for (ScheduledEntry scheduledEntry : new ArrayList<>(scheduledEntries.values())) {
            if (scheduledEntry.nextRunTick <= currentTick) {
                dueEntries.add(scheduledEntry);
            }
        }

        for (ScheduledEntry scheduledEntry : dueEntries) {
            if (!scheduledEntries.containsKey(scheduledEntry.taskId)) {
                continue;
            }
            try {
                scheduledEntry.runnable.run();
            } catch (Exception exception) {
                Logger.warn("A GameClock task failed and was cancelled.");
                exception.printStackTrace();
                scheduledEntries.remove(scheduledEntry.taskId);
                continue;
            }

            if (!scheduledEntries.containsKey(scheduledEntry.taskId)) {
                continue;
            }

            if (scheduledEntry.intervalTicks < 1L) {
                scheduledEntries.remove(scheduledEntry.taskId);
            } else {
                scheduledEntry.nextRunTick = currentTick + scheduledEntry.intervalTicks;
            }
        }
    }

    private static final class ScheduledEntry {
        private final int taskId;
        private final Runnable runnable;
        private long nextRunTick;
        private final long intervalTicks;

        private ScheduledEntry(int taskId, Runnable runnable, long nextRunTick, long intervalTicks) {
            this.taskId = taskId;
            this.runnable = runnable;
            this.nextRunTick = nextRunTick;
            this.intervalTicks = intervalTicks;
        }
    }
}
