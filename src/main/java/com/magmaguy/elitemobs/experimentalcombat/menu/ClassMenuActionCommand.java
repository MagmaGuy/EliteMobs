package com.magmaguy.elitemobs.experimentalcombat.menu;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

/** Internal transport for opaque actions emitted into modern dialog JSON. */
final class ClassMenuActionCommand extends AdvancedCommand {
    private final ClassMenuCoordinator coordinator;

    ClassMenuActionCommand(ClassMenuCoordinator coordinator) {
        super(List.of("_classmenu"));
        this.coordinator = coordinator;
        addArgument("token", new ListStringCommandArgument("<menu-action>"));
        setUsage("/em _classmenu <menu-action>");
        setDescription("Handles a short-lived Experimental Combat class-menu action.");
        setPermission("elitemobs.command");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        coordinator.dispatch(commandData.getPlayerSender(), commandData.getStringArgument("token"));
    }
}
