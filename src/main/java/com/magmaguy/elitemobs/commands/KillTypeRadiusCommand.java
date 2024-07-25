package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.KillHandler;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class KillTypeRadiusCommand extends AdvancedCommand {
    public KillTypeRadiusCommand() {
        super(List.of("kill"));
        addLiteral("type");
        addArgument("type", new ArrayList<>());
        addArgument("range", new ArrayList<>());
        setUsage("/em kill type <radius>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Kills all elites of the specified type.");
    }

    @Override
    public void execute() {
        KillHandler.radiusKillSpecificMobs(getCurrentPlayerSender(),
                EntityType.valueOf(getStringArgument("type")),
                getIntegerArgument("range"));
    }
}