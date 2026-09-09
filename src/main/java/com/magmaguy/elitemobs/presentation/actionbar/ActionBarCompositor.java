package com.magmaguy.elitemobs.presentation.actionbar;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Owns EliteMobs' single action-bar output channel.
 * <p>
 * Callers publish independent source messages instead of writing to the client directly. The
 * highest-priority live message is displayed, and a lower-priority message automatically resumes
 * when the message above it expires. Entries at the same priority are ordered by their most recent
 * publication. This is particularly important for the persistent class HUD: combat feedback can
 * cover it temporarily without destroying it.
 * <p>
 * All state and player interaction is confined to the server thread. Calls made off-thread are
 * queued and drained by the same global task that expires and renders messages.
 */
public final class ActionBarCompositor implements Listener {

    /**
     * A source owns its arbitration priority, normal lifetime, and the component encoding used by
     * the producer it replaces. Lockout subtitles historically used a raw {@link TextComponent};
     * the other action bars historically parsed legacy color codes.
     */
    public enum Source {
        CRITICAL_WARNING(500, 60, Encoding.LEGACY),
        LOCKOUT(500, 60, Encoding.RAW),
        SCRIPT(400, 60, Encoding.LEGACY),
        LUA(400, 60, Encoding.LEGACY),
        ABILITY_INPUT(310, 40, Encoding.LEGACY),
        /** Long-lived so ordinary cast and combat feedback cannot instantly replace it. */
        AFFINITY_WARNING(350, 200, Encoding.LEGACY),
        COMBAT_TRANSITION(300, 60, Encoding.LEGACY),
        SKILL_FEEDBACK(300, 60, Encoding.LEGACY),
        LOOT(200, 60, Encoding.LEGACY),
        ECONOMY(200, 60, Encoding.LEGACY),
        CLASS_HUD(100, -1L, Encoding.LEGACY),
        /** Persistent controls appended to the winning message instead of competing with feedback. */
        CONTEXT_HINT(0, -1L, Encoding.LEGACY);

        private static final long PERSISTENT = -1;

        private final int priority;
        private final long defaultDurationTicks;
        private final Encoding encoding;

        Source(int priority, long defaultDurationTicks, Encoding encoding) {
            this.priority = priority;
            this.defaultDurationTicks = defaultDurationTicks;
            this.encoding = encoding;
        }

        public int priority() {
            return priority;
        }

        public long defaultDurationTicks() {
            return defaultDurationTicks;
        }

        public boolean isPersistent() {
            return defaultDurationTicks == PERSISTENT;
        }
    }

    private enum Encoding {
        LEGACY,
        RAW,
        HUD_PROBE
    }

    private static final long KEEPALIVE_INTERVAL_TICKS = 40;
    private static final Map<UUID, PlayerState> playerStates = new HashMap<>();
    private static final ConcurrentLinkedQueue<Mutation> pendingMutations = new ConcurrentLinkedQueue<>();

    private static ActionBarCompositor listener;
    private static BukkitTask task;
    private static long currentTick;
    private static long nextSequence;

    private ActionBarCompositor() {
    }

    /** Starts the compositor and its quit listener. Safe to call more than once. */
    public static void initialize() {
        start();
    }

    /** Starts the compositor and its quit listener. Safe to call more than once. */
    public static void start() {
        if (task != null) return;
        requirePrimaryThread("start");

        listener = new ActionBarCompositor();
        Bukkit.getPluginManager().registerEvents(listener, MetadataHandler.PLUGIN);
        task = Bukkit.getScheduler().runTaskTimer(
                MetadataHandler.PLUGIN,
                ActionBarCompositor::tick,
                1L,
                1L);
    }

    /**
     * Publishes a message for the source's normal lifetime, extended when the text needs longer
     * to read. Explicit-duration publications are never adjusted.
     */
    public static void show(Player player, Source source, String message) {
        long duration = source.isPersistent()
                ? source.defaultDurationTicks()
                : Math.max(source.defaultDurationTicks(), readingDurationTicks(message));
        publish(player, source, message, duration, true);
    }

    /**
     * Ticks a slow reader needs for this message. The slow end of adult silent reading sits near
     * 150 words per minute (the average is roughly 240), so each word gets eight ticks plus a
     * one-second orientation buffer. Short messages keep their source's normal lifetime through
     * the caller's floor; the cap keeps a long message from parking over the class HUD.
     */
    public static long readingDurationTicks(String message) {
        String stripped = org.bukkit.ChatColor.stripColor(org.bukkit.ChatColor
                .translateAlternateColorCodes('&', Objects.requireNonNullElse(message, "")));
        int words = 0;
        for (String token : stripped.trim().split("\\s+"))
            if (!token.isBlank()) words++;
        return Math.min(300L, 20L + words * 8L);
    }

    /**
     * Publishes a transient message for an explicit number of server ticks.
     *
     * @param durationTicks positive lifetime in ticks
     */
    public static void show(Player player, Source source, String message, long durationTicks) {
        if (durationTicks <= 0)
            throw new IllegalArgumentException("Action-bar duration must be positive");
        publish(player, source, message, durationTicks, false);
    }

    /** Removes one source without disturbing messages owned by other sources. */
    public static void clear(Player player, Source source) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(source, "source");
        dispatch(new ClearMutation(player.getUniqueId(), source));
    }

    /** Opt-in admin calibration, cleared on logout. Ordinary sources resume when the probe is removed. */
    public static void setHudProbe(Player player, CombatHudProbe probe) {
        requirePrimaryThread("setHudProbe");
        PlayerState state = playerStates.computeIfAbsent(player.getUniqueId(), ignored -> new PlayerState());
        state.hudProbe = probe;
        render(player.getUniqueId());
    }

    /** Stops rendering and releases every retained player message. Safe to call more than once. */
    public static void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (listener != null) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }
        playerStates.clear();
        pendingMutations.clear();
        currentTick = 0;
        nextSequence = 0;
    }

    private static void publish(Player player, Source source, String message, long durationTicks, boolean readingTime) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(message, "message");
        dispatch(new ShowMutation(player.getUniqueId(), source, message, durationTicks, readingTime));
    }

    private static void dispatch(Mutation mutation) {
        if (Bukkit.isPrimaryThread()) {
            apply(mutation);
            if (task != null) render(mutation.playerId());
            return;
        }
        pendingMutations.add(mutation);
    }

    private static void tick() {
        currentTick++;

        Mutation mutation;
        while ((mutation = pendingMutations.poll()) != null)
            apply(mutation);

        Iterator<UUID> iterator = playerStates.keySet().iterator();
        while (iterator.hasNext()) {
            UUID playerId = iterator.next();
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                iterator.remove();
                continue;
            }

            PlayerState state = playerStates.get(playerId);
            removeExpired(state);
            render(player, state);
            if (state.hudProbe == null && state.entries.isEmpty() && !state.hasRenderedMessage)
                iterator.remove();
        }
    }

    private static void apply(Mutation mutation) {
        PlayerState state = playerStates.computeIfAbsent(mutation.playerId(), ignored -> new PlayerState());
        if (mutation instanceof ShowMutation show) {
            long duration = show.durationTicks;
            if (state.hudProbe != null && show.readingTime && !show.source.isPersistent())
                duration = Math.min(600L, Math.max(120L, duration * 2));
            long expiresAtTick = duration == Source.PERSISTENT
                    ? Long.MAX_VALUE
                    : saturatingAdd(currentTick, duration);
            state.entries.put(show.source,
                    new Entry(show.source, show.message, expiresAtTick, ++nextSequence));
        } else if (mutation instanceof ClearMutation clear) {
            state.entries.remove(clear.source);
        }
    }

    private static void render(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            playerStates.remove(playerId);
            return;
        }
        PlayerState state = playerStates.get(playerId);
        if (state == null) return;
        removeExpired(state);
        render(player, state);
        if (state.hudProbe == null && state.entries.isEmpty() && !state.hasRenderedMessage)
            playerStates.remove(playerId);
    }

    private static void render(Player player, PlayerState state) {
        if (state.hudProbe != null) {
            String text = state.hudProbe.text(player, ExperimentalCombatModule.isAbilityGestureOpen(player.getUniqueId()));
            Entry feedback = selectWinner(state, true);
            String feedbackText = feedback == null ? null : feedback.message;
            Encoding feedbackEncoding = feedback == null ? null : feedback.source.encoding;
            if (!state.hasRenderedMessage || state.lastEncoding != Encoding.HUD_PROBE
                    || !Objects.equals(feedbackText, state.lastFeedback)
                    || feedbackEncoding != state.lastFeedbackEncoding
                    || !text.equals(state.lastMessage)
                    || currentTick - state.lastSentAtTick >= KEEPALIVE_INTERVAL_TICKS) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, state.hudProbe.component(
                        text, feedbackText, feedbackEncoding == Encoding.LEGACY));
                state.hasRenderedMessage = true;
                state.lastMessage = text;
                state.lastFeedback = feedbackText;
                state.lastFeedbackEncoding = feedbackEncoding;
                state.lastEncoding = Encoding.HUD_PROBE;
                state.lastSentAtTick = currentTick;
            }
            return;
        }
        Entry winner = selectWinner(state, false);
        if (winner == null) {
            if (state.hasRenderedMessage) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                state.hasRenderedMessage = false;
                state.lastMessage = null;
                state.lastEncoding = null;
            }
            return;
        }

        Entry hint = state.entries.get(Source.CONTEXT_HINT);
        String suffix = hint == null || hint == winner ? "" : " §8| §r" + hint.message;
        String message = winner.message + suffix;
        boolean payloadChanged = !state.hasRenderedMessage
                || !message.equals(state.lastMessage)
                || winner.source.encoding != state.lastEncoding;
        boolean keepaliveDue = currentTick - state.lastSentAtTick >= KEEPALIVE_INTERVAL_TICKS;
        if (!payloadChanged && !keepaliveDue) return;

        if (winner.source.encoding == Encoding.LEGACY)
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        else {
            TextComponent payload = new TextComponent(winner.message);
            if (!suffix.isEmpty())
                for (var component : TextComponent.fromLegacyText(suffix)) payload.addExtra(component);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, payload);
        }

        state.hasRenderedMessage = true;
        state.lastMessage = message;
        state.lastEncoding = winner.source.encoding;
        state.lastSentAtTick = currentTick;
    }

    private static Entry selectWinner(PlayerState state, boolean feedbackOnly) {
        Entry winner = null;
        for (Entry candidate : state.entries.values()) {
            // Persistent vitals and the open-gesture controls are already represented by the HUD.
            if (feedbackOnly && (candidate.source.isPersistent() || candidate.source == Source.ABILITY_INPUT))
                continue;
            if (winner == null
                    || candidate.source.priority > winner.source.priority
                    || (candidate.source.priority == winner.source.priority
                    && candidate.sequence > winner.sequence))
                winner = candidate;
        }
        return winner;
    }

    private static void removeExpired(PlayerState state) {
        state.entries.values().removeIf(entry -> entry.expiresAtTick <= currentTick);
    }

    private static long saturatingAdd(long left, long right) {
        if (Long.MAX_VALUE - left < right) return Long.MAX_VALUE;
        return left + right;
    }

    private static void requirePrimaryThread(String operation) {
        if (!Bukkit.isPrimaryThread())
            throw new IllegalStateException("ActionBarCompositor." + operation + " must run on the server thread");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerStates.remove(event.getPlayer().getUniqueId());
    }

    private sealed interface Mutation permits ShowMutation, ClearMutation {
        UUID playerId();
    }

    private record ShowMutation(UUID playerId, Source source, String message, long durationTicks, boolean readingTime)
            implements Mutation {
    }

    private record ClearMutation(UUID playerId, Source source) implements Mutation {
    }

    private record Entry(Source source, String message, long expiresAtTick, long sequence) {
    }

    private static final class PlayerState {
        private CombatHudProbe hudProbe;
        private final EnumMap<Source, Entry> entries = new EnumMap<>(Source.class);
        private boolean hasRenderedMessage;
        private String lastMessage;
        private String lastFeedback;
        private Encoding lastFeedbackEncoding;
        private Encoding lastEncoding;
        private long lastSentAtTick;
    }
}
