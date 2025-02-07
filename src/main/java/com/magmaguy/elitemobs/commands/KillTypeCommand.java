package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.KillHandler;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.EntityTypeCommandArgument;
import org.bukkit.entity.EntityType;

import java.util.List;

public class KillTypeCommand extends AdvancedCommand {
    public KillTypeCommand() {
        super(List.of("kill"));
        addLiteral("type");
        addArgument("type", new EntityTypeCommandArgument());
        setUsage("/em kill type <entityType>");
        setPermission("elitemobs.kill.command");
        setDescription("Kills all elites of the specified type.");
    }

    @Override
    public void execute(CommandData commandData) {
        KillHandler.killEntityType(commandData.getCommandSender(), EntityType.valueOf(commandData.getStringArgument("type")));
    }
}