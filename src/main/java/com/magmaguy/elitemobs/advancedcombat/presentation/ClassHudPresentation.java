package com.magmaguy.elitemobs.advancedcombat.presentation;

import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Keeps vitals visible and adds occasional reminders only while those vitals are stable. */
public final class ClassHudPresentation {
    private static final long REMINDER_INTERVAL_NANOS = Duration.ofMinutes(3).toNanos();
    private static final long NANOS_PER_TICK = 50_000_000L;

    private final Map<UUID, DisplayState> players = new HashMap<>();

    public String render(UUID playerId, Vitals vitals, String compact, String reminder, long now) {
        DisplayState state = players.get(playerId);
        if (state == null || !state.vitals.equals(vitals)) {
            players.put(playerId, new DisplayState(vitals, now));
            return compact;
        }

        String expanded = compact + " &8| " + reminder;
        if (now - state.lastReminderOrChange >= REMINDER_INTERVAL_NANOS) {
            state.lastReminderOrChange = now;
            state.reminderUntil = now
                    + ActionBarCompositor.readingDurationTicks(expanded) * NANOS_PER_TICK;
            state.showingReminder = true;
        }
        if (state.showingReminder && now - state.reminderUntil >= 0L)
            state.showingReminder = false;
        return state.showingReminder ? expanded : compact;
    }

    public void discard(UUID playerId) {
        players.remove(playerId);
    }

    public void clear() {
        players.clear();
    }

    /** Raw values detect changes even when rounded HUD numbers stay the same. */
    public record Vitals(double health, double maximumHealth, double absorption,
                         double resource, double maximumResource, String classFormId) {
    }

    private static final class DisplayState {
        private final Vitals vitals;
        private long lastReminderOrChange;
        private long reminderUntil;
        private boolean showingReminder;

        private DisplayState(Vitals vitals, long now) {
            this.vitals = vitals;
            this.lastReminderOrChange = now;
        }
    }
}
