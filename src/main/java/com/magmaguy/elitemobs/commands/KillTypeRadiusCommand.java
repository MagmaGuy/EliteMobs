package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.KillHandler;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.EntityTypeCommandArgument;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import org.bukkit.entity.EntityType;

import java.util.List;

public class KillTypeRadiusCommand extends AdvancedCommand {
    public KillTypeRadiusCommand() {
        super(List.of("kill"));
        addLiteral("type");
        addArgument("type", new EntityTypeCommandArgument());
        addArgument("range", new IntegerCommandArgument("<range>"));
        setUsage("/em kill type <entityType> <radius>");
        setPermission("elitemobs.kill.command");
        setSenderType(SenderType.PLAYER);
        setDescription("Kills all elites of the specified type within the specified radius.");
    }

    @Override
    public void execute(CommandData commandData) {
        KillHandler.radiusKillSpecificMobs(commandData.getPlayerSender(),
                EntityType.valueOf(commandData.getStringArgument("type")),
                commandData.getIntegerArgument("range"));
    }
}