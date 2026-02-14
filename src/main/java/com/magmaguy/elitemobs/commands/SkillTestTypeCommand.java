package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.testing.SkillSystemTest;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Command to start automated combat system testing for a specific weapon type.
 * Usage: /em debug combat test <type>
 */
public class SkillTestTypeCommand extends AdvancedCommand {

    public SkillTestTypeCommand() {
        super(List.of("debug"));
        addLiteral("combat");
        addLiteral("test");
        addArgument("type", new ListStringCommandArgument(
                Arrays.stream(SkillType.values()).map(t -> t.name().toLowerCase()).toList(),
                "<type>"));
        setUsage("/em debug combat test <type>");
        setPermission("elitemobs.admin");
        setDescription("Tests skills for a specific weapon type (e.g., swords, bows).");
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

        String typeArg = commandData.getStringArgument("type");
        SkillType filterType;
        try {
            filterType = SkillType.valueOf(typeArg.toUpperCase());
        } catch (IllegalArgumentException e) {
            Logger.sendMessage(player, "§cUnknown weapon type: §e" + typeArg);
            Logger.sendMessage(player, "§7Valid types: §f" + String.join(", ",
                    Arrays.stream(SkillType.values()).map(t -> t.name().toLowerCase()).toArray(String[]::new)));
            return;
        }

        Logger.sendMessage(player, CommandMessagesConfig.getSkillTestStartMessage());
        Logger.sendMessage(player, "§7Testing §e" + filterType.getDisplayName() + "§7 skills only.");

        SkillSystemTest test = new SkillSystemTest(player, filterType);
        test.start();
    }
}
