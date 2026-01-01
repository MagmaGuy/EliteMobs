package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ToggleSkill;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;

/**
 * Batch Skill System Test - Tests all skills of each weapon type simultaneously.
 * <p>
 * <b>Test Flow:</b>
 * <ol>
 *   <li>Groups all skills by weapon type (SWORDS, AXES, BOWS, CROSSBOWS, TRIDENTS, HOES, ARMOR)</li>
 *   <li>For each weapon type, spawns a single training dummy</li>
 *   <li>For each level (10, 20, 30... 100):
 *     <ul>
 *       <li>Sets player to that skill level</li>
 *       <li>Activates ALL skills of that type simultaneously</li>
 *       <li>Resets proc counts on all skills</li>
 *       <li>Performs 200 attacks</li>
 *       <li>Checks each skill's {@link SkillBonus#getProcCount} to verify activation</li>
 *     </ul>
 *   </li>
 * </ol>
 * <p>
 * <b>Validation Logic:</b>
 * <ul>
 *   <li>If {@code level >= skill.getRequiredLevel()}: skill should have procs > 0 (PASS)</li>
 *   <li>If {@code level < skill.getRequiredLevel()}: skill should have 0 procs (PASS)</li>
 *   <li>Any mismatch is flagged as a failure</li>
 * </ul>
 * <p>
 * <b>0-Tick Trick (Melee/Armor):</b>
 * All 200 attacks happen in a single tick by:
 * <ul>
 *   <li>Clearing iframes via {@code target.setNoDamageTicks(0)} before each hit</li>
 *   <li>Resetting skill cooldowns via {@link CooldownSkill#endCooldown} before each hit</li>
 * </ul>
 * <p>
 * <b>Ranged Weapons:</b>
 * Bows, crossbows, and tridents use a 3-tick interval between shots to allow projectile travel time.
 * <p>
 * <b>Usage:</b> {@code /em skilltest start}
 *
 * @see CombatSimulator - Handles dummy spawning and attack simulation
 * @see SkillBonus#incrementProcCount - Called by skills when they activate
 */
public class SkillSystemTest {

    private static final Map<UUID, SkillSystemTest> activeSessions = new HashMap<>();

    // Test configuration
    private static final int[] TEST_LEVELS = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    private static final int HITS_PER_LEVEL = 200;

    @Getter
    private final Player player;
    @Getter
    private final UUID playerUUID;

    private final CombatSimulator combatSimulator;
    private final CombatTestLog testLog;
    private final TestReport report;

    private boolean cancelled = false;
    private BukkitTask currentTask;
    private BossBar progressBar;

    // Saved player state
    private final Map<SkillType, Long> savedSkillXP = new HashMap<>();
    private final Map<SkillType, List<String>> savedSkillSelections = new HashMap<>();
    private double savedAttackSpeed;
    private double savedKnockbackResistance;

    // Skills grouped by type
    private final Map<SkillType, List<SkillBonus>> skillsByType = new EnumMap<>(SkillType.class);

    // Test queue
    private final Queue<SkillType> typeQueue = new LinkedList<>();
    private SkillType currentType = null;
    private int currentLevelIndex = 0;

    // Results: skillId -> level -> passed
    private final Map<String, Map<Integer, Boolean>> results = new HashMap<>();
    private final Map<String, List<String>> issues = new HashMap<>();

    public SkillSystemTest(Player player) {
        this.player = player;
        this.playerUUID = player.getUniqueId();
        this.combatSimulator = new CombatSimulator(player);
        this.testLog = new CombatTestLog();
        this.report = new TestReport(playerUUID);
        activeSessions.put(playerUUID, this);
    }

    public static SkillSystemTest getSession(UUID playerUUID) {
        return activeSessions.get(playerUUID);
    }

    public static boolean hasActiveSession(UUID playerUUID) {
        return activeSessions.containsKey(playerUUID);
    }

    public void start() {
        // Group skills by type
        for (SkillType type : SkillType.values()) {
            List<SkillBonus> skills = SkillBonusRegistry.getEnabledBonuses(type);
            if (!skills.isEmpty()) {
                skillsByType.put(type, skills);
                typeQueue.add(type);
            }
        }

        if (typeQueue.isEmpty()) {
            log("§c[Test] No skills found to test!");
            return;
        }

        int totalSkills = skillsByType.values().stream().mapToInt(List::size).sum();
        log("§a§l=== BATCH SKILL TEST STARTING ===");
        log("§7Testing §e" + totalSkills + "§7 skills across §e" + typeQueue.size() + "§7 weapon types");
        log("§7All skills of each type tested simultaneously!");

        testLog.logSection("BATCH SKILL SYSTEM TEST");
        testLog.log("Player: " + player.getName());
        testLog.log("Total skills: " + totalSkills);

        // Create progress bar
        progressBar = Bukkit.createBossBar("Initializing...", BarColor.BLUE, BarStyle.SEGMENTED_10);
        progressBar.setProgress(0);
        progressBar.addPlayer(player);

        // Save and setup player
        savePlayerState();
        setupPlayerForTesting();

        // Start testing
        testNextType();
    }

    private void testNextType() {
        if (cancelled || typeQueue.isEmpty()) {
            completeTest();
            return;
        }

        currentType = typeQueue.poll();
        currentLevelIndex = 0;

        List<SkillBonus> skills = skillsByType.get(currentType);
        log("");
        log("§6=== Testing " + currentType.getDisplayName() + " §7(" + skills.size() + " skills) ===");
        testLog.logSection(currentType.getDisplayName() + " (" + skills.size() + " skills)");

        // Spawn dummy for this weapon type
        SkillBonus firstSkill = skills.get(0);
        if (!combatSimulator.spawnSingleDummy(firstSkill)) {
            log("§cFailed to spawn dummy for " + currentType.getDisplayName());
            testNextType();
            return;
        }

        testNextLevel();
    }

    private void testNextLevel() {
        if (cancelled) {
            completeTest();
            return;
        }

        if (currentLevelIndex >= TEST_LEVELS.length) {
            // Done with all levels for this type
            summarizeTypeResults();
            combatSimulator.removeAllDummies();
            testNextType();
            return;
        }

        int testLevel = TEST_LEVELS[currentLevelIndex];
        List<SkillBonus> skills = skillsByType.get(currentType);

        // Set player level and activate ALL skills of this type
        setPlayerSkillLevel(currentType, testLevel);
        activateAllSkills(currentType, skills);
        combatSimulator.equipWeapon(currentType);

        // Reset all proc counts
        for (SkillBonus skill : skills) {
            skill.resetProcCount(player);
        }

        log("§7Level §e" + testLevel + "§7: Testing " + skills.size() + " skills...");

        // All attack types now use instant same-tick damage
        // Ranged uses the "spawn projectile + direct damage" trick
        testInstantLevel(testLevel, skills);
    }

    /**
     * Tests all skills at this level using instant same-tick attacks.
     * Works for all weapon types - melee uses player.attack(), ranged uses the
     * "spawn projectile + direct damage" trick for instant hits.
     */
    private void testInstantLevel(int testLevel, List<SkillBonus> skills) {
        String dummyId = skills.get(0).getSkillId();

        // Do all hits in same tick - instant!
        for (int i = 0; i < HITS_PER_LEVEL; i++) {
            if (cancelled) return;

            // Ensure dummy exists
            if (!combatSimulator.hasDummy(dummyId)) {
                combatSimulator.respawnIfDead(dummyId, skills.get(0));
            }

            // Reset cooldowns on all skills before each attack (0-tick trick)
            resetAllCooldowns(skills);

            // Perform attack
            performAttack(currentType, dummyId);

            // Heal dummy for next hit
            combatSimulator.healDummy(dummyId);
        }

        updateProgressBar(currentType.getDisplayName() + " Lv." + testLevel, HITS_PER_LEVEL, HITS_PER_LEVEL, BarColor.GREEN);

        // Check results and move to next level
        checkLevelResults(testLevel, skills);
        currentLevelIndex++;

        // Small delay before next level for UI update
        new BukkitRunnable() {
            @Override
            public void run() {
                testNextLevel();
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

    /**
     * Resets cooldowns on all cooldown-type skills so they can proc on each attack.
     * This is the "0-tick trick" - clearing iframes and cooldowns between each hit.
     */
    private void resetAllCooldowns(List<SkillBonus> skills) {
        for (SkillBonus skill : skills) {
            if (skill instanceof CooldownSkill cooldownSkill) {
                cooldownSkill.endCooldown(player);
            }
        }
    }

    private void checkLevelResults(int level, List<SkillBonus> skills) {
        StringBuilder sb = new StringBuilder();
        sb.append("  §7Lv.").append(level).append(": ");

        int passed = 0;
        int failed = 0;

        for (SkillBonus skill : skills) {
            int procCount = skill.getProcCount(player);
            boolean shouldBeUnlocked = level >= skill.getRequiredLevel();
            boolean worked = procCount > 0;

            // Record result
            results.computeIfAbsent(skill.getSkillId(), k -> new HashMap<>()).put(level, worked == shouldBeUnlocked);

            if (shouldBeUnlocked) {
                // Skill should work
                if (worked) {
                    passed++;
                } else {
                    failed++;
                    issues.computeIfAbsent(skill.getSkillId(), k -> new ArrayList<>())
                          .add("No procs at Lv." + level + " (should be unlocked)");
                }
            } else {
                // Skill should be locked
                if (!worked) {
                    passed++;
                } else {
                    failed++;
                    issues.computeIfAbsent(skill.getSkillId(), k -> new ArrayList<>())
                          .add("Procced at Lv." + level + " (should be LOCKED, unlock=" + skill.getRequiredLevel() + ")");
                }
            }
        }

        if (failed == 0) {
            sb.append("§a✓ All ").append(passed).append(" passed");
        } else {
            sb.append("§a").append(passed).append(" passed, §c").append(failed).append(" failed");
        }

        log(sb.toString());
        testLog.log("Level " + level + ": " + passed + " passed, " + failed + " failed");
    }

    private void summarizeTypeResults() {
        List<SkillBonus> skills = skillsByType.get(currentType);
        log("");
        log("§7--- " + currentType.getDisplayName() + " Summary ---");

        for (SkillBonus skill : skills) {
            Map<Integer, Boolean> skillResults = results.get(skill.getSkillId());
            List<String> skillIssues = issues.get(skill.getSkillId());

            if (skillResults == null) continue;

            long passedLevels = skillResults.values().stream().filter(b -> b).count();
            boolean allPassed = passedLevels == TEST_LEVELS.length;

            if (allPassed) {
                log("  §a✓ " + skill.getBonusName() + " §7(unlock Lv." + skill.getRequiredLevel() + ")");
            } else {
                log("  §c✗ " + skill.getBonusName() + " §7(unlock Lv." + skill.getRequiredLevel() + ")");
                if (skillIssues != null) {
                    for (String issue : skillIssues) {
                        log("    §c- " + issue);
                    }
                }
            }

            // Add to report
            SkillTestResult result = new SkillTestResult(
                    skill.getSkillId(), skill.getBonusName(), skill.getSkillType(), skill.getRequiredLevel());
            result.setTotalHits(HITS_PER_LEVEL * TEST_LEVELS.length);
            if (skillIssues != null) {
                skillIssues.forEach(result::addIssue);
            }
            report.addResult(result);
        }
    }

    private void activateAllSkills(SkillType type, List<SkillBonus> skills) {
        // Clear current selections
        for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(playerUUID, type))) {
            PlayerSkillSelection.removeActiveSkill(playerUUID, type, id);
        }
        // Activate all skills of this type, bypassing the normal 3-skill limit for testing
        for (SkillBonus skill : skills) {
            PlayerSkillSelection.addActiveSkill(playerUUID, type, skill.getSkillId(), true);
            // Enable toggle skills so they can proc during testing
            if (skill instanceof ToggleSkill toggleSkill) {
                toggleSkill.enable(player);
            }
        }
        SkillBonusRegistry.applyBonuses(player, type);
    }

    private double performAttack(SkillType skillType, String skillId) {
        return switch (skillType) {
            case BOWS, CROSSBOWS -> combatSimulator.simulateRangedAttack(skillId);
            case TRIDENTS -> combatSimulator.simulateTridentAttack(skillId);
            case ARMOR -> combatSimulator.simulateIncomingDamage(skillId, 10.0);
            default -> combatSimulator.simulateMeleeAttack(skillId);
        };
    }

    private void setPlayerSkillLevel(SkillType type, int level) {
        PlayerData.setSkillXP(playerUUID, type, SkillXPCalculator.totalXPForLevel(level));
    }

    private void savePlayerState() {
        for (SkillType type : SkillType.values()) {
            savedSkillXP.put(type, PlayerData.getSkillXP(playerUUID, type));
            savedSkillSelections.put(type, new ArrayList<>(PlayerSkillSelection.getActiveSkills(playerUUID, type)));
        }
        AttributeInstance attackSpeedAttr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackSpeedAttr != null) savedAttackSpeed = attackSpeedAttr.getBaseValue();
        AttributeInstance knockbackAttr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (knockbackAttr != null) savedKnockbackResistance = knockbackAttr.getBaseValue();
        combatSimulator.savePlayerArmor();
    }

    private void restorePlayerState() {
        for (SkillType type : SkillType.values()) {
            Long xp = savedSkillXP.get(type);
            if (xp != null) PlayerData.setSkillXP(playerUUID, type, xp);
            for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(playerUUID, type))) {
                PlayerSkillSelection.removeActiveSkill(playerUUID, type, id);
            }
            List<String> saved = savedSkillSelections.get(type);
            if (saved != null) {
                for (String id : saved) {
                    PlayerSkillSelection.addActiveSkill(playerUUID, type, id);
                }
            }
        }
        AttributeInstance attackSpeedAttr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackSpeedAttr != null) attackSpeedAttr.setBaseValue(savedAttackSpeed);
        AttributeInstance knockbackAttr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (knockbackAttr != null) knockbackAttr.setBaseValue(savedKnockbackResistance);
        combatSimulator.restorePlayerArmor();
    }

    private void setupPlayerForTesting() {
        AttributeInstance attackSpeedAttr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackSpeedAttr != null) attackSpeedAttr.setBaseValue(100.0);
        AttributeInstance knockbackAttr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (knockbackAttr != null) knockbackAttr.setBaseValue(1.0);
    }

    public void cancel() {
        cancelled = true;
        if (currentTask != null) currentTask.cancel();
        log("§c§lTest cancelled!");
        File logFile = testLog.saveToFile();
        if (logFile != null) log("§7Partial log: §e" + logFile.getName());
        cleanup();
    }

    private void completeTest() {
        testLog.logSection("TEST COMPLETE");

        List<String> summary = report.generateSummary();
        for (String line : summary) {
            testLog.log(line.replace("&", ""));
        }

        File logFile = testLog.saveToFile();

        log("");
        log("§a§l=== TEST COMPLETE ===");
        for (String line : summary) {
            log(line.replace("&", "§"));
        }

        if (logFile != null) {
            log("§7Log saved: §e" + logFile.getAbsolutePath());
        }

        cleanup();
    }

    private void cleanup() {
        if (currentTask != null) currentTask.cancel();
        if (progressBar != null) {
            progressBar.removeAll();
            progressBar = null;
        }
        combatSimulator.removeAllDummies();
        restorePlayerState();
        activeSessions.remove(playerUUID);
    }

    private void log(String message) {
        player.sendMessage(message);
    }

    private void updateProgressBar(String action, int current, int max, BarColor color) {
        if (progressBar == null) return;
        progressBar.setTitle("§f" + action + " §7[" + current + "/" + max + "]");
        progressBar.setProgress(Math.min(1.0, (double) current / max));
        progressBar.setColor(color);
    }
}
