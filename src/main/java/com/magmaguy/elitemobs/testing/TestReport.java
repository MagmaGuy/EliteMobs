package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.skills.SkillType;
import lombok.Getter;

import java.util.*;

/**
 * Aggregates test results and generates formatted reports.
 * <p>
 * Collects {@link SkillTestResult} objects from the test run and provides:
 * <ul>
 *   <li>{@link #generateSummary()} - Concise pass/fail summary for in-game display</li>
 *   <li>{@link #generateDetailedReport()} - Verbose report with per-skill details</li>
 *   <li>{@link #exportToText()} - Plain text export for file logging</li>
 * </ul>
 *
 * @see SkillSystemTest - The main test orchestrator that populates this report
 */
@Getter
public class TestReport {

    private final UUID playerUUID;
    private final long startTime;
    private long endTime;

    private final List<SkillTestResult> results = new ArrayList<>();
    private final Map<SkillType, List<SkillTestResult>> resultsByType = new HashMap<>();

    // Summary stats
    private int totalTests = 0;
    private int passedTests = 0;
    private int failedTests = 0;
    private int warningTests = 0;

    public TestReport(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.startTime = System.currentTimeMillis();
    }

    public void addResult(SkillTestResult result) {
        results.add(result);
        resultsByType.computeIfAbsent(result.getSkillType(), k -> new ArrayList<>()).add(result);

        totalTests++;
        if (result.isPassed()) {
            passedTests++;
        } else if (result.getIssues().stream().anyMatch(s -> s.contains("mismatch"))) {
            failedTests++;
        } else {
            warningTests++;
        }
    }

    public List<String> generateSummary() {
        endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;

        List<String> lines = new ArrayList<>();

        lines.add("");
        lines.add("&6&l========== SKILL SYSTEM TEST REPORT ==========");
        lines.add("");
        lines.add(String.format("&7Duration: &f%d seconds", duration));
        lines.add(String.format("&7Total Tests: &f%d", totalTests));
        lines.add(String.format("&aPassed: &f%d &7| &cFailed: &f%d &7| &eWarnings: &f%d",
                passedTests, failedTests, warningTests));
        lines.add("");

        // Results by skill type
        for (SkillType type : SkillType.values()) {
            List<SkillTestResult> typeResults = resultsByType.get(type);
            if (typeResults == null || typeResults.isEmpty()) continue;

            long passed = typeResults.stream().filter(SkillTestResult::isPassed).count();
            long failed = typeResults.size() - passed;

            String statusColor = failed == 0 ? "&a" : "&c";
            lines.add(String.format("&e%s: %s%d/%d passed",
                    type.getDisplayName(), statusColor, passed, typeResults.size()));

            // Show failures
            for (SkillTestResult result : typeResults) {
                if (!result.isPassed()) {
                    lines.add("  &c- " + result.getSkillName() + " (Lv." + result.getTestLevel() + ")");
                    for (String issue : result.getIssues()) {
                        lines.add("    &7" + issue);
                    }
                }
            }
        }

        lines.add("");

        // Overall status
        if (failedTests == 0) {
            lines.add("&a&lALL TESTS PASSED!");
        } else {
            lines.add(String.format("&c&l%d TEST(S) FAILED - Review issues above", failedTests));
        }

        lines.add("&6&l================================================");

        return lines;
    }

    public List<String> generateDetailedReport() {
        List<String> lines = new ArrayList<>();

        lines.add("&6&l========== DETAILED TEST REPORT ==========");
        lines.add("");

        for (SkillType type : SkillType.values()) {
            List<SkillTestResult> typeResults = resultsByType.get(type);
            if (typeResults == null || typeResults.isEmpty()) continue;

            lines.add("&e&l" + type.getDisplayName());
            lines.add("&7" + "-".repeat(40));

            for (SkillTestResult result : typeResults) {
                lines.add(result.getSummary());

                // Add detailed logs
                for (String log : result.getLogs()) {
                    lines.add("  &7" + log);
                }
            }

            lines.add("");
        }

        return lines;
    }

    /**
     * Exports the report to a file-friendly format.
     */
    public String exportToText() {
        StringBuilder sb = new StringBuilder();
        sb.append("ELITEMOBS SKILL SYSTEM TEST REPORT\n");
        sb.append("==================================\n\n");
        sb.append("Generated: ").append(new Date()).append("\n");
        sb.append("Duration: ").append((endTime - startTime) / 1000).append(" seconds\n\n");

        sb.append("SUMMARY\n");
        sb.append("-------\n");
        sb.append("Total: ").append(totalTests).append("\n");
        sb.append("Passed: ").append(passedTests).append("\n");
        sb.append("Failed: ").append(failedTests).append("\n");
        sb.append("Warnings: ").append(warningTests).append("\n\n");

        sb.append("DETAILED RESULTS\n");
        sb.append("----------------\n\n");

        for (SkillType type : SkillType.values()) {
            List<SkillTestResult> typeResults = resultsByType.get(type);
            if (typeResults == null || typeResults.isEmpty()) continue;

            sb.append(type.getDisplayName().toUpperCase()).append("\n");
            sb.append("-".repeat(type.getDisplayName().length())).append("\n");

            for (SkillTestResult result : typeResults) {
                sb.append(String.format("  [%s] %s (Lv.%d)\n",
                        result.isPassed() ? "PASS" : "FAIL",
                        result.getSkillName(),
                        result.getTestLevel()));

                if (result.getExpectedProcRate() > 0) {
                    sb.append(String.format("    Proc Rate: %.1f%% (expected %.1f%%)\n",
                            result.getActualProcRate() * 100,
                            result.getExpectedProcRate() * 100));
                }

                if (result.getExpectedDamageMultiplier() != 1.0) {
                    sb.append(String.format("    Damage Mult: %.2fx (expected %.2fx)\n",
                            result.getActualDamageMultiplier(),
                            result.getExpectedDamageMultiplier()));
                }

                sb.append(String.format("    Hits: %d | Total Damage: %.1f | Skill Bonus: %.1f\n",
                        result.getTotalHits(),
                        result.getTotalDamageDealt(),
                        result.getSkillBonusDamage()));

                if (!result.isPassed()) {
                    for (String issue : result.getIssues()) {
                        sb.append("    ! ").append(issue).append("\n");
                    }
                }

                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
