package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.KillHandler;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class KillTypeCommand extends AdvancedCommand {
    public KillTypeCommand() {
        super(List.of("kill"));
        addLiteral("type");
        addArgument("type", new ArrayList<>());
        setUsage("/em kill type <entityType>");
        setPermission("elitemobs.kill.type");
        setSenderType(SenderType.PLAYER);
        setDescription("Kills all elites of the specified type.");
    }

    @Override
    public void execute() {
        KillHandler.killEntityType(getCurrentPlayerSender(), EntityType.valueOf(getStringArgument("type")));
    }
}