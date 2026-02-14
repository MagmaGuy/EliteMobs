package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.skills.SkillType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores test results for a single skill.
 * <p>
 * In batch testing mode, the primary fields used are:
 * <ul>
 *   <li>{@link #skillId}, {@link #skillName}, {@link #skillType}, {@link #testLevel} - Identification</li>
 *   <li>{@link #totalHits} - Number of attacks performed</li>
 *   <li>{@link #issues} - List of failure reasons (empty if passed)</li>
 *   <li>{@link #passed} - Whether the skill passed all level checks</li>
 * </ul>
 * <p>
 * Additional fields (procCount, expectedProcRate, damageMultiplier, etc.) are available
 * for more detailed damage-based analysis if needed.
 *
 * @see TestReport - Aggregates multiple SkillTestResult objects
 */
@Getter
public class SkillTestResult {
    private final String skillId;
    private final String skillName;
    private final SkillType skillType;
    private final int testLevel;

    // Proc testing
    private int totalHits = 0;
    private int procCount = 0;
    private double expectedProcRate = 0;

    // Damage testing
    private double totalDamageDealt = 0;
    private double baseDamageDealt = 0;
    private double skillBonusDamage = 0;
    private int hitsToKill = 0;

    // Expected values
    private double expectedDamageMultiplier = 1.0;
    private double actualDamageMultiplier = 1.0;

    // Status
    private boolean passed = true;
    private boolean skipped = false;
    private String verificationTag = null;
    private final List<String> issues = new ArrayList<>();
    private final List<String> logs = new ArrayList<>();

    public SkillTestResult(String skillId, String skillName, SkillType skillType, int testLevel) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.skillType = skillType;
        this.testLevel = testLevel;
    }

    public void setExpectedProcRate(double rate) {
        this.expectedProcRate = rate;
    }

    public void setTotalHits(int hits) {
        this.totalHits = hits;
    }

    public void setProcCount(int count) {
        this.procCount = count;
    }

    public void setExpectedDamageMultiplier(double multiplier) {
        this.expectedDamageMultiplier = multiplier;
    }

    public void setHitsToKill(int hits) {
        this.hitsToKill = hits;
    }

    public double getActualProcRate() {
        return totalHits > 0 ? (double) procCount / totalHits : 0;
    }

    public void calculateActualDamageMultiplier() {
        if (baseDamageDealt > 0) {
            actualDamageMultiplier = totalDamageDealt / baseDamageDealt;
        }
    }

    public void addLog(String message) {
        logs.add(message);
    }

    public void addIssue(String issue) {
        issues.add(issue);
        passed = false;
    }

    public void markSkipped(String reason) {
        this.skipped = true;
        this.passed = true; // Skipped skills don't count as failures
        issues.add(reason);
    }

    public void setVerificationTag(String tag) {
        this.verificationTag = tag;
    }

    public void validate() {
        // Validate proc rate (within 15% tolerance for statistical variance)
        if (expectedProcRate > 0 && totalHits >= 100) {
            double actualRate = getActualProcRate();
            double tolerance = expectedProcRate * 0.15;
            if (Math.abs(actualRate - expectedProcRate) > tolerance) {
                addIssue(String.format("Proc rate mismatch: expected %.1f%%, got %.1f%% (tolerance: %.1f%%)",
                        expectedProcRate * 100, actualRate * 100, tolerance * 100));
            }
        }

        // Validate damage multiplier (within 5% tolerance)
        if (expectedDamageMultiplier != 1.0 && totalHits > 0) {
            calculateActualDamageMultiplier();
            double tolerance = expectedDamageMultiplier * 0.05;
            if (Math.abs(actualDamageMultiplier - expectedDamageMultiplier) > tolerance) {
                addIssue(String.format("Damage multiplier mismatch: expected %.2fx, got %.2fx",
                        expectedDamageMultiplier, actualDamageMultiplier));
            }
        }
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(passed ? "&a✓ " : "&c✗ ");
        sb.append("&f").append(skillName).append(" &7(Lv.").append(testLevel).append(")");

        if (expectedProcRate > 0) {
            sb.append(String.format(" &7| Proc: &e%.1f%%&7/&e%.1f%%",
                    getActualProcRate() * 100, expectedProcRate * 100));
        }

        if (expectedDamageMultiplier != 1.0) {
            sb.append(String.format(" &7| Dmg: &e%.2fx&7/&e%.2fx",
                    actualDamageMultiplier, expectedDamageMultiplier));
        }

        if (!passed) {
            for (String issue : issues) {
                sb.append("\n  &c→ ").append(issue);
            }
        }

        return sb.toString();
    }
}
