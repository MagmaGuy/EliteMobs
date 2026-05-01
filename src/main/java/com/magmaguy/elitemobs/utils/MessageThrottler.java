package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.magmacore.util.Round;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregates bursty plugin messages so players receive one summary chat line after a quiet period
 * instead of a flood. Cooldown window is {@link DefaultConfig#getNotificationThrottleSeconds()}.
 *
 * Policies:
 *   CURRENCY       — sum amounts; flushed message uses existing chatCurrencyShowerMessage template.
 *   NO_XP          — count occurrences; flushed message is the latest rendered message, suffixed with " (xN)" when N > 1.
 *   QUEST_PROGRESS — keep latest rendered message; keyed per objective so concurrent quests don't clobber each other.
 */
public final class MessageThrottler {
    private static final Map<UUID, Map<String, PendingMessage>> pending = new HashMap<>();
    private static BukkitTask task;

    private MessageThrottler() {}

    public static void start() {
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, MessageThrottler::tick, 20L, 20L);
    }

    public static void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        pending.clear();
    }

    /**
     * Records a currency pickup for the player and returns the new running total of the pending batch.
     * Callers can use the returned value to drive live action-bar feedback that mirrors the batch.
     */
    public static double pushCurrency(Player player, double amount) {
        if (player == null) return 0;
        PendingMessage msg = getOrCreate(player, "currency", PolicyType.CURRENCY);
        msg.accumulatedAmount += amount;
        msg.touch();
        return msg.accumulatedAmount;
    }

    public static void pushNoXp(Player player, String renderedMessage) {
        if (player == null || renderedMessage == null) return;
        PendingMessage msg = getOrCreate(player, "no_xp", PolicyType.NO_XP);
        msg.count++;
        msg.lastMessage = renderedMessage;
        msg.touch();
    }

    public static void pushQuestProgress(Player player, Objective objective, String renderedMessage) {
        if (player == null || objective == null || renderedMessage == null) return;
        String key = "quest:" + System.identityHashCode(objective);
        PendingMessage msg = getOrCreate(player, key, PolicyType.QUEST);
        msg.lastMessage = renderedMessage;
        msg.touch();
    }

    private static PendingMessage getOrCreate(Player player, String key, PolicyType policy) {
        return pending.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .computeIfAbsent(key, k -> new PendingMessage(policy));
    }

    private static void tick() {
        if (pending.isEmpty()) return;
        long now = System.currentTimeMillis();
        long quietMs = Math.max(1000L, DefaultConfig.getNotificationThrottleSeconds() * 1000L);

        Iterator<Map.Entry<UUID, Map<String, PendingMessage>>> playerIt = pending.entrySet().iterator();
        while (playerIt.hasNext()) {
            Map.Entry<UUID, Map<String, PendingMessage>> playerEntry = playerIt.next();
            Player player = Bukkit.getPlayer(playerEntry.getKey());
            if (player == null || !player.isOnline()) {
                playerIt.remove();
                continue;
            }
            Iterator<PendingMessage> msgIt = playerEntry.getValue().values().iterator();
            while (msgIt.hasNext()) {
                PendingMessage msg = msgIt.next();
                if (now - msg.lastUpdatedMs >= quietMs) {
                    msg.flush(player);
                    msgIt.remove();
                }
            }
            if (playerEntry.getValue().isEmpty()) playerIt.remove();
        }
    }

    private enum PolicyType {CURRENCY, NO_XP, QUEST}

    private static final class PendingMessage {
        final PolicyType policy;
        long lastUpdatedMs = System.currentTimeMillis();
        double accumulatedAmount;
        int count;
        String lastMessage;

        PendingMessage(PolicyType policy) {
            this.policy = policy;
        }

        void touch() {
            this.lastUpdatedMs = System.currentTimeMillis();
        }

        void flush(Player player) {
            switch (policy) {
                case CURRENCY -> {
                    player.sendMessage(EconomySettingsConfig.getChatCurrencyShowerMessage()
                            .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                            .replace("$amount", Round.twoDecimalPlaces(accumulatedAmount) + ""));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(EconomySettingsConfig.getAdventurersGuildNotificationMessage()));
                }
                case NO_XP -> {
                    String suffix = count > 1 ? " (x" + count + ")" : "";
                    player.sendMessage(lastMessage + suffix);
                }
                case QUEST -> player.sendMessage(lastMessage);
            }
        }
    }
}
