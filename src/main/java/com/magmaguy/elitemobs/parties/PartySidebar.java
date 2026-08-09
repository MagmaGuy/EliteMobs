package com.magmaguy.elitemobs.parties;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Owns the combined party and quest sidebar while a player belongs to a party. */
public final class PartySidebar {
    private static final int MAX_LINES = 15;
    private static final Map<UUID, TemporaryQuestView> temporaryQuestViews = new HashMap<>();
    private static BukkitTask refreshTask;
    private static boolean showInviteAction = true;

    private PartySidebar() {
    }

    static void initialize() {
        if (refreshTask != null) refreshTask.cancel();
        long period = 20L * PartyConfig.getSidebarRotationSeconds();
        refreshTask = new BukkitRunnable() {
            @Override
            public void run() {
                showInviteAction = !showInviteAction;
                PartyManager.cleanupExpiredInvites();
                cleanupExpiredViews();
                PartyManager.getParties().values().forEach(party -> party.getMembers().forEach(playerId -> {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) refresh(player);
                }));
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, period, period);
    }

    static void shutdown() {
        if (refreshTask != null) refreshTask.cancel();
        refreshTask = null;
        temporaryQuestViews.clear();
        showInviteAction = true;
    }

    public static void showTemporaryQuest(Player player, String questName, List<String> questLines, int ticksTimeout) {
        if (!PartyManager.isInParty(player.getUniqueId())) return;
        UUID token = UUID.randomUUID();
        temporaryQuestViews.put(player.getUniqueId(),
                new TemporaryQuestView(token, questName, List.copyOf(questLines),
                        System.nanoTime() + ticksTimeout * 50_000_000L));
        refresh(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                TemporaryQuestView current = temporaryQuestViews.get(player.getUniqueId());
                if (current == null || !current.token().equals(token)) return;
                temporaryQuestViews.remove(player.getUniqueId());
                if (player.isOnline() && PartyManager.isInParty(player.getUniqueId())) refresh(player);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticksTimeout);
    }

    public static void refresh(Player player) {
        Party party = PartyManager.getParty(player.getUniqueId());
        if (party == null || !player.isOnline()) return;
        if (QuestsConfig.isHideQuestScoreboardDuringQuestDialogue()
                && QuestDialogueBossBarManager.hasActiveSession(player)) return;

        List<String> lines = new ArrayList<>();
        for (UUID memberId : party.getMembersInDisplayOrder()) {
            Player member = Bukkit.getPlayer(memberId);
            String template = memberId.equals(party.getLeader())
                    ? PartyConfig.getSidebarLeaderLine()
                    : PartyConfig.getSidebarMemberLine();
            lines.add(color(template.replace("$player", member == null ? "Unknown" : member.getName())));
        }

        QuestView questView = currentQuestView(player);
        int reservedActionLines = 1;
        if (questView != null && lines.size() + reservedActionLines < MAX_LINES) {
            lines.add(color(PartyConfig.getSidebarQuestLine().replace("$quest", questView.name())));
            int availableQuestLines = MAX_LINES - lines.size() - reservedActionLines;
            for (String questLine : questView.lines()) {
                if (availableQuestLines-- <= 0) break;
                lines.add(questLine);
            }
        }

        lines.add(color(showInviteAction
                ? PartyConfig.getSidebarInviteAction()
                : PartyConfig.getSidebarLeaveAction()));
        // Bukkit renders higher scores above lower ones. SimpleScoreboard assigns ascending
        // scores, so reverse this presentation list to keep the leader at the top and action at
        // the bottom without changing the established quest-scoreboard ordering globally.
        java.util.Collections.reverse(lines);
        SimpleScoreboard.updateScoreboard(player, color(PartyConfig.getSidebarTitle()), lines);
    }

    static void clearPlayer(Player player) {
        temporaryQuestViews.remove(player.getUniqueId());
        SimpleScoreboard.clearScoreboard(player);
        QuestTracking tracking = QuestTracking.getPlayerTrackingQuests().get(player.getUniqueId());
        if (tracking != null) tracking.refreshScoreboard();
    }

    private static QuestView currentQuestView(Player player) {
        TemporaryQuestView temporary = temporaryQuestViews.get(player.getUniqueId());
        if (temporary != null) {
            if (temporary.expiresAtNanos() > System.nanoTime())
                return new QuestView(temporary.name(), temporary.lines());
            temporaryQuestViews.remove(player.getUniqueId());
        }

        QuestTracking tracking = QuestTracking.getPlayerTrackingQuests().get(player.getUniqueId());
        if (tracking == null) return null;
        return new QuestView(
                tracking.getCustomQuest().getQuestName(),
                tracking.getCustomQuest().getQuestObjectives().getScoreboardObjectiveText());
    }

    private static void cleanupExpiredViews() {
        long now = System.nanoTime();
        temporaryQuestViews.entrySet().removeIf(entry -> entry.getValue().expiresAtNanos() <= now);
    }

    private static String color(String value) {
        return ChatColorConverter.convert(value == null ? "" : value);
    }

    private record QuestView(String name, List<String> lines) {
    }

    private record TemporaryQuestView(UUID token, String name, List<String> lines, long expiresAtNanos) {
    }
}
