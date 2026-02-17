package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.testing.SkillSystemTest;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command to cancel an active combat system test.
 * Usage: /em debug combat cancel
 */
public class SkillTestCancelCommand extends AdvancedCommand {

    public SkillTestCancelCommand() {
        super(List.of("debug"));
        addLiteral("combat");
        addLiteral("cancel");
        setUsage("/em debug combat cancel");
        setPermission("elitemobs.admin");
        setDescription("Cancels an active combat system test.");
    }

    @Override
    public void execute(CommandData commandData) {
        if (!(commandData.getCommandSender() instanceof Player player)) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSkillTestOnlyPlayerMessage());
            return;
        }

        SkillSystemTest session = SkillSystemTest.getSession(player.getUniqueId());
        if (session == null) {
            Logger.sendMessage(player, CommandMessagesConfig.getSkillTestCancelNoTestMessage());
            return;
        }

        session.cancel();
        Logger.sendMessage(player, CommandMessagesConfig.getSkillTestCancelSuccessMessage());
    }
}
