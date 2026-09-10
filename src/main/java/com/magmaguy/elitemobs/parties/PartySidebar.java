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
import com.magmaguy.elitemobs.advancedcombat.CombatHealthFormatter;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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
        String health = renderHealth(member, downed);
        if (downed) health += PartyConfig.getSidebarDownedDisplay();
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

    private static String renderHealth(Player member, boolean downed) {
        if (member == null || !member.isOnline()) return " &8-/-";
        AttributeInstance attribute = member.getAttribute(Attribute.MAX_HEALTH);
        double maximum = Math.max(1D, attribute == null ? member.getHealth() : attribute.getValue());
        double current = downed || member.isDead() ? 0D : Math.max(0D, Math.min(maximum, member.getHealth()));
        double fraction = current / maximum;
        // Green at full health, red at half health, almost black when downed.
        int from = fraction >= .5D ? 0xFF0000 : 0x080000;
        int to = fraction >= .5D ? 0x55FF55 : 0xFF0000;
        double blend = fraction >= .5D ? (fraction - .5D) * 2D : fraction * 2D;
        int red = interpolateChannel(from >> 16, to >> 16, blend);
        int green = interpolateChannel(from >> 8, to >> 8, blend);
        int blue = interpolateChannel(from, to, blend);
        String tint = net.md_5.bungee.api.ChatColor.of(new java.awt.Color(red, green, blue)).toString();
        return " " + tint + CombatHealthFormatter.format(current) + "&7/&f" + CombatHealthFormatter.format(maximum);
    }

    private static int interpolateChannel(int from, int to, double blend) {
        return (int) Math.round((from & 255) + ((to & 255) - (from & 255)) * blend);
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
                tracking.getQuest().getQuestName(),
                tracking.getQuest().getQuestObjectives().getScoreboardObjectiveText());
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
