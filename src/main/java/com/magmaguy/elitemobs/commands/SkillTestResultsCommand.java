package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.testing.TestReport;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command to review the results of the last combat system test.
 * Usage: /em debug combat results
 */
public class SkillTestResultsCommand extends AdvancedCommand {

    public SkillTestResultsCommand() {
        super(List.of("debug"));
        addLiteral("combat");
        addLiteral("results");
        setUsage("/em debug combat results");
        setPermission("elitemobs.admin");
        setDescription("Shows the results of the last combat system test.");
    }

    @Override
    public void execute(CommandData commandData) {
        if (!(commandData.getCommandSender() instanceof Player player)) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillTestOnlyPlayerMessage());
            return;
        }

        TestReport report = TestReport.getLastReport(player.getUniqueId());
        if (report == null) {
            Logger.sendMessage(player, "§cNo test results found. Run §e/em debug combat start §cfirst.");
            return;
        }

        // Show summary
        List<String> summary = report.generateSummary();
        for (String line : summary) {
            player.sendMessage(line.replace("&", "§"));
        }

        // Show detailed report
        List<String> detailed = report.generateDetailedReport();
        for (String line : detailed) {
            player.sendMessage(line.replace("&", "§"));
        }
    }
}
