package com.magmaguy.elitemobs.skills.bonuses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player skill selections.
 * <p>
 * Each player can select up to 3 active skills per weapon type.
 * Selections are persisted to the database as JSON.
 */
public class PlayerSkillSelection {

    public static final int MAX_ACTIVE_SKILLS = 3;

    private static final Gson GSON = new GsonBuilder().create();
    private static final Type SELECTION_TYPE = new TypeToken<Map<String, List<String>>>() {}.getType();

    // In-memory cache: UUID -> SkillType -> List<SkillId>
    private static final Map<UUID, Map<SkillType, List<String>>> activeSkills = new ConcurrentHashMap<>();

    private PlayerSkillSelection() {
        // Static utility class
    }

    /**
     * Gets the active skill IDs for a player and skill type.
     *
     * @param uuid      The player's UUID
     * @param skillType The skill type
     * @return List of active skill IDs (empty if none)
     */
    public static List<String> getActiveSkills(UUID uuid, SkillType skillType) {
        ensureLoaded(uuid);
        Map<SkillType, List<String>> playerSkills = activeSkills.get(uuid);
        if (playerSkills == null) {
            return Collections.emptyList();
        }
        return playerSkills.getOrDefault(skillType, Collections.emptyList());
    }

    /**
     * Adds a skill to a player's active skills.
     *
     * @param uuid      The player's UUID
     * @param skillType The skill type
     * @param skillId   The skill ID to add
     * @return true if the skill was added, false if already at max or already active
     */
    public static boolean addActiveSkill(UUID uuid, SkillType skillType, String skillId) {
        return addActiveSkill(uuid, skillType, skillId, false);
    }

    /**
     * Adds a skill to a player's active skills with optional limit bypass.
     *
     * @param uuid        The player's UUID
     * @param skillType   The skill type
     * @param skillId     The skill ID to add
     * @param bypassLimit If true, ignores the MAX_ACTIVE_SKILLS limit (for testing)
     * @return true if the skill was added, false if already at max or already active
     */
    public static boolean addActiveSkill(UUID uuid, SkillType skillType, String skillId, boolean bypassLimit) {
        ensureLoaded(uuid);
        Map<SkillType, List<String>> playerSkills = activeSkills.computeIfAbsent(uuid, k -> new EnumMap<>(SkillType.class));
        List<String> typeSkills = playerSkills.computeIfAbsent(skillType, k -> new ArrayList<>());

        // Check if already at max (unless bypassed for testing)
        if (!bypassLimit && typeSkills.size() >= MAX_ACTIVE_SKILLS) {
            return false;
        }

        // Check if already active
        if (typeSkills.contains(skillId)) {
            return false;
        }

        typeSkills.add(skillId);
        saveToDatabase(uuid);
        return true;
    }

    /**
     * Removes a skill from a player's active skills.
     *
     * @param uuid      The player's UUID
     * @param skillType The skill type
     * @param skillId   The skill ID to remove
     * @return true if the skill was removed, false if not found
     */
    public static boolean removeActiveSkill(UUID uuid, SkillType skillType, String skillId) {
        ensureLoaded(uuid);
        Map<SkillType, List<String>> playerSkills = activeSkills.get(uuid);
        if (playerSkills == null) {
            return false;
        }

        List<String> typeSkills = playerSkills.get(skillType);
        if (typeSkills == null) {
            return false;
        }

        boolean removed = typeSkills.remove(skillId);
        if (removed) {
            saveToDatabase(uuid);
        }
        return removed;
    }

    /**
     * Checks if a skill is active for a player.
     *
     * @param uuid    The player's UUID
     * @param skillId The skill ID to check
     * @return true if the skill is active
     */
    public static boolean isSkillActive(UUID uuid, String skillId) {
        ensureLoaded(uuid);
        Map<SkillType, List<String>> playerSkills = activeSkills.get(uuid);
        if (playerSkills == null) {
            return false;
        }

        for (List<String> typeSkills : playerSkills.values()) {
            if (typeSkills.contains(skillId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the number of active skills for a skill type.
     *
     * @param uuid      The player's UUID
     * @param skillType The skill type
     * @return The number of active skills
     */
    public static int getActiveSkillCount(UUID uuid, SkillType skillType) {
        return getActiveSkills(uuid, skillType).size();
    }

    /**
     * Checks if the player can add another skill for the given type.
     *
     * @param uuid      The player's UUID
     * @param skillType The skill type
     * @return true if another skill can be added
     */
    public static boolean canAddSkill(UUID uuid, SkillType skillType) {
        return getActiveSkillCount(uuid, skillType) < MAX_ACTIVE_SKILLS;
    }

    /**
     * Gets all active skill IDs for a player across all skill types.
     *
     * @param uuid The player's UUID
     * @return List of all active skill IDs
     */
    public static List<String> getAllActiveSkills(UUID uuid) {
        ensureLoaded(uuid);
        List<String> allSkills = new ArrayList<>();
        Map<SkillType, List<String>> playerSkills = activeSkills.get(uuid);
        if (playerSkills != null) {
            for (List<String> typeSkills : playerSkills.values()) {
                allSkills.addAll(typeSkills);
            }
        }
        return allSkills;
    }

    /**
     * Clears all active skills for a player.
     *
     * @param uuid The player's UUID
     */
    public static void clearAllSkills(UUID uuid) {
        activeSkills.remove(uuid);
        saveToDatabase(uuid);
    }

    /**
     * Ensures the player's skill selections are loaded from the database.
     *
     * @param uuid The player's UUID
     */
    private static void ensureLoaded(UUID uuid) {
        if (!activeSkills.containsKey(uuid)) {
            loadFromDatabase(uuid);
        }
    }

    /**
     * Loads skill selections from the database for a player.
     *
     * @param uuid The player's UUID
     */
    public static void loadFromDatabase(UUID uuid) {
        String json = PlayerData.getSkillBonusSelections(uuid);
        if (json == null || json.isEmpty() || json.equals("{}")) {
            activeSkills.put(uuid, new EnumMap<>(SkillType.class));
            return;
        }

        try {
            Map<String, List<String>> parsed = GSON.fromJson(json, SELECTION_TYPE);
            Map<SkillType, List<String>> converted = new EnumMap<>(SkillType.class);

            if (parsed != null) {
                for (Map.Entry<String, List<String>> entry : parsed.entrySet()) {
                    try {
                        SkillType skillType = SkillType.valueOf(entry.getKey());
                        converted.put(skillType, new ArrayList<>(entry.getValue()));
                    } catch (IllegalArgumentException e) {
                        Logger.warn("Unknown skill type in database: " + entry.getKey());
                    }
                }
            }

            activeSkills.put(uuid, converted);
        } catch (Exception e) {
            Logger.warn("Failed to parse skill selections for player " + uuid + ": " + e.getMessage());
            activeSkills.put(uuid, new EnumMap<>(SkillType.class));
        }
    }

    /**
     * Saves skill selections to the database for a player.
     *
     * @param uuid The player's UUID
     */
    public static void saveToDatabase(UUID uuid) {
        Map<SkillType, List<String>> playerSkills = activeSkills.get(uuid);
        if (playerSkills == null || playerSkills.isEmpty()) {
            PlayerData.setSkillBonusSelections(uuid, "{}");
            return;
        }

        // Convert to string keys for JSON
        Map<String, List<String>> toSerialize = new HashMap<>();
        for (Map.Entry<SkillType, List<String>> entry : playerSkills.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                toSerialize.put(entry.getKey().name(), entry.getValue());
            }
        }

        String json = GSON.toJson(toSerialize);
        PlayerData.setSkillBonusSelections(uuid, json);
    }

    /**
     * Called when a player joins to load their selections.
     *
     * @param player The player
     */
    public static void onPlayerJoin(Player player) {
        loadFromDatabase(player.getUniqueId());
    }

    /**
     * Called when a player leaves to save their selections.
     *
     * @param player The player
     */
    public static void onPlayerLeave(Player player) {
        saveToDatabase(player.getUniqueId());
        activeSkills.remove(player.getUniqueId());
    }

    /**
     * Clears the cache for a specific player.
     *
     * @param uuid The player's UUID
     */
    public static void clearCache(UUID uuid) {
        activeSkills.remove(uuid);
    }

    /**
     * Clears all cached selections.
     * Called on plugin shutdown.
     */
    public static void shutdown() {
        // Save all cached data
        for (UUID uuid : new ArrayList<>(activeSkills.keySet())) {
            saveToDatabase(uuid);
        }
        activeSkills.clear();
    }
}
