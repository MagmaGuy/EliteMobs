package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.DynamicListStringCommandArgument;

import java.util.List;

public final class ExperimentalClassInfoCommand extends AdvancedCommand {
    public ExperimentalClassInfoCommand() {
        super(List.of("class"));
        addLiteral("info");
        addArgument("class", new DynamicListStringCommandArgument(
                () -> ExperimentalClassCommandSupport.catalog().forms().stream()
                        .map(ClassFormDefinition::id).toList(),
                "<class>"));
        setUsage("/em class info <class>");
        setDescription("Explains one Experimental Combat class form.");
        setPermission("elitemobs.command");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        ExperimentalClassCommandSupport.showForm(
                commandData.getPlayerSender(), commandData.getStringArgument("class"));
    }
}
