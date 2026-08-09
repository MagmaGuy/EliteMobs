package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.DamageBreakdown;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.GrimReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.LongReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PolearmMasterySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.FlurrySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.PoiseSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.SwiftStrikesSkill;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.*;

/** Owns skill-test observations, grading, summaries, and damage-validation reporting. */
final class SkillTestEvaluator {

    private final Player player;
    private final UUID playerId;
    private final CombatSimulator simulator;
    private final CombatTestLog testLog;
    private final TestReport report;
    private final Map<SkillType, List<SkillBonus>> skillsByType;

    private final Map<String, Integer> totalProcCounts = new HashMap<>();
    private final Map<String, Integer> totalHitCounts = new HashMap<>();
    private final Map<String, Map<Integer, Integer>> killStacksObserved = new HashMap<>();
    private final Map<String, Integer> killsLanded = new HashMap<>();
    private final Map<String, Map<Integer, Boolean>> results = new HashMap<>();
    private final Map<String, List<String>> issues = new HashMap<>();
    private final Map<SkillType, Map<Integer, double[]>> offensiveBaseline = new HashMap<>();
    private final Map<Integer, double[]> defensiveBaseline = new HashMap<>();
    private final Map<SkillType, Map<Integer, Double>> offensiveSkillDamage = new HashMap<>();
    private final Map<SkillType, Map<Integer, Integer>> offensiveSkillHits = new HashMap<>();
    private final Map<Integer, Double> defensiveSkillDamage = new HashMap<>();
    private final Map<Integer, Integer> defensiveSkillHits = new HashMap<>();

    SkillTestEvaluator(Player player, CombatSimulator simulator, CombatTestLog testLog, TestReport report,
                       Map<SkillType, List<SkillBonus>> skillsByType) {
        this.player = player;
        this.playerId = player.getUniqueId();
        this.simulator = simulator;
        this.testLog = testLog;
        this.report = report;
        this.skillsByType = skillsByType;
    }

    void resetTypeCounters() {
        totalProcCounts.clear();
        totalHitCounts.clear();
    }

    void observeKillStacks(int level, List<SkillBonus> skills) {
        for (SkillBonus skill : skills) {
            if (!(skill instanceof StackingSkill stackingSkill)) continue;
            killStacksObserved.computeIfAbsent(skill.getSkillId(), ignored -> new HashMap<>())
                    .merge(level, stackingSkill.getCurrentStacks(player), Math::max);
        }
    }

    void finishKillPhase(int level, int kills, List<SkillBonus> skills) {
        for (SkillBonus skill : skills) {
            killsLanded.merge(skill.getSkillId(), kills, Integer::sum);
            killStacksObserved.computeIfAbsent(skill.getSkillId(), ignored -> new HashMap<>()).putIfAbsent(level, 0);
        }
    }

    void recordDefensiveDamage(int level, double damage) {
        defensiveSkillDamage.merge(level, damage, Double::sum);
        defensiveSkillHits.merge(level, 1, Integer::sum);
    }

    void recordOffensiveDamage(SkillType type, int level, double damage) {
        offensiveSkillDamage.computeIfAbsent(type, ignored -> new HashMap<>()).merge(level, damage, Double::sum);
        offensiveSkillHits.computeIfAbsent(type, ignored -> new HashMap<>()).merge(level, 1, Integer::sum);
    }

    void checkLevelResults(int level, List<SkillBonus> skills, int killPhaseHits) {
        StringBuilder summary = new StringBuilder("  §7Lv.").append(level).append(": ");
        int passed = 0;
        int failed = 0;

        for (SkillBonus skill : skills) {
            boolean shouldBeUnlocked = level >= skill.getRequiredLevel();
            boolean worked;
            if (SkillSystemTest.KILL_REQUIRED_SKILLS.contains(skill.getSkillId())) {
                int stacks = killStacksAt(skill.getSkillId(), level);
                worked = stacks > 0;
                results.computeIfAbsent(skill.getSkillId(), ignored -> new HashMap<>()).put(level, worked == shouldBeUnlocked);
                if (worked == shouldBeUnlocked) passed++;
                else {
                    failed++;
                    issues.computeIfAbsent(skill.getSkillId(), ignored -> new ArrayList<>()).add(shouldBeUnlocked
                            ? "No stacks banked at Lv." + level + " after " + SkillSystemTest.KILLS_PER_LEVEL + " kills"
                            : "Banked " + stacks + " stacks at Lv." + level + " (should be LOCKED, unlock="
                            + skill.getRequiredLevel() + ")");
                }
                continue;
            }

            worked = skill.getTestStrategy() == SkillBonus.TestStrategy.ATTRIBUTE_CHECK
                    ? verifyAttributeEffect(skill)
                    : skill.getProcCount(player) > 0;
            if (shouldBeUnlocked && skill.getTestStrategy() != SkillBonus.TestStrategy.ATTRIBUTE_CHECK) {
                totalProcCounts.merge(skill.getSkillId(), skill.getProcCount(player), Integer::sum);
                totalHitCounts.merge(skill.getSkillId(), SkillSystemTest.HITS_PER_LEVEL + killPhaseHits, Integer::sum);
            }

            results.computeIfAbsent(skill.getSkillId(), ignored -> new HashMap<>()).put(level, worked == shouldBeUnlocked);
            if (worked == shouldBeUnlocked) passed++;
            else {
                failed++;
                String issue = shouldBeUnlocked
                        ? "No procs at Lv." + level + " (should be unlocked, strategy=" + skill.getTestStrategy().name() + ")"
                        : "Procced at Lv." + level + " (should be LOCKED, unlock=" + skill.getRequiredLevel() + ")";
                issues.computeIfAbsent(skill.getSkillId(), ignored -> new ArrayList<>()).add(issue);
            }
        }

        if (failed == 0) summary.append("§a✓ All ").append(passed).append(" passed");
        else summary.append("§a").append(passed).append(" passed, §c").append(failed).append(" failed");
        player.sendMessage(summary.toString());
        testLog.log("Level " + level + ": " + passed + " passed, " + failed + " failed");
    }

    void summarizeType(SkillType type) {
        List<SkillBonus> skills = skillsByType.get(type);
        player.sendMessage("");
        player.sendMessage("§7--- " + type.getDisplayName() + " Summary ---");
        for (SkillBonus skill : skills) {
            Map<Integer, Boolean> skillResults = results.get(skill.getSkillId());
            List<String> skillIssues = issues.get(skill.getSkillId());
            if (skillResults == null) continue;

            boolean allPassed = skillResults.values().stream().filter(Boolean::booleanValue).count()
                    == SkillSystemTest.TEST_LEVELS.length;
            String verificationTag = buildVerificationTag(skill);
            player.sendMessage("  " + (allPassed ? "§a✓ " : "§c✗ ") + skill.getBonusName() + " §7" + verificationTag);
            if (!allPassed && skillIssues != null) {
                for (String issue : skillIssues) player.sendMessage("    §c- " + issue);
            }

            SkillTestResult result = new SkillTestResult(
                    skill.getSkillId(), skill.getBonusName(), skill.getSkillType(), skill.getRequiredLevel());
            Integer procs = totalProcCounts.get(skill.getSkillId());
            Integer hits = totalHitCounts.get(skill.getSkillId());
            if (procs != null && hits != null && hits > 0) {
                result.setTotalHits(hits);
                result.setProcCount(procs);
                if (skill instanceof ProcSkill procSkill) result.setExpectedProcRate(procSkill.getProcChance(50));
            } else result.setTotalHits(SkillSystemTest.HITS_PER_LEVEL * SkillSystemTest.TEST_LEVELS.length);
            result.setVerificationTag(verificationTag);
            if (skillIssues != null) skillIssues.forEach(result::addIssue);
            report.addResult(result);
        }
    }

    void runOffensiveBaseline(int level, SkillType type, String dummyId) {
        for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(playerId, type))) {
            PlayerSkillSelection.removeActiveSkill(playerId, type, id);
        }
        List<SkillBonus> skills = skillsByType.get(type);
        respawnIfNeeded(dummyId, skills);

        double totalDamage = 0;
        int validHits = 0;
        double expected = 0;
        if (type == SkillType.SWORDS || type == SkillType.AXES) {
            DamageBreakdown breakdown = simulator.simulateMeleeAttackWithBreakdown(dummyId);
            if (breakdown != null) {
                double attackSpeed = type == SkillType.AXES ? 1.0 : 1.6;
                String weaponType = type == SkillType.AXES ? "DIAMOND_AXE" : "DIAMOND_SWORD";
                expected = DamageBreakdown.calculateExpectedDamage(
                        breakdown.getItemLevel(), level, 50, weaponType, attackSpeed);
            }
            simulator.healDummy(dummyId);
        }
        for (int index = 0; index < SkillSystemTest.BASELINE_HITS; index++) {
            respawnIfNeeded(dummyId, skills);
            double damage = performAttack(type, dummyId);
            if (damage > 0) {
                totalDamage += damage;
                validHits++;
            }
            simulator.healDummy(dummyId);
        }
        offensiveBaseline.computeIfAbsent(type, ignored -> new HashMap<>())
                .put(level, new double[]{expected, validHits > 0 ? totalDamage / validHits : 0});
    }

    void runDefensiveBaseline(int level, String dummyId) {
        for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(playerId, SkillType.ARMOR))) {
            PlayerSkillSelection.removeActiveSkill(playerId, SkillType.ARMOR, id);
        }
        List<SkillBonus> skills = skillsByType.get(SkillType.ARMOR);
        respawnIfNeeded(dummyId, skills);
        double totalDamage = 0;
        int validHits = 0;
        for (int index = 0; index < SkillSystemTest.BASELINE_HITS; index++) {
            double damage = simulator.simulateIncomingDamage(dummyId, 10.0);
            if (damage > 0) {
                totalDamage += damage;
                validHits++;
            }
        }
        defensiveBaseline.put(level, new double[]{
                CombatSimulator.calculateExpectedDefensiveDamage(level, 50, player),
                validHits > 0 ? totalDamage / validHits : 0});
    }

    void logDamageValidation() {
        testLog.logSection("DAMAGE VALIDATION");
        Map<SkillType, Map<Integer, double[]>> offensiveData = buildOffensiveDamageData();
        Map<Integer, double[]> defensiveData = buildDefensiveDamageData();
        report.setOffensiveDamageData(offensiveData);
        report.setDefensiveDamageData(defensiveData);

        testLog.log("OFFENSIVE (Player -> Elite):");
        player.sendMessage("§6=== DAMAGE VALIDATION ===");
        player.sendMessage("§eOFFENSIVE (Player -> Elite):");
        for (SkillType type : SkillType.values()) logOffensiveType(type, offensiveData.get(type));

        if (!defensiveData.isEmpty()) {
            testLog.log("DEFENSIVE (Elite -> Player):");
            player.sendMessage("§eDEFENSIVE (Elite -> Player):");
            StringBuilder summary = new StringBuilder("  §7Armor:");
            testLog.log("  Armor:");
            for (int level : SkillSystemTest.TEST_LEVELS) {
                double[] values = defensiveData.get(level);
                if (values == null) continue;
                double percentOff = values[0] != 0 ? ((values[1] - values[0]) / values[0]) * 100 : 0;
                String line = String.format("    Lv.%d: Expected: %.1f  Actual: %.1f  (%+.1f%%)",
                        level, values[0], values[1], percentOff);
                if (values[2] > 0) line += String.format(" | With skills: %.1f", values[2]);
                testLog.log(line);
            }
            appendLevel50Summary(summary, defensiveData.get(50));
            player.sendMessage(summary.toString());
        }
    }

    private int killStacksAt(String skillId, int level) {
        return killStacksObserved.getOrDefault(skillId, Map.of()).getOrDefault(level, 0);
    }

    private boolean verifyAttributeEffect(SkillBonus skill) {
        return switch (skill.getSkillId()) {
            case SwiftStrikesSkill.SKILL_ID -> player.getWalkSpeed() > 0.2f;
            case PoiseSkill.SKILL_ID -> hasModifier(Attribute.KNOCKBACK_RESISTANCE, PoiseSkill.MODIFIER_KEY_STRING);
            case FlurrySkill.SKILL_ID -> skill.getProcCount(player) > 0
                    || hasModifier(Attribute.ATTACK_SPEED, FlurrySkill.MODIFIER_KEY_STRING);
            case GrimReachSkill.SKILL_ID -> hasEntityInteractionRangeModifier(GrimReachSkill.MODIFIER_KEY_STRING);
            case LongReachSkill.SKILL_ID -> hasEntityInteractionRangeModifier(LongReachSkill.MODIFIER_KEY_STRING);
            case PolearmMasterySkill.SKILL_ID -> hasModifier(Attribute.ATTACK_SPEED, PolearmMasterySkill.MODIFIER_KEY_STRING);
            default -> skill.getProcCount(player) > 0;
        };
    }

    private boolean hasEntityInteractionRangeModifier(String key) {
        try {
            return hasModifier(Attribute.ENTITY_INTERACTION_RANGE, key);
        } catch (NoSuchFieldError error) {
            return true;
        }
    }

    private boolean hasModifier(Attribute attribute, String modifierKey) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return false;
        NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, modifierKey);
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (modifier.getKey().equals(key)) return true;
        }
        return false;
    }

    private String buildVerificationTag(SkillBonus skill) {
        if (SkillSystemTest.KILL_REQUIRED_SKILLS.contains(skill.getSkillId())) {
            int peakStacks = killStacksObserved.getOrDefault(skill.getSkillId(), Map.of())
                    .values().stream().mapToInt(Integer::intValue).max().orElse(0);
            int maxStacks = skill instanceof StackingSkill stackingSkill ? stackingSkill.getMaxStacks() : 0;
            return String.format("(KILL %d kills, peak %d/%d stacks)",
                    killsLanded.getOrDefault(skill.getSkillId(), 0), peakStacks, maxStacks);
        }
        Integer procs = totalProcCounts.get(skill.getSkillId());
        Integer hits = totalHitCounts.get(skill.getSkillId());
        return switch (skill.getTestStrategy()) {
            case ATTRIBUTE_CHECK -> "(ATTR OK)";
            case CONDITION_SETUP -> procs != null && hits != null && hits > 0
                    ? String.format("(COND %d/%d %.0f%%)", procs, hits, (double) procs / hits * 100)
                    : "(COND OK)";
            default -> {
                if (procs == null || hits == null || hits <= 0) yield "(unlock Lv." + skill.getRequiredLevel() + ")";
                String expected = skill instanceof ProcSkill procSkill
                        ? String.format("/%.0f%%", procSkill.getProcChance(50) * 100) : "";
                yield String.format("(PROC %.0f%%%s)", (double) procs / hits * 100, expected);
            }
        };
    }

    private void respawnIfNeeded(String dummyId, List<SkillBonus> skills) {
        if (!simulator.hasDummy(dummyId) && skills != null && !skills.isEmpty()) {
            simulator.respawnIfDead(dummyId, skills.get(0));
        }
    }

    private double performAttack(SkillType type, String dummyId) {
        return switch (type) {
            case BOWS, CROSSBOWS -> simulator.simulateRangedAttack(dummyId);
            case TRIDENTS -> simulator.simulateTridentAttack(dummyId);
            case ARMOR -> simulator.simulateIncomingDamage(dummyId, 10.0);
            case MACES -> simulator.simulateDirectAttack(dummyId);
            default -> simulator.simulateMeleeAttack(dummyId);
        };
    }

    private Map<SkillType, Map<Integer, double[]>> buildOffensiveDamageData() {
        Map<SkillType, Map<Integer, double[]>> data = new HashMap<>();
        for (var typeEntry : offensiveBaseline.entrySet()) {
            Map<Integer, double[]> levels = new HashMap<>();
            for (var levelEntry : typeEntry.getValue().entrySet()) {
                int level = levelEntry.getKey();
                Double total = offensiveSkillDamage.getOrDefault(typeEntry.getKey(), Map.of()).get(level);
                Integer count = offensiveSkillHits.getOrDefault(typeEntry.getKey(), Map.of()).get(level);
                double average = total != null && count != null && count > 0 ? total / count : 0;
                levels.put(level, new double[]{levelEntry.getValue()[0], levelEntry.getValue()[1], average});
            }
            data.put(typeEntry.getKey(), levels);
        }
        return data;
    }

    private Map<Integer, double[]> buildDefensiveDamageData() {
        Map<Integer, double[]> data = new HashMap<>();
        for (var entry : defensiveBaseline.entrySet()) {
            Double total = defensiveSkillDamage.get(entry.getKey());
            Integer count = defensiveSkillHits.get(entry.getKey());
            double average = total != null && count != null && count > 0 ? total / count : 0;
            data.put(entry.getKey(), new double[]{entry.getValue()[0], entry.getValue()[1], average});
        }
        return data;
    }

    private void logOffensiveType(SkillType type, Map<Integer, double[]> data) {
        if (data == null || data.isEmpty()) return;
        StringBuilder summary = new StringBuilder("  §7").append(type.getDisplayName()).append(":");
        testLog.log("  " + type.getDisplayName() + ":");
        for (int level : SkillSystemTest.TEST_LEVELS) {
            double[] values = data.get(level);
            if (values == null) continue;
            String line;
            if (values[0] > 0) {
                double percentOff = ((values[1] - values[0]) / values[0]) * 100;
                line = String.format("    Lv.%d: Expected: %.1f  Actual: %.1f  (%+.1f%%)",
                        level, values[0], values[1], percentOff);
            } else line = String.format("    Lv.%d: Actual: %.1f (no formula expected)", level, values[1]);
            if (values[2] > 0) line += String.format(" | With skills: %.1f", values[2]);
            testLog.log(line);
        }
        appendLevel50Summary(summary, data.get(50));
        player.sendMessage(summary.toString());
    }

    private static void appendLevel50Summary(StringBuilder summary, double[] values) {
        if (values == null) return;
        if (values[0] > 0) {
            double percentOff = ((values[1] - values[0]) / values[0]) * 100;
            summary.append(String.format(" Baseline @Lv50: %.1f/%.1f (%+.1f%%)", values[1], values[0], percentOff));
        } else summary.append(String.format(" Baseline @Lv50: %.1f", values[1]));
        if (values[2] > 0 && values[1] > 0) {
            summary.append(String.format(" | Skills: %.1f (%.2fx)", values[2], values[2] / values[1]));
        }
    }
}
