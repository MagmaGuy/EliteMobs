package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.DynamicListStringCommandArgument;

import java.util.List;

public final class AdvancedClassInfoCommand extends AdvancedCommand {
    public AdvancedClassInfoCommand() {
        super(List.of("class"));
        addLiteral("info");
        addArgument("class", new DynamicListStringCommandArgument(
                () -> AdvancedClassCommandSupport.catalog().forms().stream()
                        .map(ClassFormDefinition::id).toList(),
                "<class>"));
        setUsage("/em class info <class>");
        setDescription("Explains one [Alpha] Advanced Combat System class form.");
        setPermission("elitemobs.command");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        AdvancedClassCommandSupport.showForm(
                commandData.getPlayerSender(), commandData.getStringArgument("class"));
    }
}
