package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;

import java.util.List;

public class DungeonResetLockoutCommand extends AdvancedCommand {
    public DungeonResetLockoutCommand() {
        super(List.of("dungeon"));
        addLiteral("resetlockout");
        addArgument("player", new PlayerCommandArgument());
        setUsage("/em dungeon resetlockout <player>");
        setPermission("elitemobs.dungeon.resetlockout");
        setDescription("Clears all dungeon boss lockouts for a specific player.");
    }

    @Override
    public void execute(CommandData commandData) {
        DungeonCommands.resetLockout(commandData.getCommandSender(), commandData.getStringArgument("player"));
    }
}
