package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.DamageBreakdown;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.IronStanceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.LastStandSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.axes.ExecutionerSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.OverdrawSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.PackHunterSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.HuntersPreySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.SteadyAimSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.DeathsEmbraceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.GrimReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.ReapWhatYouSowSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.ReapersHarvestSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.DivineShieldSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.LongReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PolearmMasterySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.DepthChargeSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.ReturningHasteSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.RiptideMasterySkill;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
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
public class SkillSystemTest implements Listener {

    private static final Map<UUID, SkillSystemTest> activeSessions = new HashMap<>();

    // Test configuration
    private static final int[] TEST_LEVELS = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    private static final int HITS_PER_LEVEL = 200;

    // Skills that cannot be tested in the batch test and should be marked as SKIPPED
    private static final Map<String, String> SKIP_REASONS = Map.ofEntries(
            Map.entry("spears_phalanx", "defensive (frontal attack)")
    );

    // Skills that need the dummy set to low HP for part of the test
    // Uses 20% HP to satisfy all thresholds (Executioner <=40%, FinishingFlourish <=30%,
    // HuntersPrey <50%, ReapersHarvest <25%)
    private static final Set<String> HEALTH_THRESHOLD_SKILLS = Set.of(
            ExecutionerSkill.SKILL_ID,
            FinishingFlourishSkill.SKILL_ID,
            HuntersPreySkill.SKILL_ID,
            ReapersHarvestSkill.SKILL_ID
    );

    // Armor skills that need the player set to low HP for part of the test
    private static final Set<String> PLAYER_HEALTH_THRESHOLD_SKILLS = Set.of(
            "armor_grit",
            "armor_adrenaline_surge",
            "armor_second_wind"
    );

    // Armor skills that need fatal (no absorption) damage to test death prevention
    private static final Set<String> FATAL_DAMAGE_ARMOR_SKILLS = Set.of("armor_last_stand");

    // Armor skills that need high damage (>20% max HP) to trigger
    private static final Set<String> HIGH_DAMAGE_THRESHOLD_SKILLS = Set.of("armor_reactive_shielding");

    // Skills that need a riposte-ready state primed before each attack
    private static final Set<String> RIPOSTE_SETUP_SKILLS = Set.of(RiposteSkill.SKILL_ID);

    // Skills that need critical hits to proc
    private static final Set<String> CRITICAL_HIT_SKILLS = Set.of(VorpalStrikeSkill.SKILL_ID);

    // Skills that need the player to be blocking during incoming damage
    private static final Set<String> BLOCKING_DEFENSE_SKILLS = Set.of(ParrySkill.SKILL_ID);

    // Weapon skills that have a defensive component (death prevention) needing fatal damage testing
    private static final Map<String, SkillType> DEFENSIVE_WEAPON_SKILLS = Map.of(
            DeathsEmbraceSkill.SKILL_ID, SkillType.HOES,
            DivineShieldSkill.SKILL_ID, SkillType.MACES
    );

    // Offensive skills that need the PLAYER set to low HP for part of the test
    // (e.g. ReapWhatYouSow checks player HP, not target HP)
    private static final Set<String> OFFENSIVE_PLAYER_HEALTH_SKILLS = Set.of(
            ReapWhatYouSowSkill.SKILL_ID
    );

    // Depth Charge needs target in water
    private static final Set<String> WATER_REQUIRED_SKILLS = Set.of(
            DepthChargeSkill.SKILL_ID
    );

    // Skills that need storm weather (e.g. Riptide Mastery checks player.isInWater() || world.hasStorm())
    private static final Set<String> STORM_REQUIRED_SKILLS = Set.of(
            RiptideMasterySkill.SKILL_ID
    );
    private static final int BASELINE_HITS = 10;
    // Flag for whether SPEARS skill type is available on this server
    private static boolean spearsAvailable = true;

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

    static {
        try {
            Material.valueOf("IRON_SPEAR");
        } catch (IllegalArgumentException e) {
            spearsAvailable = false;
        }
    }

    // Optional filter for selective testing
    private final SkillType filterType;

    // Saved player state
    private final Map<SkillType, Long> savedSkillXP = new HashMap<>();
    private final Map<SkillType, List<String>> savedSkillSelections = new HashMap<>();
    private double savedAttackSpeed;
    private double savedKnockbackResistance;
    // Per-skill proc count tracking across levels
    private final Map<String, Integer> totalProcCounts = new HashMap<>();
    private final Map<String, Integer> totalHitCounts = new HashMap<>();
    // Damage validation: offensive baseline {type -> {level -> [expected, actualBaseline]}}
    private final Map<SkillType, Map<Integer, double[]>> offensiveBaseline = new HashMap<>();

    // Skills grouped by type
    private final Map<SkillType, List<SkillBonus>> skillsByType = new EnumMap<>(SkillType.class);
    // Damage validation: defensive baseline {level -> [expected, actualBaseline]}
    private final Map<Integer, double[]> defensiveBaseline = new HashMap<>();
    // Accumulate damage during skill testing (with all skills active)
    private final Map<SkillType, Map<Integer, Double>> offensiveSkillDamage = new HashMap<>();

    // Test queue
    private final Queue<SkillType> typeQueue = new LinkedList<>();
    private SkillType currentType = null;
    private int currentLevelIndex = 0;

    // Results: skillId -> level -> passed
    private final Map<String, Map<Integer, Boolean>> results = new HashMap<>();
    private final Map<String, List<String>> issues = new HashMap<>();
    private final Map<SkillType, Map<Integer, Integer>> offensiveSkillHits = new HashMap<>();
    private final Map<Integer, Double> defensiveSkillDamage = new HashMap<>();
    private final Map<Integer, Integer> defensiveSkillHits = new HashMap<>();
    // Overall progress tracking
    private int totalTypes = 0;
    private int completedTypes = 0;
    private double savedMaxHealthBase;
    private double savedMaxAbsorption;
    private float savedWalkSpeed;
    // Saved weather state for storm-dependent skill testing
    private boolean savedStormState = false;

    public SkillSystemTest(Player player) {
        this(player, null);
    }

    /**
     * Creates a test session with an optional weapon type filter.
     * If filterType is non-null, only that weapon type will be tested.
     */
    public SkillSystemTest(Player player, SkillType filterType) {
        this.player = player;
        this.playerUUID = player.getUniqueId();
        this.combatSimulator = new CombatSimulator(player);
        this.testLog = new CombatTestLog();
        this.report = new TestReport(playerUUID);
        this.filterType = filterType;
        activeSessions.put(playerUUID, this);

        // Register disconnect listener
        Bukkit.getPluginManager().registerEvents(this, MetadataHandler.PLUGIN);
    }

    public static SkillSystemTest getSession(UUID playerUUID) {
        return activeSessions.get(playerUUID);
    }

    public static boolean hasActiveSession(UUID playerUUID) {
        return activeSessions.containsKey(playerUUID);
    }

    /**
     * Handles player disconnect during testing - restores state and cleans up.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getUniqueId().equals(playerUUID)) {
            cancel();
        }
    }

    public void start() {
        // Set testing active flag to suppress entity spawning
        CombatSimulator.setTestingActive(true);

        // Group skills by type
        for (SkillType type : SkillType.values()) {
            // Apply filter if selective testing
            if (filterType != null && type != filterType) continue;

            // Skip SPEARS if the server doesn't have spear materials
            if (type == SkillType.SPEARS && !spearsAvailable) {
                log("§7Skipping " + type.getDisplayName() + " §7(not available on this MC version)");
                // Add all spear skills as skipped
                List<SkillBonus> spearSkills = SkillBonusRegistry.getEnabledBonuses(type);
                for (SkillBonus skill : spearSkills) {
                    SkillTestResult result = new SkillTestResult(
                            skill.getSkillId(), skill.getBonusName(), skill.getSkillType(), skill.getRequiredLevel());
                    result.markSkipped("spears not available on this MC version");
                    report.addSkippedResult(result);
                }
                continue;
            }

            List<SkillBonus> skills = SkillBonusRegistry.getEnabledBonuses(type);
            if (!skills.isEmpty()) {
                skillsByType.put(type, skills);
                typeQueue.add(type);
            }
        }

        if (typeQueue.isEmpty()) {
            log("§c[Test] No skills found to test!");
            CombatSimulator.setTestingActive(false);
            return;
        }

        totalTypes = typeQueue.size();
        int totalSkills = skillsByType.values().stream().mapToInt(List::size).sum();
        String filterMsg = filterType != null ? " §7(filtered: §e" + filterType.getDisplayName() + "§7)" : "";
        log("§a§l=== BATCH SKILL TEST STARTING ===" + filterMsg);
        log("§7Testing §e" + totalSkills + "§7 skills across §e" + typeQueue.size() + "§7 weapon types");
        log("§7All skills of each type tested simultaneously!");

        testLog.logSection("BATCH SKILL SYSTEM TEST");
        testLog.log("Player: " + player.getName());
        testLog.log("Total skills: " + totalSkills);
        if (filterType != null) testLog.log("Filter: " + filterType.getDisplayName());

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

        // Clean up previous dummies, stray entities, and add delay for entity despawn
        combatSimulator.removeAllDummies();
        combatSimulator.cleanupTestEntities();

        currentType = typeQueue.poll();
        currentLevelIndex = 0;

        // Reset per-skill tracking for this type
        totalProcCounts.clear();
        totalHitCounts.clear();

        List<SkillBonus> skills = skillsByType.get(currentType);

        // Filter out untestable skills and add them as skipped
        List<SkillBonus> skippedSkills = new ArrayList<>();
        List<SkillBonus> testableSkills = new ArrayList<>();
        for (SkillBonus skill : skills) {
            String reason = SKIP_REASONS.get(skill.getSkillId());
            if (reason != null) {
                skippedSkills.add(skill);
                SkillTestResult result = new SkillTestResult(
                        skill.getSkillId(), skill.getBonusName(), skill.getSkillType(), skill.getRequiredLevel());
                result.markSkipped(reason);
                report.addSkippedResult(result);
            } else {
                testableSkills.add(skill);
            }
        }
        skillsByType.put(currentType, testableSkills);

        if (testableSkills.isEmpty()) {
            log("§7All " + currentType.getDisplayName() + " skills skipped");
            completedTypes++;
            testNextType();
            return;
        }

        log("");
        int skippedCount = skippedSkills.size();
        String skippedMsg = skippedCount > 0 ? " §7(" + skippedCount + " skipped)" : "";
        log("§6=== Testing " + currentType.getDisplayName() + " §7(" + testableSkills.size() + " skills)" + skippedMsg + " §6[" + (completedTypes + 1) + "/" + totalTypes + "] ===");
        testLog.logSection(currentType.getDisplayName() + " (" + testableSkills.size() + " skills" + (skippedCount > 0 ? ", " + skippedCount + " skipped" : "") + ")");

        // Update overall progress bar
        updateOverallProgress();

        // Delay 2 ticks to let entity despawn propagate, then spawn next dummy
        new BukkitRunnable() {
            @Override
            public void run() {
                if (cancelled) {
                    completeTest();
                    return;
                }

                // Spawn dummy for this weapon type
                SkillBonus firstSkill = testableSkills.get(0);
                if (!combatSimulator.spawnSingleDummy(firstSkill)) {
                    log("§cFailed to spawn dummy for " + currentType.getDisplayName());
                    testNextType();
                    return;
                }

                testNextLevel();
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 2);
    }

    private void testNextLevel() {
        if (cancelled) {
            completeTest();
            return;
        }

        if (currentLevelIndex >= TEST_LEVELS.length) {
            // Done with all levels for this type
            summarizeTypeResults();
            completedTypes++;
            testNextType();
            return;
        }

        int testLevel = TEST_LEVELS[currentLevelIndex];
        List<SkillBonus> skills = skillsByType.get(currentType);

        // Set player level and equip weapon BEFORE baseline check
        setPlayerSkillLevel(currentType, testLevel);
        combatSimulator.equipWeapon(currentType);

        // Equip armor for armor tests (needed for baseline too)
        if (currentType == SkillType.ARMOR) {
            combatSimulator.equipArmorSet(testLevel);
            // Set the player's MAX_HEALTH base value directly to the formula value.
            // The 1-shot protection cap in PlayerDamagedByEliteMobEvent uses
            // AttributeManager.getAttributeBaseValue() which returns the BASE value,
            // NOT the effective value (base + modifiers). So we must set the base value
            // rather than adding a modifier via ArmorSkillHealthBonus.applyHealthBonus().
            double formulaMaxHealth = 20.0 + Math.max(0, testLevel - 1) * 2.0;
            AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealthAttr != null) {
                maxHealthAttr.setBaseValue(formulaMaxHealth);
            }
            player.setHealth(formulaMaxHealth);
        }

        // Run baseline damage check BEFORE activating skills
        String baselineDummyId = skills.get(0).getSkillId();
        if (currentType == SkillType.ARMOR) {
            runDefensiveBaseline(testLevel, baselineDummyId);
        } else {
            runOffensiveBaseline(testLevel, currentType, baselineDummyId);
        }

        // Now activate ALL skills of this type
        activateAllSkills(currentType, skills);

        // Apply passive weapon effects (speed, knockback resist, reach, attack speed)
        // These normally fire on PlayerItemHeldEvent which doesn't trigger in testing
        combatSimulator.applyPassiveWeaponEffects(currentType, testLevel);

        // Set up conditional skill states
        setupConditionalSkillStates(skills);

        // Reset all proc counts
        for (SkillBonus skill : skills) {
            skill.resetProcCount(player);
        }

        // Update progress bar for this level
        updateOverallProgress();

        log("§7Level §e" + testLevel + "§7: Testing " + skills.size() + " skills...");

        // All attack types now use instant same-tick damage
        // Ranged uses the "spawn projectile + direct damage" trick
        testInstantLevel(testLevel, skills);
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

    /**
     * Tests all skills at this level using instant same-tick attacks.
     * Works for all weapon types - melee uses player.attack(), ranged uses the
     * "spawn projectile + direct damage" trick for instant hits.
     */
    private void testInstantLevel(int testLevel, List<SkillBonus> skills) {
        String dummyId = skills.get(0).getSkillId();

        // Check if any skills need the dummy at low HP for part of the test
        boolean hasThresholdSkills = skills.stream()
                .anyMatch(s -> HEALTH_THRESHOLD_SKILLS.contains(s.getSkillId()));

        // Check if any offensive skills need the player at low HP
        boolean hasOffensivePlayerHealthSkills = currentType != SkillType.ARMOR && skills.stream()
                .anyMatch(s -> OFFENSIVE_PLAYER_HEALTH_SKILLS.contains(s.getSkillId()));

        // Check if any skills need water at the dummy location
        boolean hasWaterSkills = skills.stream()
                .anyMatch(s -> WATER_REQUIRED_SKILLS.contains(s.getSkillId()));

        // Check if any skills need storm weather
        boolean hasStormSkills = skills.stream()
                .anyMatch(s -> STORM_REQUIRED_SKILLS.contains(s.getSkillId()));

        // Check for new condition categories
        boolean hasRiposteSkills = skills.stream()
                .anyMatch(s -> RIPOSTE_SETUP_SKILLS.contains(s.getSkillId()));

        boolean hasCriticalHitSkills = skills.stream()
                .anyMatch(s -> CRITICAL_HIT_SKILLS.contains(s.getSkillId()));

        boolean hasBlockingDefenseSkills = skills.stream()
                .anyMatch(s -> BLOCKING_DEFENSE_SKILLS.contains(s.getSkillId()));

        boolean hasDefensiveWeaponSkills = skills.stream()
                .anyMatch(s -> DEFENSIVE_WEAPON_SKILLS.containsKey(s.getSkillId()));

        // Enable PackHunter test override if present
        boolean hasPackHunter = skills.stream()
                .anyMatch(s -> PackHunterSkill.SKILL_ID.equals(s.getSkillId()));
        if (hasPackHunter) {
            PackHunterSkill.setTestOverrideNearbyPlayers(true);
        }

        // Place water at dummy for water-dependent skills (e.g. Depth Charge)
        if (hasWaterSkills) {
            combatSimulator.placeWaterAtDummy(dummyId);
        }

        // Set storm weather for storm-dependent skills (e.g. Riptide Mastery)
        if (hasStormSkills) {
            savedStormState = player.getWorld().hasStorm();
            player.getWorld().setStorm(true);
        }

        // Package the hit loop + post-loop as a Runnable so we can optionally delay it.
        // Water-dependent skills (e.g. Depth Charge) need 1 tick after block placement
        // for the entity's isInWater() state to update.
        Runnable hitLoopTask = () -> {
            // Do all hits in same tick - instant!
            for (int i = 0; i < HITS_PER_LEVEL; i++) {
                if (cancelled) return;

                // Ensure dummy exists
                if (!combatSimulator.hasDummy(dummyId)) {
                    combatSimulator.respawnIfDead(dummyId, skills.get(0));
                    // Re-place water if dummy was respawned
                    if (hasWaterSkills) {
                        combatSimulator.placeWaterAtDummy(dummyId);
                    }
                }

                // Set dummy to low HP BEFORE attack for threshold skills (second half of hits)
                // This ensures conditional skills see low HP when they check during the attack
                if (hasThresholdSkills && i >= HITS_PER_LEVEL / 2) {
                    combatSimulator.setDummyHealthPercent(dummyId, 0.20);
                }

                // Reset cooldowns on all skills before each attack (0-tick trick)
                resetAllCooldowns(skills);

                // Prime riposte-ready state before each attack if testing Riposte
                if (hasRiposteSkills) {
                    RiposteSkill.onPlayerBlock(player);
                }

                // ===== ARMOR-TYPE HIT PARTITIONING =====
                // For armor skills, use partitioned hit ranges so ALL armor conditions
                // are tested (normal, low-HP, high-damage, fatal) without exclusive branching.
                // All armor hits use the test damage override to bypass the defense formula,
                // ensuring non-zero damage at all armor levels.
                if (currentType == SkillType.ARMOR) {
                    if (i < 100) {
                        // Hits 0-99: Normal incoming damage with override (10 damage, full HP)
                        // Tests: IronStance, Fortify, Retaliation, BattleHardened, Evasion, etc.
                        double damage = combatSimulator.simulateIncomingDamageWithOverride(dummyId, 10.0);
                        if (damage > 0) {
                            defensiveSkillDamage.merge(testLevel, damage, Double::sum);
                            defensiveSkillHits.merge(testLevel, 1, Integer::sum);
                        }
                    } else if (i < 120) {
                        // Hits 100-119: Very low HP (15%) for SecondWind (<25%) and AdrenalineSurge (<30%)
                        // Player at 15% ensures projected health drops below both thresholds
                        // even after IronStance+BattleHardened+Fortify reduce getDamage().
                        double lowHpDamage = player.getMaxHealth() * 0.25;
                        double damage = combatSimulator.simulateIncomingDamage(dummyId, lowHpDamage, 0.15);
                        if (damage > 0) {
                            defensiveSkillDamage.merge(testLevel, damage, Double::sum);
                            defensiveSkillHits.merge(testLevel, 1, Integer::sum);
                        }
                    } else if (i < 140) {
                        // Hits 120-139: Moderate low HP (40%) for Grit (<50% threshold)
                        // At 40%, Grit's conditionMet (health < 50%) returns true, but
                        // projected health stays above SecondWind's 25% threshold, so
                        // SecondWind doesn't heal the player above 50% and break Grit.
                        double gritDamage = player.getMaxHealth() * 0.25;
                        double damage = combatSimulator.simulateIncomingDamage(dummyId, gritDamage, 0.40);
                        if (damage > 0) {
                            defensiveSkillDamage.merge(testLevel, damage, Double::sum);
                            defensiveSkillHits.merge(testLevel, 1, Integer::sum);
                        }
                    } else if (i < 170) {
                        // Hits 140-169: High damage (500% of max HP) with override
                        // Tests: ReactiveShielding (>20% max HP threshold)
                        // IronStance+BattleHardened+Fortify stack up to ~94% reduction at Lv.100
                        // (multiplier 0.057), so we need very high initial damage to ensure
                        // getDamage()/maxHP >= 0.20 after all reductions.
                        // At Lv.100: 5.0 * 0.057 = 0.285 ≥ 0.20 threshold ✓
                        // Absorption buffer (1000) safely absorbs all remaining damage.
                        double highDamage = player.getMaxHealth() * 5.0;
                        double damage = combatSimulator.simulateHighDamageWithOverride(dummyId, highDamage);
                        if (damage > 0) {
                            defensiveSkillDamage.merge(testLevel, damage, Double::sum);
                            defensiveSkillHits.merge(testLevel, 1, Integer::sum);
                        }
                    } else {
                        // Hits 170-199: Fatal damage (override ensures death-level damage)
                        // Tests: LastStand (death prevention)
                        // Only do fatal damage if LastStand is unlocked - otherwise the player dies
                        boolean lastStandUnlocked = skills.stream()
                                .filter(s -> FATAL_DAMAGE_ARMOR_SKILLS.contains(s.getSkillId()))
                                .anyMatch(s -> testLevel >= s.getRequiredLevel());
                        if (lastStandUnlocked) {
                            combatSimulator.simulateFatalIncomingDamage(dummyId);
                            defensiveSkillDamage.merge(testLevel, 0.0, Double::sum);
                            defensiveSkillHits.merge(testLevel, 1, Integer::sum);
                        } else {
                            // Fallback to normal damage with override
                            double damage = combatSimulator.simulateIncomingDamageWithOverride(dummyId, 10.0);
                            if (damage > 0) {
                                defensiveSkillDamage.merge(testLevel, damage, Double::sum);
                                defensiveSkillHits.merge(testLevel, 1, Integer::sum);
                            }
                        }
                    }
                } else {
                    // ===== NON-ARMOR WEAPON TYPES =====
                    // Additional incoming damage for blocking defense skills (Parry)
                    // Done independently - not exclusive with the offensive attack below
                    if (hasBlockingDefenseSkills && i % 4 == 0) {
                        combatSimulator.simulateBlockingIncomingDamage(dummyId, 10.0);
                    }

                    // Choose offensive attack type
                    double damage;
                    if (hasCriticalHitSkills) {
                        // Critical attack for VorpalStrike
                        damage = combatSimulator.simulateCriticalAttack(dummyId);
                    } else if (hasOffensivePlayerHealthSkills && i >= HITS_PER_LEVEL / 2) {
                        // Set player to low HP before attack (e.g. ReapWhatYouSow)
                        player.setHealth(player.getMaxHealth() * 0.30);
                        damage = performAttack(currentType, dummyId);
                        player.setHealth(player.getMaxHealth());
                    } else {
                        // Normal attack
                        damage = performAttack(currentType, dummyId);
                    }
                    if (damage > 0) {
                        offensiveSkillDamage.computeIfAbsent(currentType, k -> new HashMap<>())
                                .merge(testLevel, damage, Double::sum);
                        offensiveSkillHits.computeIfAbsent(currentType, k -> new HashMap<>())
                                .merge(testLevel, 1, Integer::sum);
                    }
                }

                // Heal dummy after attack to keep it alive for next hit
                combatSimulator.healDummy(dummyId);
            }

            // Post-loop: test defensive weapon skills (DeathsEmbrace, DivineShield)
            // These need fatal incoming damage while holding the correct weapon
            if (hasDefensiveWeaponSkills) {
                // Force LastStand (armor skill) onto a long cooldown so it doesn't
                // steal death prevention from weapon-type skills being tested.
                // LastStand would otherwise trigger first in applySkillBonuses() and
                // cancel the event before DeathsEmbrace/DivineShield are checked.
                SkillBonus lastStandSkill = SkillBonusRegistry.getSkillById("armor_last_stand");
                if (lastStandSkill instanceof LastStandSkill ls) {
                    ls.startCooldown(player, 100); // Force 120s cooldown
                }

                for (SkillBonus skill : skills) {
                    if (!DEFENSIVE_WEAPON_SKILLS.containsKey(skill.getSkillId())) continue;
                    if (testLevel < skill.getRequiredLevel()) continue;

                    // Equip the correct weapon for the death prevention skill
                    SkillType weaponType = DEFENSIVE_WEAPON_SKILLS.get(skill.getSkillId());
                    combatSimulator.equipWeapon(weaponType);

                    for (int j = 0; j < 50; j++) {
                        if (cancelled) return;
                        resetAllCooldowns(skills);

                        // Keep LastStand on cooldown (resetAllCooldowns only resets
                        // current type's skills, but be explicit for safety)
                        if (lastStandSkill instanceof LastStandSkill ls) {
                            ls.startCooldown(player, 100);
                        }

                        if (!combatSimulator.hasDummy(dummyId)) {
                            combatSimulator.respawnIfDead(dummyId, skills.get(0));
                        }

                        combatSimulator.simulateFatalIncomingDamage(dummyId);
                    }

                    // Re-equip the primary weapon for this test type
                    combatSimulator.equipWeapon(currentType);
                }

                // Restore LastStand cooldown state
                if (lastStandSkill instanceof LastStandSkill ls) {
                    ls.endCooldown(player);
                }
            }

            // Disable PackHunter test override
            if (hasPackHunter) {
                PackHunterSkill.setTestOverrideNearbyPlayers(false);
            }

            // Restore water block if we placed one
            if (hasWaterSkills) {
                combatSimulator.restoreWaterBlock();
            }

            // Restore weather if we changed it
            if (hasStormSkills) {
                player.getWorld().setStorm(savedStormState);
            }

            // Clean up any stray projectiles/entities from this level's attacks
            combatSimulator.cleanupTestEntities();

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
        };

        // Water-dependent skills need 1 tick for the entity to register isInWater()
        // after the water block is placed. Delay the hit loop accordingly.
        if (hasWaterSkills) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!cancelled) hitLoopTask.run();
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 1);
        } else {
            hitLoopTask.run();
        }
    }

    private void checkLevelResults(int level, List<SkillBonus> skills) {
        StringBuilder sb = new StringBuilder();
        sb.append("  §7Lv.").append(level).append(": ");

        int passed = 0;
        int failed = 0;

        for (SkillBonus skill : skills) {
            boolean shouldBeUnlocked = level >= skill.getRequiredLevel();
            boolean worked;

            // Check result based on test strategy
            switch (skill.getTestStrategy()) {
                case ATTRIBUTE_CHECK -> worked = verifyAttributeEffect(skill);
                case CONDITION_SETUP -> {
                    // Conditional skills use proc count but with pre-set conditions
                    int procCount = skill.getProcCount(player);
                    worked = procCount > 0;
                    // Track proc counts for proc rate display
                    if (shouldBeUnlocked) {
                        totalProcCounts.merge(skill.getSkillId(), procCount, Integer::sum);
                        totalHitCounts.merge(skill.getSkillId(), HITS_PER_LEVEL, Integer::sum);
                    }
                }
                default -> {
                    int procCount = skill.getProcCount(player);
                    worked = procCount > 0;
                    // Track proc counts for proc rate display
                    if (shouldBeUnlocked) {
                        totalProcCounts.merge(skill.getSkillId(), procCount, Integer::sum);
                        totalHitCounts.merge(skill.getSkillId(), HITS_PER_LEVEL, Integer::sum);
                    }
                }
            }

            // Record result
            results.computeIfAbsent(skill.getSkillId(), k -> new HashMap<>()).put(level, worked == shouldBeUnlocked);

            if (shouldBeUnlocked) {
                // Skill should work
                if (worked) {
                    passed++;
                } else {
                    failed++;
                    String strategy = skill.getTestStrategy().name();
                    issues.computeIfAbsent(skill.getSkillId(), k -> new ArrayList<>())
                          .add("No procs at Lv." + level + " (should be unlocked, strategy=" + strategy + ")");
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

    private void activateAllSkills(SkillType type, List<SkillBonus> skills) {
        // Clear current selections
        for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(playerUUID, type))) {
            PlayerSkillSelection.removeActiveSkill(playerUUID, type, id);
        }
        // Activate all skills of this type, bypassing the normal 3-skill limit for testing
        for (SkillBonus skill : skills) {
            PlayerSkillSelection.addActiveSkill(playerUUID, type, skill.getSkillId(), true);
        }
        SkillBonusRegistry.applyBonuses(player, type);
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

            // Build verification tag based on test strategy
            String verTag = buildVerificationTag(skill);

            if (allPassed) {
                log("  §a✓ " + skill.getBonusName() + " §7" + verTag);
            } else {
                log("  §c✗ " + skill.getBonusName() + " §7" + verTag);
                if (skillIssues != null) {
                    for (String issue : skillIssues) {
                        log("    §c- " + issue);
                    }
                }
            }

            // Add to report
            SkillTestResult result = new SkillTestResult(
                    skill.getSkillId(), skill.getBonusName(), skill.getSkillType(), skill.getRequiredLevel());

            // Set proc rate data
            Integer totalProcs = totalProcCounts.get(skill.getSkillId());
            Integer totalHits = totalHitCounts.get(skill.getSkillId());
            if (totalProcs != null && totalHits != null && totalHits > 0) {
                result.setTotalHits(totalHits);
                result.setProcCount(totalProcs);
                // Set expected proc rate from ProcSkill interface
                if (skill instanceof ProcSkill procSkill) {
                    result.setExpectedProcRate(procSkill.getProcChance(50)); // Use mid-level expected rate
                }
            } else {
                result.setTotalHits(HITS_PER_LEVEL * TEST_LEVELS.length);
            }

            result.setVerificationTag(verTag);
            if (skillIssues != null) {
                skillIssues.forEach(result::addIssue);
            }
            report.addResult(result);
        }
    }

    private void setPlayerSkillLevel(SkillType type, int level) {
        PlayerData.setSkillXP(playerUUID, type, SkillXPCalculator.totalXPForLevel(level));
    }

    private double performAttack(SkillType skillType, String skillId) {
        return switch (skillType) {
            case BOWS, CROSSBOWS -> combatSimulator.simulateRangedAttack(skillId);
            case TRIDENTS -> combatSimulator.simulateTridentAttack(skillId);
            case ARMOR -> combatSimulator.simulateIncomingDamage(skillId, 10.0);
            // Maces use direct event creation to avoid NMS double-event on 1.21.4+
            case MACES -> combatSimulator.simulateDirectAttack(skillId);
            default -> combatSimulator.simulateMeleeAttack(skillId);
        };
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
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) savedMaxHealthBase = maxHealthAttr.getBaseValue();
        AttributeInstance maxAbsorptionAttr = player.getAttribute(Attribute.MAX_ABSORPTION);
        if (maxAbsorptionAttr != null) savedMaxAbsorption = maxAbsorptionAttr.getBaseValue();
        savedWalkSpeed = player.getWalkSpeed();
        combatSimulator.savePlayerArmor();
    }

    private void restorePlayerState() {
        // Remove all active skill bonus effects before restoring selections
        SkillBonusRegistry.removeAllBonuses(player);

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
        // Restore MAX_HEALTH base value before applying the modifier
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) maxHealthAttr.setBaseValue(savedMaxHealthBase);
        AttributeInstance maxAbsorptionAttr = player.getAttribute(Attribute.MAX_ABSORPTION);
        if (maxAbsorptionAttr != null) maxAbsorptionAttr.setBaseValue(savedMaxAbsorption);
        player.setWalkSpeed(savedWalkSpeed);
        // Remove any lingering passive attribute modifiers from testing
        removePassiveModifiers();
        combatSimulator.restorePlayerArmor();
        // Restore armor skill health bonus based on the restored XP
        ArmorSkillHealthBonus.applyHealthBonus(player);
        // Re-apply bonuses for the originally saved skill selections
        SkillBonusRegistry.applyAllBonuses(player);
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
    }

    public void cancel() {
        cancelled = true;
        if (currentTask != null) currentTask.cancel();
        log("§c§lTest cancelled!");
        File logFile = testLog.saveToFile();
        if (logFile != null) log("§7Partial log: §e" + logFile.getName());
        cleanup();
    }

    private void setupPlayerForTesting() {
        AttributeInstance attackSpeedAttr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackSpeedAttr != null) attackSpeedAttr.setBaseValue(100.0);
        AttributeInstance knockbackAttr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (knockbackAttr != null) knockbackAttr.setBaseValue(1.0);
        // MAX_ABSORPTION defaults to 0 in 1.21+, must set it so setAbsorptionAmount() works
        AttributeInstance maxAbsorptionAttr = player.getAttribute(Attribute.MAX_ABSORPTION);
        if (maxAbsorptionAttr != null) maxAbsorptionAttr.setBaseValue(2000.0);
    }

    private void completeTest() {
        // Log and wire damage validation data BEFORE storing report
        logDamageValidation();

        testLog.logSection("TEST COMPLETE");

        // Store report for later review
        report.store();

        List<String> summary = report.generateSummary();
        for (String line : summary) {
            testLog.log(line.replace("&", ""));
        }

        // Also write detailed report and export to log file
        List<String> detailed = report.generateDetailedReport();
        for (String line : detailed) {
            testLog.log(line.replace("&", ""));
        }
        String exported = report.exportToText();
        testLog.log(exported);

        File logFile = testLog.saveToFile();

        log("");
        for (String line : summary) {
            log(line.replace("&", "§"));
        }

        if (logFile != null) {
            log("§7Log saved: §e" + logFile.getName());
        }
        log("§7Use §e/em debug combat results §7to review this report.");

        // Give the player a written book with the full report
        try {
            org.bukkit.inventory.ItemStack book = report.generateBook();
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(book);
                log("§7A §eSkill Test Report §7book has been added to your inventory.");
            } else {
                player.getWorld().dropItem(player.getLocation(), book);
                log("§7A §eSkill Test Report §7book was dropped at your feet (inventory full).");
            }
        } catch (Exception e) {
            // Book generation is non-critical, don't fail the test
            log("§7Could not generate report book: " + e.getMessage());
        }

        cleanup();
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

    private void cleanup() {
        CombatSimulator.setTestingActive(false);
        HandlerList.unregisterAll(this);
        if (currentTask != null) currentTask.cancel();
        if (progressBar != null) {
            progressBar.removeAll();
            progressBar = null;
        }
        combatSimulator.restoreWaterBlock(); // Restore any water blocks before removing dummies
        player.getWorld().setStorm(savedStormState); // Restore weather
        combatSimulator.removeAllDummies();
        combatSimulator.cleanupTestEntities();
        restorePlayerState();
        activeSessions.remove(playerUUID);
    }

    /**
     * Updates the boss bar with overall progress across all weapon types.
     */
    private void updateOverallProgress() {
        if (progressBar == null || currentType == null) return;
        int totalLevels = TEST_LEVELS.length;
        double overallProgress = totalTypes > 0
                ? (double) (completedTypes * totalLevels + currentLevelIndex) / (totalTypes * totalLevels)
                : 0;
        int currentLevel = currentLevelIndex < totalLevels ? TEST_LEVELS[currentLevelIndex] : 100;
        int overallPct = (int) (overallProgress * 100);
        String title = String.format("§fTesting %s §7(%d/%d types) | Lv.%d | %d%% overall",
                currentType.getDisplayName(), completedTypes + 1, totalTypes,
                currentLevel, overallPct);
        progressBar.setTitle(title);
        progressBar.setProgress(Math.min(1.0, overallProgress));
        progressBar.setColor(BarColor.BLUE);
    }

    /**
     * Sets up conditional skill states before testing.
     * Simulates the conditions that normally require player actions.
     */
    private void setupConditionalSkillStates(List<SkillBonus> skills) {
        for (SkillBonus skill : skills) {
            if (skill.getTestStrategy() != SkillBonus.TestStrategy.CONDITION_SETUP) continue;

            if (skill instanceof OverdrawSkill) {
                OverdrawSkill.simulateFullDraw(playerUUID);
            } else if (skill instanceof SteadyAimSkill) {
                SteadyAimSkill.simulateStationary(playerUUID, 3000); // 3 seconds standing still
            } else if (skill instanceof ReturningHasteSkill) {
                ReturningHasteSkill.simulateStacks(playerUUID, 3); // Pre-set 3 stacks
            } else if (skill instanceof IronStanceSkill) {
                IronStanceSkill.simulateStationary(playerUUID); // Mark as standing still
            }
            // ExecutionerSkill and FinishingFlourishSkill use CONDITION_SETUP too,
            // but their condition (low HP dummy) is handled in testInstantLevel() via HEALTH_THRESHOLD_SKILLS
        }
    }

    /**
     * Verifies that a passive utility skill has correctly modified player attributes.
     * Uses NamespacedKey-based lookup via AttributeInstance.getModifier(Key).
     */
    private boolean verifyAttributeEffect(SkillBonus skill) {
        String skillId = skill.getSkillId();

        return switch (skillId) {
            case SwiftStrikesSkill.SKILL_ID -> player.getWalkSpeed() > 0.2f;
            case PoiseSkill.SKILL_ID -> hasModifier(Attribute.KNOCKBACK_RESISTANCE, PoiseSkill.MODIFIER_KEY_STRING);
            case FlurrySkill.SKILL_ID -> {
                // Flurry is STACKING - it only applies after hits, so check it with proc count as fallback
                yield skill.getProcCount(player) > 0 || hasModifier(Attribute.ATTACK_SPEED, FlurrySkill.MODIFIER_KEY_STRING);
            }
            case GrimReachSkill.SKILL_ID -> {
                try {
                    yield hasModifier(Attribute.ENTITY_INTERACTION_RANGE, GrimReachSkill.MODIFIER_KEY_STRING);
                } catch (NoSuchFieldError e) {
                    yield true; // Pre-1.20.5, skip check
                }
            }
            case LongReachSkill.SKILL_ID -> {
                try {
                    yield hasModifier(Attribute.ENTITY_INTERACTION_RANGE, LongReachSkill.MODIFIER_KEY_STRING);
                } catch (NoSuchFieldError e) {
                    yield true; // Pre-1.20.5, skip check
                }
            }
            case PolearmMasterySkill.SKILL_ID -> hasModifier(Attribute.ATTACK_SPEED, PolearmMasterySkill.MODIFIER_KEY_STRING);
            default -> skill.getProcCount(player) > 0; // Fallback to proc count
        };
    }

    /**
     * Checks if the player has an attribute modifier with the given key string.
     * Uses NamespacedKey-based matching via AttributeModifier.getKey().
     */
    private boolean hasModifier(Attribute attribute, String modifierKeyString) {
        AttributeInstance attr = player.getAttribute(attribute);
        if (attr == null) return false;
        NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, modifierKeyString);
        for (AttributeModifier mod : attr.getModifiers()) {
            if (mod.getKey().equals(key)) return true;
        }
        return false;
    }

    /**
     * Builds a verification tag string for display based on test strategy and results.
     */
    private String buildVerificationTag(SkillBonus skill) {
        switch (skill.getTestStrategy()) {
            case ATTRIBUTE_CHECK -> {
                return "(ATTR OK)";
            }
            case CONDITION_SETUP -> {
                Integer procs = totalProcCounts.get(skill.getSkillId());
                Integer hits = totalHitCounts.get(skill.getSkillId());
                if (procs != null && hits != null && hits > 0) {
                    return String.format("(COND %d/%d %.0f%%)", procs, hits, (double) procs / hits * 100);
                }
                return "(COND OK)";
            }
            default -> {
                Integer procs = totalProcCounts.get(skill.getSkillId());
                Integer hits = totalHitCounts.get(skill.getSkillId());
                if (procs != null && hits != null && hits > 0) {
                    double rate = (double) procs / hits * 100;
                    String expectedStr = "";
                    if (skill instanceof ProcSkill procSkill) {
                        double expected = procSkill.getProcChance(50) * 100;
                        expectedStr = String.format("/%.0f%%", expected);
                    }
                    return String.format("(PROC %.0f%%%s)", rate, expectedStr);
                }
                return "(unlock Lv." + skill.getRequiredLevel() + ")";
            }
        }
    }

    // ===== DAMAGE BASELINE METHODS =====

    /**
     * Removes passive attribute modifiers that were applied during testing.
     */
    private void removePassiveModifiers() {
        SwiftStrikesSkill.removeSpeedBonus(player);
        PoiseSkill.removeKnockbackResistance(player);
        FlurrySkill.removeAttackSpeedModifier(player);
        GrimReachSkill.removeReachBonus(player);
        LongReachSkill.removeReachBonus(player);
        PolearmMasterySkill.removeAttackSpeedBonus(player);
    }

    /**
     * Runs an offensive baseline damage check (no skills active) for the given type and level.
     * Records expected vs actual damage in {@link #offensiveBaseline}.
     */
    private void runOffensiveBaseline(int level, SkillType type, String dummyId) {
        // Clear all skills for this type
        for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(playerUUID, type))) {
            PlayerSkillSelection.removeActiveSkill(playerUUID, type, id);
        }

        // Ensure dummy is alive
        List<SkillBonus> skills = skillsByType.get(type);
        if (!combatSimulator.hasDummy(dummyId) && skills != null && !skills.isEmpty()) {
            combatSimulator.respawnIfDead(dummyId, skills.get(0));
        }

        double totalDamage = 0;
        int validHits = 0;
        double expected = 0;
        boolean isMelee = (type == SkillType.SWORDS || type == SkillType.AXES);

        // For melee types, get expected damage from a breakdown sample
        if (isMelee) {
            DamageBreakdown breakdown = combatSimulator.simulateMeleeAttackWithBreakdown(dummyId);
            if (breakdown != null) {
                int weaponLevel = breakdown.getItemLevel();
                double attackSpeed = type == SkillType.AXES ? 1.0 : 1.6;
                expected = DamageBreakdown.calculateExpectedDamage(weaponLevel, level, 50, attackSpeed);
            }
            combatSimulator.healDummy(dummyId);
        }

        // Perform baseline attacks
        for (int i = 0; i < BASELINE_HITS; i++) {
            if (!combatSimulator.hasDummy(dummyId) && skills != null && !skills.isEmpty()) {
                combatSimulator.respawnIfDead(dummyId, skills.get(0));
            }
            double dmg = performAttack(type, dummyId);
            if (dmg > 0) {
                totalDamage += dmg;
                validHits++;
            }
            combatSimulator.healDummy(dummyId);
        }

        double avgDamage = validHits > 0 ? totalDamage / validHits : 0;
        offensiveBaseline.computeIfAbsent(type, k -> new HashMap<>())
                .put(level, new double[]{expected, avgDamage});
    }

    /**
     * Runs a defensive baseline damage check (no armor skills active) for the given level.
     * Records expected vs actual incoming damage in {@link #defensiveBaseline}.
     */
    private void runDefensiveBaseline(int level, String dummyId) {
        // Clear all armor skills
        for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(playerUUID, SkillType.ARMOR))) {
            PlayerSkillSelection.removeActiveSkill(playerUUID, SkillType.ARMOR, id);
        }

        // Ensure dummy is alive
        List<SkillBonus> skills = skillsByType.get(SkillType.ARMOR);
        if (!combatSimulator.hasDummy(dummyId) && skills != null && !skills.isEmpty()) {
            combatSimulator.respawnIfDead(dummyId, skills.get(0));
        }

        double totalDamage = 0;
        int validHits = 0;

        for (int i = 0; i < BASELINE_HITS; i++) {
            double dmg = combatSimulator.simulateIncomingDamage(dummyId, 10.0);
            if (dmg > 0) {
                totalDamage += dmg;
                validHits++;
            }
        }

        double avgDamage = validHits > 0 ? totalDamage / validHits : 0;
        // mob level is 50 (damage test dummy)
        double expected = CombatSimulator.calculateExpectedDefensiveDamage(level, 50, player);
        defensiveBaseline.put(level, new double[]{expected, avgDamage});
    }

    /**
     * Builds and logs the damage validation section, and wires data into the report.
     */
    private void logDamageValidation() {
        testLog.logSection("DAMAGE VALIDATION");

        // Build offensive damage data for report
        Map<SkillType, Map<Integer, double[]>> offData = new HashMap<>();
        for (var typeEntry : offensiveBaseline.entrySet()) {
            SkillType type = typeEntry.getKey();
            Map<Integer, double[]> levelData = new HashMap<>();
            for (var lvEntry : typeEntry.getValue().entrySet()) {
                int level = lvEntry.getKey();
                double expectedVal = lvEntry.getValue()[0];
                double baseline = lvEntry.getValue()[1];
                double avgSkill = 0;
                Map<Integer, Double> totals = offensiveSkillDamage.get(type);
                Map<Integer, Integer> counts = offensiveSkillHits.get(type);
                if (totals != null && counts != null) {
                    Double total = totals.get(level);
                    Integer count = counts.get(level);
                    if (total != null && count != null && count > 0)
                        avgSkill = total / count;
                }
                levelData.put(level, new double[]{expectedVal, baseline, avgSkill});
            }
            offData.put(type, levelData);
        }
        report.setOffensiveDamageData(offData);

        // Build defensive damage data for report
        Map<Integer, double[]> defData = new HashMap<>();
        for (var lvEntry : defensiveBaseline.entrySet()) {
            int level = lvEntry.getKey();
            double expectedVal = lvEntry.getValue()[0];
            double baseline = lvEntry.getValue()[1];
            double avgSkill = 0;
            Double total = defensiveSkillDamage.get(level);
            Integer count = defensiveSkillHits.get(level);
            if (total != null && count != null && count > 0)
                avgSkill = total / count;
            defData.put(level, new double[]{expectedVal, baseline, avgSkill});
        }
        report.setDefensiveDamageData(defData);

        // Log offensive
        testLog.log("OFFENSIVE (Player -> Elite):");
        log("§6=== DAMAGE VALIDATION ===");
        log("§eOFFENSIVE (Player -> Elite):");
        for (SkillType type : SkillType.values()) {
            Map<Integer, double[]> data = offData.get(type);
            if (data == null || data.isEmpty()) continue;

            StringBuilder sb = new StringBuilder();
            sb.append("  §7").append(type.getDisplayName()).append(":");
            testLog.log("  " + type.getDisplayName() + ":");

            for (int level : TEST_LEVELS) {
                double[] vals = data.get(level);
                if (vals == null) continue;
                double expectedVal = vals[0];
                double baseline = vals[1];
                double avgSkill = vals[2];

                if (expectedVal > 0) {
                    double pctOff = expectedVal != 0 ? ((baseline - expectedVal) / expectedVal) * 100 : 0;
                    String line = String.format("    Lv.%d: Expected: %.1f  Actual: %.1f  (%+.1f%%)",
                            level, expectedVal, baseline, pctOff);
                    if (avgSkill > 0) {
                        line += String.format(" | With skills: %.1f", avgSkill);
                    }
                    testLog.log(line);
                } else {
                    String line = String.format("    Lv.%d: Actual: %.1f (no formula expected)", level, baseline);
                    if (avgSkill > 0) {
                        line += String.format(" | With skills: %.1f", avgSkill);
                    }
                    testLog.log(line);
                }
            }

            // Condensed in-game view at level 50
            double[] lv50 = data.get(50);
            if (lv50 != null) {
                if (lv50[0] > 0) {
                    double pctOff = ((lv50[1] - lv50[0]) / lv50[0]) * 100;
                    sb.append(String.format(" Baseline @Lv50: %.1f/%.1f (%+.1f%%)", lv50[1], lv50[0], pctOff));
                } else {
                    sb.append(String.format(" Baseline @Lv50: %.1f", lv50[1]));
                }
                if (lv50[2] > 0 && lv50[1] > 0) {
                    sb.append(String.format(" | Skills: %.1f (%.2fx)", lv50[2], lv50[2] / lv50[1]));
                }
            }
            log(sb.toString());
        }

        // Log defensive
        if (!defData.isEmpty()) {
            testLog.log("DEFENSIVE (Elite -> Player):");
            log("§eDEFENSIVE (Elite -> Player):");
            StringBuilder sb = new StringBuilder("  §7Armor:");
            testLog.log("  Armor:");

            for (int level : TEST_LEVELS) {
                double[] vals = defData.get(level);
                if (vals == null) continue;
                double expectedVal = vals[0];
                double baseline = vals[1];
                double avgSkill = vals[2];

                double pctOff = expectedVal != 0 ? ((baseline - expectedVal) / expectedVal) * 100 : 0;
                String line = String.format("    Lv.%d: Expected: %.1f  Actual: %.1f  (%+.1f%%)",
                        level, expectedVal, baseline, pctOff);
                if (avgSkill > 0) {
                    line += String.format(" | With skills: %.1f", avgSkill);
                }
                testLog.log(line);
            }

            // Condensed in-game view at level 50
            double[] lv50 = defData.get(50);
            if (lv50 != null) {
                double pctOff = lv50[0] != 0 ? ((lv50[1] - lv50[0]) / lv50[0]) * 100 : 0;
                sb.append(String.format(" Baseline @Lv50: %.1f/%.1f (%+.1f%%)", lv50[1], lv50[0], pctOff));
                if (lv50[2] > 0 && lv50[1] > 0) {
                    sb.append(String.format(" | Skills: %.1f (%.2fx)", lv50[2], lv50[2] / lv50[1]));
                }
            }
            log(sb.toString());
        }
    }
}
