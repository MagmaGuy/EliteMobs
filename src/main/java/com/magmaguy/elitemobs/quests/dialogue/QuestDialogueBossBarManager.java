package com.magmaguy.elitemobs.quests.dialogue;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.menus.QuestMenu;
import com.magmaguy.elitemobs.thirdparty.geyser.GeyserDetector;
import com.magmaguy.elitemobs.utils.BossBarUtil;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
import com.magmaguy.magmacore.util.ChatColorConverter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QuestDialogueBossBarManager {
    // Bar 0 carries the dialogue box image glyph; the name, body lines and prompt stack below it.
    private static final int BOX_BAR_INDEX = 0;
    private static final int NAME_BAR_INDEX = 1;
    private static final int FIRST_LINE_BAR_INDEX = 2;
    private static final int BODY_LINES = 3;
    private static final int PROMPT_BAR_INDEX = FIRST_LINE_BAR_INDEX + BODY_LINES;
    private static final int TOTAL_BARS = PROMPT_BAR_INDEX + 1;
    private static final int DIALOGUE_SLOWNESS_AMPLIFIER = 2;
    private static final int DIALOGUE_SLOWNESS_DURATION_TICKS = 20 * 30;
    private static final Map<UUID, DialogueSession> activeSessions = new ConcurrentHashMap<>();
    private static final Map<String, Long> recentlyShownTurnInDialogs = new ConcurrentHashMap<>();

    private QuestDialogueBossBarManager() {
    }

    public static void shutdown() {
        for (DialogueSession session : new ArrayList<>(activeSessions.values())) {
            session.close(false);
        }
        activeSessions.clear();
        recentlyShownTurnInDialogs.clear();
    }

    public static boolean showQuestMenuIntro(List<? extends Quest> quests, Player player, NPCEntity npcEntity, Runnable onComplete) {
        if (!canShowQuestDialogueBossBars(player)) return false;
        if (quests == null || quests.isEmpty()) return false;
        List<String> dialogueLines = new ArrayList<>();
        for (Quest quest : quests) {
            dialogueLines.addAll(getQuestDialogueLines(quest, player, npcEntity));
        }
        if (dialogueLines.isEmpty()) return false;
        startDialogue(player, getSpeakerName(npcEntity), dialogueLines, onComplete);
        return true;
    }

    public static boolean showRawDialogue(Player player, String speakerName, List<String> dialogueLines, Runnable onComplete) {
        if (!canShowQuestDialogueBossBars(player)) return false;
        if (dialogueLines == null || dialogueLines.isEmpty()) return false;
        startDialogue(player, speakerName, dialogueLines, onComplete);
        return true;
    }

    private static boolean canShowQuestDialogueBossBars(Player player) {
        return QuestsConfig.isUseQuestDialogueBossBars()
                && player != null
                && !GeyserDetector.bedrockPlayer(player);
    }

    public static boolean consumeRecentlyShownQuestCompleteDialog(Player player, Quest quest) {
        Long shownAt = recentlyShownTurnInDialogs.remove(dialogueKey(player, quest));
        return shownAt != null && System.currentTimeMillis() - shownAt < 60_000L;
    }

    private static void startDialogue(Player player, String speakerName, List<String> dialogueLines, Runnable onComplete) {
        close(player, false);
        DialogueSession session = new DialogueSession(player, speakerName, dialogueLines, onComplete);
        activeSessions.put(player.getUniqueId(), session);
        session.start();
    }

    public static void close(Player player, boolean runCallback) {
        DialogueSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) session.close(runCallback);
    }

    public static boolean hasActiveSession(Player player) {
        return player != null && activeSessions.containsKey(player.getUniqueId());
    }

    private static List<String> getQuestDialogueLines(Quest quest, Player player, NPCEntity npcEntity) {
        List<String> lines = new ArrayList<>();
        if (quest instanceof CustomQuest customQuest) {
            if (customQuest.getCustomQuestsConfigFields() == null) return lines;
            if (!quest.isAccepted()) {
                lines.addAll(firstNonEmpty(
                        customQuest.getCustomQuestsConfigFields().getQuestAcceptDialog(),
                        customQuest.getCustomQuestsConfigFields().getQuestLore()));
            } else if (quest.getQuestObjectives().isOver()
                    && npcEntity != null
                    && Objects.equals(quest.getQuestTaker(), npcEntity.getNPCsConfigFields().getFilename())) {
                List<String> questCompleteDialog = customQuest.getCustomQuestsConfigFields().getQuestCompleteDialog();
                if (questCompleteDialog != null && !questCompleteDialog.isEmpty()) {
                    lines.addAll(questCompleteDialog);
                    recentlyShownTurnInDialogs.put(dialogueKey(player, quest), System.currentTimeMillis());
                }
            }
            if (!lines.isEmpty()) lines.add(0, "&e" + customQuest.getQuestName());
        } else if (quest instanceof DynamicQuest && !quest.isAccepted()) {
            QuestMenu.QuestText questText = new QuestMenu.QuestText(quest, npcEntity, player);
            lines.add("&e" + quest.getQuestName());
            for (TextComponent textComponent : questText.getBody()) {
                lines.add(textComponent.toPlainText());
            }
        }
        return parsePlaceholders(player, lines);
    }

    private static String dialogueKey(Player player, Quest quest) {
        return player.getUniqueId() + ":" + quest.getQuestID();
    }

    private static List<String> firstNonEmpty(List<String> first, List<String> fallback) {
        if (first != null && !first.isEmpty()) return first;
        if (fallback != null) return fallback;
        return List.of();
    }

    private static List<String> parsePlaceholders(Player player, List<String> lines) {
        List<String> parsedLines = new ArrayList<>();
        Location location = player.getLocation();
        for (String line : lines) {
            if (line == null) continue;
            parsedLines.add(line
                    .replace("$player", player.getName())
                    .replace("$getX", location.getX() + "")
                    .replace("$getY", location.getY() + "")
                    .replace("$getZ", location.getZ() + ""));
        }
        return parsedLines;
    }

    private static String getSpeakerName(NPCEntity npcEntity) {
        if (npcEntity == null || npcEntity.getNPCsConfigFields().getName() == null)
            return "&eEliteMobs";
        return npcEntity.getNPCsConfigFields().getName();
    }

    private static class DialogueSession {
        private final Player player;
        private final String speakerName;
        private final List<Page> pages;
        private final Runnable onComplete;
        private final List<KeyedBossBar> hiddenKeyedBossBars = new ArrayList<>();
        private final List<BossBar> hiddenEliteBossBars = new ArrayList<>();
        private final BossBar[] bars = new BossBar[TOTAL_BARS];
        private final PotionEffect previousSlowness;
        private BukkitTask task;
        private int pageIndex = 0;
        private int visibleCharacters = 0;
        private boolean closed = false;

        private DialogueSession(Player player, String speakerName, List<String> dialogueLines, Runnable onComplete) {
            this.player = player;
            this.speakerName = speakerName;
            this.pages = paginate(wrapLines(dialogueLines));
            this.onComplete = onComplete;
            this.previousSlowness = player.getPotionEffect(PotionEffectType.SLOWNESS);
        }

        private void start() {
            suppressExistingBossBars();
            suppressScoreboard();
            applyMovementLock();
            createBars();
            showCurrentPage();
            task = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, this::tick, 1L, 1L);
        }

        private void suppressScoreboard() {
            // The quest tracking scoreboard (sidebar) and compass boss bar would otherwise clutter the
            // dialogue box. The compass bar re-adds the player every tick, so QuestTracking gates on
            // hasActiveSession(); here we just blank the sidebar and restore it on close.
            if (!QuestsConfig.isHideQuestScoreboardDuringQuestDialogue()) return;
            SimpleScoreboard.blankScoreboard(player);
        }

        private void restoreScoreboard() {
            if (!QuestsConfig.isHideQuestScoreboardDuringQuestDialogue() || !player.isOnline()) return;
            // The session is already removed from activeSessions by now, so scoreboard display is no longer
            // gated. If the player is tracking a quest, repaint it from current state (an objective may have
            // progressed during the dialogue); otherwise restore whatever board they had before.
            QuestTracking tracking = QuestTracking.getPlayerTrackingQuests().get(player.getUniqueId());
            if (tracking != null) tracking.refreshScoreboard();
            else SimpleScoreboard.clearScoreboard(player);
        }

        private void applyMovementLock() {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                    DIALOGUE_SLOWNESS_DURATION_TICKS,
                    DIALOGUE_SLOWNESS_AMPLIFIER,
                    false,
                    false,
                    false), true);
        }

        private void suppressExistingBossBars() {
            if (QuestsConfig.isHideKeyedBossBarsDuringQuestDialogue()) {
                Iterator<KeyedBossBar> iterator = Bukkit.getBossBars();
                while (iterator.hasNext()) {
                    KeyedBossBar bossBar = iterator.next();
                    if (!bossBar.getPlayers().contains(player)) continue;
                    bossBar.removePlayer(player);
                    hiddenKeyedBossBars.add(bossBar);
                }
            }
            for (BossBar bossBar : new ArrayList<>(BossBarUtil.bossBars)) {
                if (!bossBar.getPlayers().contains(player)) continue;
                bossBar.removePlayer(player);
                hiddenEliteBossBars.add(bossBar);
            }
        }

        private void createBars() {
            for (int i = 0; i < bars.length; i++) {
                BossBar bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
                bossBar.setProgress(0D);
                bossBar.addPlayer(player);
                bars[i] = bossBar;
            }
            // The box image never types in, so set it once. The white bar fill is hidden by the
            // transparent boss_bar sprites in the resource pack, leaving only the box glyph visible.
            bars[BOX_BAR_INDEX].setTitle(OffsetFontUtil.shift(
                    OffsetFontUtil.boxGlyph(), QuestsConfig.getQuestDialogueBoxOffsetX()));
        }

        private void showCurrentPage() {
            visibleCharacters = 0;
            updateBars();
        }

        private void tick() {
            if (!player.isOnline()) {
                close(false);
                return;
            }
            Page page = pages.get(pageIndex);
            if (visibleCharacters >= page.visibleLength()) return;
            visibleCharacters = Math.min(page.visibleLength(),
                    visibleCharacters + Math.max(1, QuestsConfig.getQuestDialogueCharactersPerTick()));
            updateBars();
        }

        private void advance() {
            Page page = pages.get(pageIndex);
            if (visibleCharacters < page.visibleLength()) {
                visibleCharacters = page.visibleLength();
                updateBars();
                return;
            }
            if (pageIndex + 1 >= pages.size()) {
                close(true);
                return;
            }
            pageIndex++;
            showCurrentPage();
        }

        private void updateBars() {
            Page page = pages.get(pageIndex);
            bars[NAME_BAR_INDEX].setTitle(formatName(speakerName));

            int remaining = visibleCharacters;
            for (int i = 0; i < BODY_LINES; i++) {
                String line = page.lines().get(i);
                int visibleLength = visibleLength(line);
                int lineCharacters = Math.min(visibleLength, Math.max(0, remaining));
                bars[FIRST_LINE_BAR_INDEX + i].setTitle(formatLine(substringVisible(line, lineCharacters)));
                remaining -= visibleLength;
            }
            bars[PROMPT_BAR_INDEX].setTitle(formatPrompt());
        }

        private void close(boolean runCallback) {
            if (closed) return;
            closed = true;
            activeSessions.remove(player.getUniqueId(), this);
            if (task != null) task.cancel();
            for (BossBar bossBar : bars) {
                if (bossBar != null) bossBar.removeAll();
            }
            clearMovementLock();
            restoreBossBars();
            restoreScoreboard();
            if (runCallback && onComplete != null && player.isOnline()) {
                Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, onComplete);
            }
        }

        private void clearMovementLock() {
            if (!player.isOnline()) return;
            PotionEffect currentSlowness = player.getPotionEffect(PotionEffectType.SLOWNESS);
            if (currentSlowness != null &&
                    (currentSlowness.getAmplifier() != DIALOGUE_SLOWNESS_AMPLIFIER ||
                            currentSlowness.getDuration() > DIALOGUE_SLOWNESS_DURATION_TICKS))
                return;
            if (currentSlowness != null) player.removePotionEffect(PotionEffectType.SLOWNESS);
            if (previousSlowness != null) player.addPotionEffect(previousSlowness, true);
        }

        private void restoreBossBars() {
            if (!player.isOnline()) return;
            for (KeyedBossBar bossBar : hiddenKeyedBossBars) {
                bossBar.addPlayer(player);
            }
            for (BossBar bossBar : hiddenEliteBossBars) {
                bossBar.addPlayer(player);
            }
        }
    }

    private record Page(List<String> lines) {
        private int visibleLength() {
            int total = 0;
            for (String line : lines) total += QuestDialogueBossBarManager.visibleLength(line);
            return total;
        }
    }

    private static List<String> wrapLines(List<String> rawLines) {
        List<String> wrappedLines = new ArrayList<>();
        for (String rawLine : rawLines) {
            String line = ChatColorConverter.convert(rawLine);
            String strippedLine = ChatColor.stripColor(line);
            if (strippedLine == null || strippedLine.isBlank()) {
                wrappedLines.add("");
                continue;
            }
            wrappedLines.addAll(wrapLine(line, Math.max(1, QuestsConfig.getQuestDialogueCharactersPerLine())));
        }
        return wrappedLines;
    }

    private static List<String> wrapLine(String line, int limit) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : line.split(" ")) {
            boolean currentHasVisibleContent = visibleLength(current.toString()) > 0;
            String wordWithSpace = currentHasVisibleContent ? " " + word : word;
            if (currentHasVisibleContent && visibleLength(current + wordWithSpace) > limit) {
                lines.add(current.toString());
                current = new StringBuilder(activeFormatCodes(current.toString()));
                if (visibleLength(current + word) > limit) {
                    lines.addAll(splitVisible(current + word, limit));
                    current = new StringBuilder();
                } else current.append(word);
            } else if (visibleLength(current + word) > limit) {
                lines.addAll(splitVisible(current + word, limit));
                current = new StringBuilder();
            } else {
                current.append(wordWithSpace);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }

    private static List<String> splitVisible(String line, int limit) {
        List<String> lines = new ArrayList<>();
        String remaining = line;
        while (visibleLength(remaining) > limit) {
            String part = substringVisible(remaining, limit);
            lines.add(part);
            remaining = activeFormatCodes(part) + remaining.substring(part.length());
        }
        if (!remaining.isEmpty()) lines.add(remaining);
        return lines;
    }

    private static List<Page> paginate(List<String> wrappedLines) {
        List<Page> pages = new ArrayList<>();
        for (int i = 0; i < wrappedLines.size(); i += BODY_LINES) {
            List<String> pageLines = new ArrayList<>();
            for (int j = 0; j < BODY_LINES; j++) {
                int index = i + j;
                pageLines.add(index < wrappedLines.size() ? wrappedLines.get(index) : "");
            }
            pages.add(new Page(pageLines));
        }
        if (pages.isEmpty()) pages.add(new Page(List.of("", "", "")));
        return pages;
    }

    private static String formatName(String name) {
        return OffsetFontUtil.shift(
                forceStyle(ChatColorConverter.convert(QuestsConfig.getQuestDialogueNamePrefix()
                        + name
                        + QuestsConfig.getQuestDialogueNameSuffix()), ChatColor.BOLD),
                QuestsConfig.getQuestDialogueNameOffsetX());
    }

    private static String formatLine(String line) {
        return OffsetFontUtil.shift(
                ChatColorConverter.convert(QuestsConfig.getQuestDialogueLinePrefix()
                        + line
                        + QuestsConfig.getQuestDialogueLineSuffix()),
                QuestsConfig.getQuestDialogueLineOffsetX());
    }

    private static String formatPrompt() {
        return OffsetFontUtil.shift(
                ChatColorConverter.convert(QuestsConfig.getQuestDialogueLinePrefix()
                        + QuestsConfig.getQuestDialoguePromptText()
                        + QuestsConfig.getQuestDialogueLineSuffix()),
                QuestsConfig.getQuestDialoguePromptOffsetX());
    }

    private static int visibleLength(String string) {
        int length = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == ChatColor.COLOR_CHAR && i + 1 < string.length()) {
                i++;
                continue;
            }
            length++;
        }
        return length;
    }

    private static String substringVisible(String string, int visibleCharacters) {
        if (visibleCharacters <= 0) return "";
        StringBuilder builder = new StringBuilder();
        int visibleCount = 0;
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (character == ChatColor.COLOR_CHAR && i + 1 < string.length()) {
                builder.append(character).append(string.charAt(i + 1));
                i++;
                continue;
            }
            if (visibleCount >= visibleCharacters) break;
            builder.append(character);
            visibleCount++;
        }
        return builder.toString();
    }

    private static String forceStyle(String string, ChatColor style) {
        if (string == null || string.isEmpty()) return "";
        StringBuilder builder = new StringBuilder(style.toString());
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            builder.append(character);
            if (character == ChatColor.COLOR_CHAR && i + 1 < string.length()) {
                char code = string.charAt(++i);
                builder.append(code);
                if ((code == 'x' || code == 'X') && i + 12 < string.length()) {
                    builder.append(string, i + 1, i + 13).append(style);
                    i += 12;
                    continue;
                }
                ChatColor chatColor = ChatColor.getByChar(code);
                if (chatColor == null) continue;
                if (chatColor.isColor() || chatColor == ChatColor.RESET) builder.append(style);
            }
        }
        return builder.toString();
    }

    private static String activeFormatCodes(String string) {
        String activeColor = "";
        List<ChatColor> activeFormats = new ArrayList<>();
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) != ChatColor.COLOR_CHAR || i + 1 >= string.length()) continue;
            char code = string.charAt(++i);
            if (code == 'x' || code == 'X') {
                String hexColor = readHexColor(string, i - 1);
                if (!hexColor.isEmpty()) {
                    activeColor = hexColor;
                    activeFormats.clear();
                    i += 12;
                }
                continue;
            }
            ChatColor chatColor = ChatColor.getByChar(code);
            if (chatColor == null) continue;
            if (chatColor == ChatColor.RESET) {
                activeColor = "";
                activeFormats.clear();
            } else if (chatColor.isColor()) {
                activeColor = chatColor.toString();
                activeFormats.clear();
            } else if (!activeFormats.contains(chatColor)) {
                activeFormats.add(chatColor);
            }
        }

        StringBuilder builder = new StringBuilder(activeColor);
        for (ChatColor activeFormat : activeFormats) builder.append(activeFormat);
        return builder.toString();
    }

    private static String readHexColor(String string, int colorCodeStart) {
        if (colorCodeStart + 13 >= string.length()) return "";
        StringBuilder builder = new StringBuilder(14);
        for (int i = 0; i < 7; i++) {
            int sectionIndex = colorCodeStart + i * 2;
            if (string.charAt(sectionIndex) != ChatColor.COLOR_CHAR) return "";
            builder.append(ChatColor.COLOR_CHAR).append(string.charAt(sectionIndex + 1));
        }
        return builder.toString();
    }

    public static class QuestDialogueBossBarEvents implements Listener {
        @EventHandler
        public void onSneak(PlayerToggleSneakEvent event) {
            if (!event.isSneaking()) return;
            DialogueSession session = activeSessions.get(event.getPlayer().getUniqueId());
            if (session == null) return;
            event.setCancelled(true);
            session.advance();
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            close(event.getPlayer(), false);
        }
    }
}
