package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Writes timestamped test logs to a file for debugging and analysis.
 * <p>
 * Logs are saved to {@code plugins/EliteMobs/logs/combat_tests/combat_test_[timestamp].log}
 * <p>
 * Provides structured logging methods:
 * <ul>
 *   <li>{@link #logSection} - Major section headers</li>
 *   <li>{@link #logSubsection} - Minor section headers</li>
 *   <li>{@link #log} - General messages with elapsed time prefix</li>
 *   <li>{@link #logError}, {@link #logWarning} - Tagged error/warning messages</li>
 *   <li>{@link #logResult} - Pass/fail result entries</li>
 * </ul>
 *
 * @see SkillSystemTest - Creates and populates the log during testing
 */
public class CombatTestLog {

    private final List<String> logEntries = new ArrayList<>();
    private final String sessionId;
    private final long startTime;

    public CombatTestLog() {
        this.startTime = System.currentTimeMillis();
        this.sessionId = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    }

    public void log(String message) {
        long elapsed = System.currentTimeMillis() - startTime;
        String timestamp = String.format("[%d.%03ds]", elapsed / 1000, elapsed % 1000);
        logEntries.add(timestamp + " " + message);
    }

    public void logSection(String title) {
        log("");
        log("=".repeat(60));
        log(title);
        log("=".repeat(60));
    }

    public void logSubsection(String title) {
        log("");
        log("--- " + title + " ---");
    }

    public void logDamageEvent(String skillName, int level, double baseDamage, double finalDamage,
                                boolean procced, boolean isCrit, String notes) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  [HIT] Base: %.2f -> Final: %.2f", baseDamage, finalDamage));
        if (procced) sb.append(" [PROC]");
        if (isCrit) sb.append(" [CRIT]");
        if (notes != null && !notes.isEmpty()) sb.append(" | ").append(notes);
        log(sb.toString());
    }

    public void logProcTest(String skillId, String skillName, int level,
                            double expectedProcRate, int totalHits, int procCount,
                            double avgBaseDamage, double avgProcDamage, double damageIncrease) {
        log(String.format("  Skill: %s (Lv.%d)", skillName, level));
        log(String.format("  Expected Proc Rate: %.1f%%", expectedProcRate * 100));
        log(String.format("  Actual Proc Rate: %.1f%% (%d/%d hits)",
                (totalHits > 0 ? (double) procCount / totalHits * 100 : 0), procCount, totalHits));
        log(String.format("  Base Damage (avg): %.2f", avgBaseDamage));
        log(String.format("  Proc Damage (avg): %.2f", avgProcDamage));
        log(String.format("  Damage Increase: +%.1f%%", damageIncrease * 100));
    }

    public void logBaseline(int level, double avgDamage, int hitsToKill, double dps) {
        log(String.format("  Level %d: Avg Damage = %.2f, Hits to Kill = %d, DPS = %.1f",
                level, avgDamage, hitsToKill, dps));
    }

    public void logError(String error) {
        log("[ERROR] " + error);
    }

    public void logWarning(String warning) {
        log("[WARN] " + warning);
    }

    public void logResult(String skillName, boolean passed, String details) {
        String status = passed ? "[PASS]" : "[FAIL]";
        log(String.format("  %s %s - %s", status, skillName, details));
    }

    /**
     * Saves the log to a file in the EliteMobs/logs/combat_tests/ directory.
     */
    public File saveToFile() {
        File logsDir = new File(MetadataHandler.PLUGIN.getDataFolder(), "logs/combat_tests");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }

        File logFile = new File(logsDir, "combat_test_" + sessionId + ".log");

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile))) {
            writer.println("ELITEMOBS COMBAT SYSTEM TEST LOG");
            writer.println("Session: " + sessionId);
            writer.println("Generated: " + new Date());
            writer.println();

            for (String entry : logEntries) {
                writer.println(entry);
            }

            writer.println();
            writer.println("=== END OF LOG ===");

            Logger.info("Combat test log saved to: " + logFile.getAbsolutePath());
            return logFile;
        } catch (IOException e) {
            Logger.warn("Failed to save combat test log: " + e.getMessage());
            return null;
        }
    }

    public List<String> getEntries() {
        return new ArrayList<>(logEntries);
    }
}
