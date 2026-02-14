package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.skills.SkillType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

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

    // Store last test report per player for review with /em debug combat results
    private static final Map<UUID, TestReport> lastReports = new HashMap<>();

    private final UUID playerUUID;
    private final long startTime;
    private long endTime;

    private final List<SkillTestResult> results = new ArrayList<>();
    private final Map<SkillType, List<SkillTestResult>> resultsByType = new HashMap<>();
    private final List<SkillTestResult> skippedResults = new ArrayList<>();

    // Summary stats
    private int totalTests = 0;
    private int passedTests = 0;
    private int failedTests = 0;
    private int skippedTests = 0;

    // Damage validation data
    // Offensive: {type -> {level -> [expected, baseline, avgWithSkills]}}
    @Setter private Map<SkillType, Map<Integer, double[]>> offensiveDamageData;
    // Defensive: {level -> [expected, baseline, avgWithSkills]}
    @Setter private Map<Integer, double[]> defensiveDamageData;

    public TestReport(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Gets the last stored test report for a player.
     */
    public static TestReport getLastReport(UUID playerUUID) {
        return lastReports.get(playerUUID);
    }

    /**
     * Stores this report as the last report for the player.
     */
    public void store() {
        lastReports.put(playerUUID, this);
    }

    public void addResult(SkillTestResult result) {
        results.add(result);
        resultsByType.computeIfAbsent(result.getSkillType(), k -> new ArrayList<>()).add(result);

        totalTests++;
        if (result.isSkipped()) {
            skippedTests++;
        } else if (result.isPassed()) {
            passedTests++;
        } else {
            failedTests++;
        }
    }

    /**
     * Adds a skipped skill result.
     */
    public void addSkippedResult(SkillTestResult result) {
        skippedResults.add(result);
        totalTests++;
        skippedTests++;
    }

    public List<String> generateSummary() {
        endTime = System.currentTimeMillis();
        double durationSec = (endTime - startTime) / 1000.0;

        List<String> lines = new ArrayList<>();

        int testedCount = passedTests + failedTests;
        lines.add("");
        lines.add(String.format("&6&l=== SKILL TEST COMPLETE (%.1fs) | %d skills | %d passed | %d failed | %d skipped ===",
                durationSec, totalTests, passedTests, failedTests, skippedTests));
        lines.add("");

        // Results by skill type - compact format with proc rates
        for (SkillType type : SkillType.values()) {
            List<SkillTestResult> typeResults = resultsByType.get(type);
            if (typeResults == null || typeResults.isEmpty()) continue;

            long passed = typeResults.stream().filter(r -> r.isPassed() && !r.isSkipped()).count();
            long skipped = typeResults.stream().filter(SkillTestResult::isSkipped).count();
            long total = typeResults.size() - skipped;

            String statusColor = (passed == total) ? "&a" : "&c";
            lines.add(String.format("&e%s (%s%d/%d&e)", type.getDisplayName(), statusColor, passed, total));

            // Show each skill with its verification result
            StringBuilder skillLine = new StringBuilder("  ");
            for (SkillTestResult result : typeResults) {
                if (result.isSkipped()) continue;
                if (result.isPassed()) {
                    skillLine.append("&a").append(result.getSkillName());
                    if (result.getVerificationTag() != null) {
                        skillLine.append("&7[").append(result.getVerificationTag()).append("]");
                    }
                } else {
                    skillLine.append("&c").append(result.getSkillName()).append("&7[FAILED]");
                }
                skillLine.append(" ");
            }
            lines.add(skillLine.toString().trim());

            // Show failure details
            for (SkillTestResult result : typeResults) {
                if (!result.isPassed() && !result.isSkipped()) {
                    for (String issue : result.getIssues()) {
                        lines.add("    &c- " + result.getSkillName() + ": " + issue);
                    }
                }
            }
        }

        // Show skipped skills
        if (!skippedResults.isEmpty()) {
            StringBuilder skippedLine = new StringBuilder("&7SKIPPED (").append(skippedResults.size()).append("): ");
            for (SkillTestResult result : skippedResults) {
                skippedLine.append(result.getSkillName());
                if (!result.getIssues().isEmpty()) {
                    skippedLine.append("(").append(result.getIssues().get(0)).append(")");
                }
                skippedLine.append(", ");
            }
            // Remove trailing ", "
            String skippedStr = skippedLine.toString();
            if (skippedStr.endsWith(", ")) skippedStr = skippedStr.substring(0, skippedStr.length() - 2);
            lines.add("");
            lines.add(skippedStr);
        }

        // Damage validation summary
        if (offensiveDamageData != null || defensiveDamageData != null) {
            lines.add("&6=== DAMAGE VALIDATION ===");

            if (offensiveDamageData != null) {
                lines.add("&eOFFENSIVE (Player -> Elite)");
                for (SkillType type : SkillType.values()) {
                    Map<Integer, double[]> data = offensiveDamageData.get(type);
                    if (data == null || data.isEmpty()) continue;
                    // Show condensed view at level 50
                    double[] lv50 = data.get(50);
                    if (lv50 == null) continue;
                    StringBuilder sb = new StringBuilder();
                    sb.append("  &7").append(type.getDisplayName()).append(": ");
                    if (lv50[0] > 0) {
                        double pctOff = ((lv50[1] - lv50[0]) / lv50[0]) * 100;
                        sb.append(String.format("Baseline @Lv50: %.1f/%.1f (%+.1f%%)", lv50[1], lv50[0], pctOff));
                    } else {
                        sb.append(String.format("Baseline @Lv50: %.1f", lv50[1]));
                    }
                    if (lv50[2] > 0 && lv50[1] > 0) {
                        sb.append(String.format(" | Skills: %.1f (%.2fx)", lv50[2], lv50[2] / lv50[1]));
                    }
                    lines.add(sb.toString());
                }
            }

            if (defensiveDamageData != null && !defensiveDamageData.isEmpty()) {
                lines.add("&eDEFENSIVE (Elite -> Player)");
                double[] lv50 = defensiveDamageData.get(50);
                if (lv50 != null) {
                    StringBuilder sb = new StringBuilder("  &7Armor: ");
                    double pctOff = lv50[0] != 0 ? ((lv50[1] - lv50[0]) / lv50[0]) * 100 : 0;
                    sb.append(String.format("Baseline @Lv50: %.1f/%.1f (%+.1f%%)", lv50[1], lv50[0], pctOff));
                    if (lv50[2] > 0 && lv50[1] > 0) {
                        sb.append(String.format(" | Skills: %.1f (%.2fx)", lv50[2], lv50[2] / lv50[1]));
                    }
                    lines.add(sb.toString());
                }
            }
        }

        lines.add("");

        // Overall status
        if (failedTests == 0) {
            lines.add("&a&lALL TESTS PASSED!" + (skippedTests > 0 ? " &7(" + skippedTests + " skipped)" : ""));
        } else {
            lines.add(String.format("&c&l%d TEST(S) FAILED &7- Use /em debug combat results for details", failedTests));
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
        sb.append("Skipped: ").append(skippedTests).append("\n\n");

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

        // Damage validation section
        if (offensiveDamageData != null || defensiveDamageData != null) {
            sb.append("\nDAMAGE VALIDATION\n");
            sb.append("=================\n\n");

            if (offensiveDamageData != null) {
                sb.append("OFFENSIVE (Player -> Elite)\n");
                sb.append("--------------------------\n");
                for (SkillType type : SkillType.values()) {
                    Map<Integer, double[]> data = offensiveDamageData.get(type);
                    if (data == null || data.isEmpty()) continue;

                    sb.append(type.getDisplayName()).append(":\n");
                    int[] levels = data.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
                    for (int level : levels) {
                        double[] vals = data.get(level);
                        if (vals[0] > 0) {
                            double pctOff = ((vals[1] - vals[0]) / vals[0]) * 100;
                            sb.append(String.format("  Lv.%d: Expected: %.1f   Actual: %.1f   (%+.1f%%)\n",
                                    level, vals[0], vals[1], pctOff));
                        } else {
                            sb.append(String.format("  Lv.%d: Actual: %.1f (no formula expected)\n",
                                    level, vals[1]));
                        }
                    }
                    // Show avg with skills at level 50
                    double[] lv50 = data.get(50);
                    if (lv50 != null && lv50[2] > 0 && lv50[1] > 0) {
                        sb.append(String.format("  Avg with skills @Lv50: %.1f/hit (%.2fx baseline)\n",
                                lv50[2], lv50[2] / lv50[1]));
                    }
                    sb.append("\n");
                }
            }

            if (defensiveDamageData != null && !defensiveDamageData.isEmpty()) {
                sb.append("DEFENSIVE (Elite -> Player)\n");
                sb.append("---------------------------\n");
                sb.append("Armor:\n");
                int[] levels = defensiveDamageData.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
                for (int level : levels) {
                    double[] vals = defensiveDamageData.get(level);
                    double pctOff = vals[0] != 0 ? ((vals[1] - vals[0]) / vals[0]) * 100 : 0;
                    sb.append(String.format("  Lv.%d: Expected: %.1f   Actual: %.1f   (%+.1f%%)\n",
                            level, vals[0], vals[1], pctOff));
                }
                double[] lv50 = defensiveDamageData.get(50);
                if (lv50 != null && lv50[2] > 0 && lv50[1] > 0) {
                    sb.append(String.format("  Avg with skills @Lv50: %.1f/hit (%.2fx baseline)\n",
                            lv50[2], lv50[2] / lv50[1]));
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Generates a written book summarizing the test results.
     * <p>
     * The book contains a cover page with overall stats, per-weapon-type pages
     * listing each skill's pass/fail status, and an issues page if any failures occurred.
     *
     * @return An {@link ItemStack} of {@link Material#WRITTEN_BOOK} with the report
     */
    public ItemStack generateBook() {
        if (endTime == 0) endTime = System.currentTimeMillis();

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        if (bookMeta == null) return book; // Fallback: return empty book
        bookMeta.setAuthor("EliteMobs");
        bookMeta.setTitle("Skill Test Report");

        int PAGE_LIMIT = 256;

        // --- Cover page ---
        double durationSec = (endTime - startTime) / 1000.0;
        StringBuilder cover = new StringBuilder();
        cover.append("\u00A76\u00A7lSkill Test Report\n\n");
        cover.append("\u00A70Duration: \u00A7e").append(String.format("%.1f", durationSec)).append("s\n\n");
        cover.append("\u00A70Skills tested: \u00A7e").append(totalTests).append("\n");
        cover.append("\u00A7aPassed: ").append(passedTests).append("\n");
        cover.append("\u00A7cFailed: ").append(failedTests).append("\n");
        cover.append("\u00A77Skipped: ").append(skippedTests).append("\n\n");
        if (failedTests == 0) {
            cover.append("\u00A7a\u00A7lALL PASSED");
        } else {
            cover.append("\u00A7c\u00A7l").append(failedTests).append(" FAILED");
        }
        bookMeta.addPage(cover.toString());

        // --- Per weapon type pages ---
        for (SkillType type : SkillType.values()) {
            List<SkillTestResult> typeResults = resultsByType.get(type);
            if (typeResults == null || typeResults.isEmpty()) continue;

            // Also gather any skipped results that match this type
            List<SkillTestResult> skippedForType = new ArrayList<>();
            for (SkillTestResult sr : skippedResults) {
                if (sr.getSkillType() == type) {
                    skippedForType.add(sr);
                }
            }

            StringBuilder page = new StringBuilder();
            page.append("\u00A76\u00A7l").append(type.getDisplayName()).append("\n\n");

            List<SkillTestResult> allForType = new ArrayList<>(typeResults);
            // Add skipped results that weren't already in typeResults
            for (SkillTestResult sr : skippedForType) {
                if (!allForType.contains(sr)) {
                    allForType.add(sr);
                }
            }

            for (SkillTestResult result : allForType) {
                StringBuilder entry = new StringBuilder();
                if (result.isSkipped()) {
                    entry.append("\u00A77[SKIP] ").append(result.getSkillName());
                } else if (result.isPassed()) {
                    entry.append("\u00A7a\u2713 ").append(result.getSkillName());
                    if (result.getVerificationTag() != null) {
                        entry.append(" \u00A77").append(result.getVerificationTag());
                    }
                } else {
                    entry.append("\u00A7c\u2717 ").append(result.getSkillName());
                    if (result.getVerificationTag() != null) {
                        entry.append(" \u00A77").append(result.getVerificationTag());
                    }
                }
                entry.append("\n");

                // Check if adding this entry would exceed the page limit
                if (page.length() + entry.length() > PAGE_LIMIT) {
                    bookMeta.addPage(page.toString());
                    page = new StringBuilder();
                    page.append("\u00A76\u00A7l").append(type.getDisplayName()).append(" (cont.)\n\n");
                }
                page.append(entry);
            }

            if (page.length() > 0) {
                bookMeta.addPage(page.toString());
            }
        }

        // --- Issues page (if any failures) ---
        if (failedTests > 0) {
            StringBuilder issuePage = new StringBuilder();
            issuePage.append("\u00A7c\u00A7lFailed Skills\n\n");

            for (SkillType type : SkillType.values()) {
                List<SkillTestResult> typeResults = resultsByType.get(type);
                if (typeResults == null) continue;

                for (SkillTestResult result : typeResults) {
                    if (result.isPassed() || result.isSkipped()) continue;

                    StringBuilder entry = new StringBuilder();
                    entry.append("\u00A7c").append(result.getSkillName());
                    if (!result.getIssues().isEmpty()) {
                        entry.append("\n\u00A77").append(result.getIssues().get(0));
                    }
                    entry.append("\n");

                    if (issuePage.length() + entry.length() > PAGE_LIMIT) {
                        bookMeta.addPage(issuePage.toString());
                        issuePage = new StringBuilder();
                        issuePage.append("\u00A7c\u00A7lFailed Skills (cont.)\n\n");
                    }
                    issuePage.append(entry);
                }
            }

            if (issuePage.length() > 0) {
                bookMeta.addPage(issuePage.toString());
            }
        }

        book.setItemMeta(bookMeta);
        return book;
    }
}
