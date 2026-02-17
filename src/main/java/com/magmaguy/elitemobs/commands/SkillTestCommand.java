package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.testing.SkillSystemTest;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command to start automated combat system testing.
 * Usage: /em debug combat start
 */
public class SkillTestCommand extends AdvancedCommand {

    public SkillTestCommand() {
        super(List.of("debug"));
        addLiteral("combat");
        addLiteral("start");
        setUsage("/em debug combat start");
        setPermission("elitemobs.admin");
        setDescription("Starts the automated combat system test.");
    }

    @Override
    public void execute(CommandData commandData) {
        if (!(commandData.getCommandSender() instanceof Player player)) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillTestOnlyPlayerMessage());
            return;
        }

        if (SkillSystemTest.hasActiveSession(player.getUniqueId())) {
            Logger.sendMessage(player, CommandMessagesConfig.getSkillTestAlreadyRunningMessage());
            return;
        }

        Logger.sendMessage(player, CommandMessagesConfig.getSkillTestStartMessage());
        Logger.sendMessage(player, CommandMessagesConfig.getSkillTestInfoMessage1());
        Logger.sendMessage(player, CommandMessagesConfig.getSkillTestInfoMessage2());

        SkillSystemTest test = new SkillSystemTest(player);
        test.start();
    }
}
