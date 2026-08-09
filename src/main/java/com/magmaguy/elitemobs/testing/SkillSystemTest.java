package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.IronStanceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.LastStandSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.axes.ExecutionerSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.DeadEyeSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.OverdrawSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.PackHunterSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.HuntersPreySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.SteadyAimSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.DeathsEmbraceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.ReapWhatYouSowSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.ReapersHarvestSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.SoulSiphonSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.DivineShieldSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.DepthChargeSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.ReturningHasteSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.RiptideMasterySkill;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;

/**
 * Admin-only batch diagnostic for the skill system. It tests each weapon type across the configured
 * level checkpoints, grades observed effects, and produces a report. Session mutation, observations,
 * and combat simulation are delegated to dedicated owners so cancellation can restore player state.
 * Kill-triggered skills use a separate real-dummy kill phase because the normal hit loop heals its
 * target after every attack.
 */
public class SkillSystemTest implements Listener {

    private static final Map<UUID, SkillSystemTest> activeSessions = new HashMap<>();

    // Test configuration
    static final int[] TEST_LEVELS = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    static final int HITS_PER_LEVEL = 200;

    // Skills that cannot be tested in the batch test and should be marked as SKIPPED
    private static final Map<String, String> SKIP_REASONS = Map.ofEntries(
            Map.entry("spears_phalanx", "defensive (frontal attack)")
    );

    /**
     * Skills that bank their state on real elite <b>kills</b> rather than on hits.
     * <p>
     * The hit loop heals the dummy after every attack so it survives 200 swings, which means these
     * skills never see an {@code EliteMobDeathEvent}. They get a dedicated kill phase after the hit
     * loop ({@link #runKillPhase}) that genuinely kills and respawns the dummy, and they are graded
     * on banked stacks rather than on proc count.
     * <p>
     * Proc count is not a usable signal here: {@code EliteMobDamagedByPlayerEvent} increments Soul
     * Siphon's proc count on every swing just to <i>read</i> its stacks for the damage bonus, so a
     * proc-count grade would pass with a permanently empty stack bar.
     */
    static final Set<String> KILL_REQUIRED_SKILLS = Set.of(SoulSiphonSkill.SKILL_ID);

    /**
     * Kills performed per level for {@link #KILL_REQUIRED_SKILLS}. Above Soul Siphon's 10 stack
     * maximum on purpose, so the run proves both that stacks accumulate and that they stop at the
     * cap rather than climbing forever.
     */
    static final int KILLS_PER_LEVEL = 12;

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

    // Skills that need critical hits to proc. Dead Eye is gated on isCriticalStrike() in its
    // tryActivate, so without it here the BOWS run reports 0 procs for a skill that works fine.
    // The critical attack path drops the rangedAttack flag, which only ReturningHaste (TRIDENTS)
    // reads, so routing the BOWS run through it does not disturb the other bow skills.
    private static final Set<String> CRITICAL_HIT_SKILLS = Set.of(
            VorpalStrikeSkill.SKILL_ID,
            DeadEyeSkill.SKILL_ID
    );

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
    static final int BASELINE_HITS = 10;
    // Flag for whether SPEARS skill type is available on this server
    private static boolean spearsAvailable = true;

    @Getter
    private final Player player;
    @Getter
    private final UUID playerUUID;

    private final CombatSimulator combatSimulator;
    private final CombatTestLog testLog;
    private final TestReport report;
    private final SkillTestEvaluator evaluator;
    private final SkillTestPlayerState playerState;

    private boolean cancelled = false;
    private boolean cleaned;
    private boolean stormStateCaptured;
    private World savedStormWorld;
    private boolean packHunterOverrideActive;
    private final Set<BukkitTask> scheduledTasks = new HashSet<>();
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

    // Killing blows landed during the current level's kill phase. They are real attacks and do
    // increment proc counts, so they have to count towards the proc-rate denominator too.
    private int killPhaseHits = 0;
    // Skills grouped by type
    private final Map<SkillType, List<SkillBonus>> skillsByType = new EnumMap<>(SkillType.class);

    // Test queue
    private final Queue<SkillType> typeQueue = new LinkedList<>();
    private SkillType currentType = null;
    private int currentLevelIndex = 0;

    // Overall progress tracking
    private int totalTypes = 0;
    private int completedTypes = 0;
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
        this.evaluator = new SkillTestEvaluator(player, combatSimulator, testLog, report, skillsByType);
        this.playerState = new SkillTestPlayerState(player, combatSimulator);
        this.filterType = filterType;
    }

    public static SkillSystemTest getSession(UUID playerUUID) {
        return activeSessions.get(playerUUID);
    }

    public static boolean hasActiveSession(UUID playerUUID) {
        return activeSessions.containsKey(playerUUID);
    }

    public static boolean hasActiveSession() {
        return !activeSessions.isEmpty();
    }

    public static void shutdown() {
        for (SkillSystemTest session : new ArrayList<>(activeSessions.values())) session.cancel();
        activeSessions.clear();
        CombatSimulator.setTestingActive(false);
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
        if (!activeSessions.isEmpty()) {
            SkillSystemTest active = activeSessions.values().iterator().next();
            log("§cA combat diagnostic is already running for " + active.player.getName() + ".");
            return;
        }

        // Set testing active flag to suppress entity spawning

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
            return;
        }

        activeSessions.put(playerUUID, this);
        Bukkit.getPluginManager().registerEvents(this, MetadataHandler.PLUGIN);
        CombatSimulator.setTestingActive(true);

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
        try {
            playerState.captureAndPrepare();
        } catch (RuntimeException exception) {
            log("§cFailed to prepare the combat diagnostic: " + exception.getMessage());
            cleanup();
            return;
        }

        // Start testing
        testNextType();
    }

    private void testNextType() {
        if (cancelled) {
            cleanup();
            return;
        }
        if (typeQueue.isEmpty()) {
            completeTest();
            return;
        }

        // Clean up previous dummies, stray entities, and add delay for entity despawn
        combatSimulator.removeAllDummies();
        combatSimulator.cleanupTestEntities();

        currentType = typeQueue.poll();
        currentLevelIndex = 0;

        // Reset per-skill tracking for this type
        evaluator.resetTypeCounters();

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
        schedule(() -> {
            if (cancelled) return;
            SkillBonus firstSkill = testableSkills.get(0);
            if (!combatSimulator.spawnSingleDummy(firstSkill)) {
                log("§cFailed to spawn dummy for " + currentType.getDisplayName());
                testNextType();
                return;
            }
            testNextLevel();
        }, 2);
    }

    private void testNextLevel() {
        if (cancelled) {
            cleanup();
            return;
        }

        if (currentLevelIndex >= TEST_LEVELS.length) {
            // Done with all levels for this type
            evaluator.summarizeType(currentType);
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
            evaluator.runDefensiveBaseline(testLevel, baselineDummyId);
        } else {
            evaluator.runOffensiveBaseline(testLevel, currentType, baselineDummyId);
        }

        killPhaseHits = 0;

        // Kill-banked state lives in static per-player maps and survives between levels, so a
        // stack banked at Lv.30 would still be sitting there at Lv.40 and would also make the
        // "should be LOCKED" levels report stacks the current level never earned. Clear it first;
        // activateAllSkills re-registers the skill only if this level meets its unlock level.
        for (SkillBonus skill : skills)
            if (KILL_REQUIRED_SKILLS.contains(skill.getSkillId())) skill.onDeactivate(player);

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

        // Skills that only bank state on elite kills need the dummy to actually die
        boolean hasKillSkills = skills.stream()
                .anyMatch(s -> KILL_REQUIRED_SKILLS.contains(s.getSkillId()));

        // Enable PackHunter test override if present
        boolean hasPackHunter = skills.stream()
                .anyMatch(s -> PackHunterSkill.SKILL_ID.equals(s.getSkillId()));
        if (hasPackHunter) {
            PackHunterSkill.setTestOverrideNearbyPlayers(true);
            packHunterOverrideActive = true;
        }

        // Place water at dummy for water-dependent skills (e.g. Depth Charge)
        if (hasWaterSkills) {
            combatSimulator.placeWaterAtDummy(dummyId);
        }

        // Set storm weather for storm-dependent skills (e.g. Riptide Mastery)
        if (hasStormSkills) {
            savedStormState = player.getWorld().hasStorm();
            savedStormWorld = player.getWorld();
            stormStateCaptured = true;
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
                            evaluator.recordDefensiveDamage(testLevel, damage);
                        }
                    } else if (i < 120) {
                        // Hits 100-119: Very low HP (15%) for SecondWind (<25%) and AdrenalineSurge (<30%)
                        // Player at 15% ensures projected health drops below both thresholds
                        // even after IronStance+BattleHardened+Fortify reduce getDamage().
                        double lowHpDamage = player.getMaxHealth() * 0.25;
                        double damage = combatSimulator.simulateIncomingDamage(dummyId, lowHpDamage, 0.15);
                        if (damage > 0) {
                            evaluator.recordDefensiveDamage(testLevel, damage);
                        }
                    } else if (i < 140) {
                        // Hits 120-139: Moderate low HP (40%) for Grit (<50% threshold)
                        // At 40%, Grit's conditionMet (health < 50%) returns true, but
                        // projected health stays above SecondWind's 25% threshold, so
                        // SecondWind doesn't heal the player above 50% and break Grit.
                        double gritDamage = player.getMaxHealth() * 0.25;
                        double damage = combatSimulator.simulateIncomingDamage(dummyId, gritDamage, 0.40);
                        if (damage > 0) {
                            evaluator.recordDefensiveDamage(testLevel, damage);
                        }
                    } else if (i < 170) {
                        // Hits 140-169: High damage (500% of max HP) with override
                        // Tests: ReactiveShielding (>20% max HP threshold)
                        // Every defensive skill source combined is now clamped by
                        // PlayerDamagedByEliteMobEvent.MAX_AGGREGATE_DEFENSIVE_REDUCTION (0.85), so a
                        // fully stacked IronStance+BattleHardened+Fortify build still lets through at
                        // least 15% of the hit instead of the ~94% reduction the old per-skill-only
                        // clamps allowed. Worst case at Lv.100: 5.0 * 0.15 = 0.75 ≥ 0.20 threshold ✓
                        // Absorption buffer (1000) safely absorbs all remaining damage.
                        double highDamage = player.getMaxHealth() * 5.0;
                        double damage = combatSimulator.simulateHighDamageWithOverride(dummyId, highDamage);
                        if (damage > 0) {
                            evaluator.recordDefensiveDamage(testLevel, damage);
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
                            evaluator.recordDefensiveDamage(testLevel, 0.0);
                        } else {
                            // Fallback to normal damage with override
                            double damage = combatSimulator.simulateIncomingDamageWithOverride(dummyId, 10.0);
                            if (damage > 0) {
                                evaluator.recordDefensiveDamage(testLevel, damage);
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
                        evaluator.recordOffensiveDamage(currentType, testLevel, damage);
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

            // Post-loop: kill phase for skills that only bank state on elite deaths.
            // Runs last so the 200 measured hits are never taken against a dying or freshly
            // respawned dummy, which would corrupt the offensive damage averages.
            if (hasKillSkills) {
                // The defensive-weapon-skill block above swaps weapons; make sure the killing
                // blows are landed with this type's weapon, since Soul Siphon only credits a kill
                // made with a hoe.
                combatSimulator.equipWeapon(currentType);
                runKillPhase(testLevel, skills, dummyId);
            }

            // Disable PackHunter test override
            if (hasPackHunter) {
                PackHunterSkill.setTestOverrideNearbyPlayers(false);
                packHunterOverrideActive = false;
            }

            // Restore water block if we placed one
            if (hasWaterSkills) {
                combatSimulator.restoreWaterBlock();
            }

            // Restore weather if we changed it
            if (hasStormSkills) {
                savedStormWorld.setStorm(savedStormState);
            }

            // Clean up any stray projectiles/entities from this level's attacks
            combatSimulator.cleanupTestEntities();

            updateProgressBar(currentType.getDisplayName() + " Lv." + testLevel, HITS_PER_LEVEL, HITS_PER_LEVEL, BarColor.GREEN);

            // Check results and move to next level
            evaluator.checkLevelResults(testLevel, skills, killPhaseHits);
            currentLevelIndex++;

            // Small delay before next level for UI update
            schedule(this::testNextLevel, 1);
        };

        // Water-dependent skills need 1 tick for the entity to register isInWater()
        // after the water block is placed. Delay the hit loop accordingly.
        if (hasWaterSkills) {
            schedule(hitLoopTask, 1);
        } else {
            hitLoopTask.run();
        }
    }

    /**
     * Kills and respawns the dummy {@link #KILLS_PER_LEVEL} times, recording the highest stack
     * count each {@link #KILL_REQUIRED_SKILLS} skill reached.
     * <p>
     * This is the measurement Soul Siphon needs: its stacks come from its own
     * {@code EliteMobDeathEvent} listener, so the only way to observe them is to give it real
     * deaths to listen to. Stacks are read after every kill rather than once at the end, so a
     * skill that banks a stack and immediately loses it is still visible.
     * <p>
     * Runs at every test level, including the locked ones. At a level below the skill's unlock,
     * {@code SkillBonusRegistry.applyBonuses} never registers the player, so the listener ignores
     * the kills and zero stacks is the correct - and now actually measured - result.
     */
    private void runKillPhase(int testLevel, List<SkillBonus> skills, String dummyId) {
        List<SkillBonus> killSkills = skills.stream()
                .filter(s -> KILL_REQUIRED_SKILLS.contains(s.getSkillId()))
                .toList();
        if (killSkills.isEmpty()) return;

        SkillBonus respawnTemplate = skills.get(0);
        int kills = 0;

        for (int i = 0; i < KILLS_PER_LEVEL; i++) {
            if (cancelled) return;

            if (!combatSimulator.hasDummy(dummyId) && !combatSimulator.respawnIfDead(dummyId, respawnTemplate)) {
                log("§cKill phase: failed to respawn dummy at Lv." + testLevel);
                break;
            }

            if (!combatSimulator.simulateKill(dummyId)) break;
            kills++;

            // Read stacks while the kill is still fresh - Soul Siphon decays them after 12s.
            evaluator.observeKillStacks(testLevel, killSkills);

            combatSimulator.respawnIfDead(dummyId, respawnTemplate);
        }

        killPhaseHits = kills;
        evaluator.finishKillPhase(testLevel, kills, killSkills);

        if (kills < KILLS_PER_LEVEL)
            log("§eKill phase: only " + kills + "/" + KILLS_PER_LEVEL + " kills landed at Lv." + testLevel);
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

    public void cancel() {
        if (cleaned) return;
        cancelled = true;
        log("§c§lTest cancelled!");
        File logFile = testLog.saveToFile();
        if (logFile != null) log("§7Partial log: §e" + logFile.getName());
        cleanup();
    }

    private void completeTest() {
        if (cleaned || cancelled) return;
        // Log and wire damage validation data BEFORE storing report
        evaluator.logDamageValidation();

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
        if (cleaned) return;
        cleaned = true;
        CombatSimulator.setTestingActive(false);
        CombatSimulator.setBlockingOverride(false);
        CombatSimulator.setTestDamageOverride(-1);
        HandlerList.unregisterAll(this);
        for (BukkitTask task : new ArrayList<>(scheduledTasks)) task.cancel();
        scheduledTasks.clear();
        if (progressBar != null) {
            progressBar.removeAll();
            progressBar = null;
        }
        cleanupSafely("water block", combatSimulator::restoreWaterBlock);
        if (stormStateCaptured && savedStormWorld != null)
            cleanupSafely("weather", () -> savedStormWorld.setStorm(savedStormState));
        if (packHunterOverrideActive)
            cleanupSafely("Pack Hunter override", () -> PackHunterSkill.setTestOverrideNearbyPlayers(false));
        cleanupSafely("test dummies", combatSimulator::removeAllDummies);
        cleanupSafely("test projectiles", combatSimulator::cleanupTestEntities);
        cleanupSafely("player state", playerState::restore);
        activeSessions.remove(playerUUID);
    }

    private void cleanupSafely(String state, Runnable cleanupAction) {
        try {
            cleanupAction.run();
        } catch (RuntimeException exception) {
            Logger.warn("Failed to restore combat diagnostic " + state + ": " + exception.getMessage());
        }
    }

    private void schedule(Runnable runnable, long delayTicks) {
        BukkitTask[] taskReference = new BukkitTask[1];
        taskReference[0] = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
            scheduledTasks.remove(taskReference[0]);
            if (!cleaned && !cancelled) runnable.run();
        }, delayTicks);
        scheduledTasks.add(taskReference[0]);
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

}
