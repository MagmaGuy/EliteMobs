package com.magmaguy.elitemobs.commands;

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
            Logger.sendMessage(commandData.getCommandSender(), "&cThis command can only be run by a player!");
            return;
        }

        if (SkillSystemTest.hasActiveSession(player.getUniqueId())) {
            Logger.sendMessage(player, "&cA test is already running! Use &e/em skill test cancel &cto stop it.");
            return;
        }

        Logger.sendMessage(player, "&a[Test] Starting skill system test...");
        Logger.sendMessage(player, "&7This will temporarily modify your skill levels and selections.");
        Logger.sendMessage(player, "&7Your original state will be restored after the test.");

        SkillSystemTest test = new SkillSystemTest(player);
        test.start();
    }
}
