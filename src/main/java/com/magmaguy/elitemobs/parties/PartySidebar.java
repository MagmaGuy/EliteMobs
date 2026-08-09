package com.magmaguy.elitemobs.parties;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
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
    private static final int HEALTH_SEGMENTS = 5;
    private static final long REFRESH_PERIOD_TICKS = 20L;
    private static final Map<UUID, TemporaryQuestView> temporaryQuestViews = new HashMap<>();
    private static final Map<UUID, SidebarSnapshot> lastRenderedSidebars = new HashMap<>();
    private static BukkitTask refreshTask;
    private static boolean showInviteAction = true;
    private static int secondsSinceActionRotation = 0;

    private PartySidebar() {
    }

    /** Whether the party UI, rather than the normal quest UI, currently owns the sidebar. */
    public static boolean isEnabled() {
        return PartyConfig.isEnabled() && PartyConfig.isSidebarEnabled();
    }

    static void initialize() {
        if (refreshTask != null) refreshTask.cancel();
        secondsSinceActionRotation = 0;
        refreshTask = new BukkitRunnable() {
            @Override
            public void run() {
                PartyManager.cleanupExpiredInvites();
                cleanupExpiredViews();
                if (!isEnabled()) return;
                if (++secondsSinceActionRotation >= PartyConfig.getSidebarRotationSeconds()) {
                    showInviteAction = !showInviteAction;
                    secondsSinceActionRotation = 0;
                }
                PartyManager.getParties().values().forEach(party -> party.getMembers().forEach(playerId -> {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) refresh(player);
                }));
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, REFRESH_PERIOD_TICKS, REFRESH_PERIOD_TICKS);
    }

    static void shutdown() {
        if (refreshTask != null) refreshTask.cancel();
        refreshTask = null;
        temporaryQuestViews.clear();
        lastRenderedSidebars.clear();
        showInviteAction = true;
        secondsSinceActionRotation = 0;
    }

    public static void showTemporaryQuest(Player player, String questName, List<String> questLines, int ticksTimeout) {
        if (!isEnabled() || !PartyManager.isInParty(player.getUniqueId())) return;
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
        try {
            refreshInternal(player);
        } catch (RuntimeException exception) {
            Logger.warn("Failed to refresh the party sidebar for " + player.getName() + ".");
            exception.printStackTrace();
        }
    }

    private static void refreshInternal(Player player) {
        if (!isEnabled()) return;
        Party party = PartyManager.getParty(player.getUniqueId());
        if (party == null || !player.isOnline()) return;
        if (QuestsConfig.isHideQuestScoreboardDuringQuestDialogue()
                && QuestDialogueBossBarManager.hasActiveSession(player)) return;

        DungeonInstance dungeonInstance = getDungeonInstance(player);
        List<String> lines = new ArrayList<>();
        for (UUID memberId : party.getMembersInDisplayOrder()) {
            Player member = Bukkit.getPlayer(memberId);
            String template = memberId.equals(party.getLeader())
                    ? PartyConfig.getSidebarLeaderLine()
                    : PartyConfig.getSidebarMemberLine();
            lines.add(renderMemberLine(template, member, dungeonInstance));
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
        String title = color(PartyConfig.getSidebarTitle());
        SidebarSnapshot snapshot = new SidebarSnapshot(title, List.copyOf(lines));
        if (snapshot.equals(lastRenderedSidebars.get(player.getUniqueId()))
                && SimpleScoreboard.hasManagedSidebar(player))
            return;
        SimpleScoreboard.updateScoreboard(player, title, lines);
        lastRenderedSidebars.put(player.getUniqueId(), snapshot);
    }

    private static String renderMemberLine(String template, Player member, DungeonInstance dungeonInstance) {
        boolean downed = member != null
                && dungeonInstance != null
                && dungeonInstance.isSpectator(member)
                && dungeonInstance.getRemainingLives(member) != null;
        String health = downed ? PartyConfig.getSidebarDownedDisplay() : renderHealth(member);
        String lives = renderLives(member, dungeonInstance);
        boolean hasHealthPlaceholder = template.contains("$health");
        boolean hasLivesPlaceholder = template.contains("$lives");
        String rendered = template
                .replace("$player", member == null ? PartyConfig.getUnknownPlayerName() : member.getName())
                .replace("$health", health)
                .replace("$lives", lives);
        // Preserve the new information for existing Party.yml files whose customized line
        // templates predate the placeholders.
        if (!hasHealthPlaceholder) rendered += health;
        if (!hasLivesPlaceholder) rendered += lives;
        return color(rendered);
    }

    private static String renderHealth(Player member) {
        double healthFraction = 0D;
        if (member != null && member.isOnline() && member.isValid()) {
            double maximumHealth = Math.max(1D,
                    AttributeManager.getAttributeValue(member, "generic_max_health"));
            healthFraction = Math.max(0D, Math.min(1D, member.getHealth() / maximumHealth));
        }

        int filledSegments = healthFraction <= 0D
                ? 0
                : Math.max(1, (int) Math.ceil(healthFraction * HEALTH_SEGMENTS));
        String filledColor = healthFraction > 2D / 3D
                ? PartyConfig.getSidebarHealthHealthyColor()
                : healthFraction > 1D / 3D
                ? PartyConfig.getSidebarHealthWoundedColor()
                : PartyConfig.getSidebarHealthCriticalColor();
        String glyph = PartyConfig.getSidebarHealthGlyph();
        StringBuilder healthBar = new StringBuilder(" ");
        if (filledSegments > 0)
            healthBar.append(filledColor).append(glyph.repeat(filledSegments));
        if (filledSegments < HEALTH_SEGMENTS)
            healthBar.append(PartyConfig.getSidebarHealthMissingColor())
                    .append(glyph.repeat(HEALTH_SEGMENTS - filledSegments));
        return healthBar.toString();
    }

    private static String renderLives(Player member, DungeonInstance dungeonInstance) {
        if (member == null || dungeonInstance == null) return "";
        Integer lives = dungeonInstance.getRemainingLives(member);
        if (lives == null) return "";
        return PartyConfig.getSidebarLivesDisplay().replace("$lives", String.valueOf(Math.max(0, lives)));
    }

    private static DungeonInstance getDungeonInstance(Player viewer) {
        MatchInstance matchInstance = PlayerData.getMatchInstance(viewer);
        return matchInstance instanceof DungeonInstance dungeonInstance ? dungeonInstance : null;
    }

    static void clearPlayer(Player player) {
        temporaryQuestViews.remove(player.getUniqueId());
        if (lastRenderedSidebars.remove(player.getUniqueId()) == null) return;
        if (QuestsConfig.isHideQuestScoreboardDuringQuestDialogue()
                && QuestDialogueBossBarManager.hasActiveSession(player)) return;
        SimpleScoreboard.clearScoreboard(player);
        QuestTracking tracking = QuestTracking.getPlayerTrackingQuests().get(player.getUniqueId());
        if (tracking != null) tracking.refreshScoreboard();
    }

    private static QuestView currentQuestView(Player player) {
        if (!QuestsConfig.isUseQuestScoreboards()) return null;
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

    private record SidebarSnapshot(String title, List<String> lines) {
    }
}
