package com.magmaguy.elitemobs.thirdparty.placeholderapi;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.magmacore.util.ChatColorConverter;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class Placeholders extends PlaceholderExpansion {

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor() {
        return MetadataHandler.PLUGIN.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier() {
        return "elitemobs";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     * <p>
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion() {
        return MetadataHandler.PLUGIN.getDescription().getVersion();
    }

    /**
     * Converts a number to tiny text Roman numerals.
     * Uses small caps Unicode characters: ɪ ᴠ x ʟ ᴄ ᴅ ᴍ
     *
     * @param num The number to convert (1-3999)
     * @return The tiny Roman numeral string
     */
    private static String toTinyRoman(int num) {
        if (num <= 0 || num > 3999) return String.valueOf(num);

        // Tiny text Roman numeral characters
        String[] thousands = {"", "ᴍ", "ᴍᴍ", "ᴍᴍᴍ"};
        String[] hundreds = {"", "ᴄ", "ᴄᴄ", "ᴄᴄᴄ", "ᴄᴅ", "ᴅ", "ᴅᴄ", "ᴅᴄᴄ", "ᴅᴄᴄᴄ", "ᴄᴍ"};
        String[] tens = {"", "x", "xx", "xxx", "xʟ", "ʟ", "ʟx", "ʟxx", "ʟxxx", "xᴄ"};
        String[] ones = {"", "ɪ", "ɪɪ", "ɪɪɪ", "ɪᴠ", "ᴠ", "ᴠɪ", "ᴠɪɪ", "ᴠɪɪɪ", "ɪx"};

        return thousands[num / 1000] +
                hundreds[(num % 1000) / 100] +
                tens[(num % 100) / 10] +
                ones[num % 10];
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param player     A {@link org.bukkit.OfflinePlayer Player}.
     * @param identifier A String containing the identifier/value.
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        if (player == null)
            return "Only online players!";

        /*
        There is a weird bug with PAPI where, under certain circumstances, it seems to fail to see cached values and
        queries the database. This is usually fine for something like chat, but it quickly becomes a massive issue for
        tablists, scoreboards and other places that might display several placeholders simultaneously and update them
        every tick, quickly racking up hundreds or thousands of queries per tick, on top of the normal runtime accesses
        that EliteMobs does. At scale, this causes large issues. Hence, at least for now, PAPI does not have access to the
        databases for safety purposes. All of the queries should be in memory regardless.
         */

        if (!PlayerData.isInMemory(player.getUniqueId()) || PlayerData.getPlayerData(player.getUniqueId()) == null)
            return "Uninitialized player data!";

        if (identifier.startsWith("quest_"))
            return questPlaceholder(player, identifier);

        switch (identifier) {
            case "player_combat_tier":
            case "player_combat_level":
                return "" + CombatLevelCalculator.calculateCombatLevel(player.getUniqueId());
            case "player_combat_level_roman":
                return toTinyRoman(CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()));
            case "player_money":
                return EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId()));
            case "player_top_tier":
            case "player_top_combat_level":
                return "" + CombatLevelCalculator.highestOnlineCombatLevel();
            case "player_kills":
                return "" + PlayerData.getKills(player.getUniqueId());
            case "player_deaths":
                return "" + PlayerData.getDeaths(player.getUniqueId());

            // Skill level placeholders
            case "player_swords_level":
                return "" + SkillXPCalculator.levelFromTotalXP(
                        PlayerData.getSkillXP(player.getUniqueId(), SkillType.SWORDS));
            case "player_axes_level":
                return "" + SkillXPCalculator.levelFromTotalXP(
                        PlayerData.getSkillXP(player.getUniqueId(), SkillType.AXES));
            case "player_bows_level":
                return "" + SkillXPCalculator.levelFromTotalXP(
                        PlayerData.getSkillXP(player.getUniqueId(), SkillType.BOWS));
            case "player_crossbows_level":
                return "" + SkillXPCalculator.levelFromTotalXP(
                        PlayerData.getSkillXP(player.getUniqueId(), SkillType.CROSSBOWS));
            case "player_tridents_level":
                return "" + SkillXPCalculator.levelFromTotalXP(
                        PlayerData.getSkillXP(player.getUniqueId(), SkillType.TRIDENTS));
            case "player_hoes_level":
                return "" + SkillXPCalculator.levelFromTotalXP(
                        PlayerData.getSkillXP(player.getUniqueId(), SkillType.HOES));
            case "player_maces_level":
                return "" + SkillXPCalculator.levelFromTotalXP(
                        PlayerData.getSkillXP(player.getUniqueId(), SkillType.MACES));
            case "player_spears_level":
                return "" + SkillXPCalculator.levelFromTotalXP(
                        PlayerData.getSkillXP(player.getUniqueId(), SkillType.SPEARS));
            case "player_staves_level":
                return "" + SkillXPCalculator.levelFromTotalXP(
                        PlayerData.getSkillXP(player.getUniqueId(), SkillType.STAVES));
            case "player_wands_level":
                return "" + SkillXPCalculator.levelFromTotalXP(
                        PlayerData.getSkillXP(player.getUniqueId(), SkillType.WANDS));
            case "player_armor_level":
                return "" + SkillXPCalculator.levelFromTotalXP(
                        PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR));

            // Guild rank placeholders removed - they will return null which PAPI handles gracefully
            case "player_active_guild_rank_numerical":
            case "player_maximum_guild_rank_numerical":
            case "player_active_guild_rank_name":
            case "player_maximum_guild_rank_name":
            case "player_prestige_guild_rank_numerical":
            case "player_top_guild_rank":
            case "player_shortened_guild_rank":
                return "N/A"; // Guild rank system removed
        }

        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
        // was provided
        return null;
    }

    /**
     * Quest placeholders describe the quest the player is tracking, so another scoreboard plugin can show it
     * with useQuestScoreboards off. Objective lines are the same text as the EliteMobs quest sidebar, which
     * shows a single turn-in line once every objective is done. Without a tracked quest they are empty.
     */
    private static String questPlaceholder(Player player, String identifier) {
        if (identifier.equals("quest_active_count"))
            return "" + PlayerData.getQuests(player.getUniqueId()).size();
        QuestTracking tracking = QuestTracking.getPlayerTrackingQuests().get(player.getUniqueId());
        Quest quest = tracking == null ? null : tracking.getQuest();
        switch (identifier) {
            case "quest_is_tracking":
                return quest != null ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
            case "quest_tracked_name":
                return quest == null || quest.getQuestName() == null ? "" : ChatColorConverter.convert(quest.getQuestName());
            case "quest_tracked_complete":
                return quest != null && quest.getQuestObjectives().isOver()
                        ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }
        List<String> lines = quest == null ? List.of() : quest.getQuestObjectives().getScoreboardObjectiveText();
        if (identifier.equals("quest_tracked_objective_count")) return "" + lines.size();
        if (!identifier.startsWith("quest_tracked_objective_")) return null;

        // quest_tracked_objective_<n> is a sidebar line; _name, _current and _target are that objective's raw values.
        String[] parts = identifier.substring("quest_tracked_objective_".length()).split("_", 2);
        int index;
        try {
            index = Integer.parseInt(parts[0]) - 1;
        } catch (NumberFormatException exception) {
            return null;
        }
        if (index < 0) return null;
        if (parts.length == 1) return index < lines.size() ? ChatColorConverter.convert(lines.get(index)) : "";
        List<Objective> objectives = quest == null || quest.getQuestObjectives().getObjectives() == null
                ? List.of()
                : quest.getQuestObjectives().getObjectives().stream().filter(Objects::nonNull).toList();
        Objective objective = index < objectives.size() ? objectives.get(index) : null;
        return switch (parts[1]) {
            case "name" -> objective == null || objective.getObjectiveName() == null
                    ? "" : ChatColorConverter.convert(objective.getObjectiveName());
            case "current" -> objective == null ? "" : "" + objective.getCurrentAmount();
            case "target" -> objective == null ? "" : "" + objective.getTargetAmount();
            default -> null;
        };
    }

}
