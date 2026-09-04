package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.pathfinding.patrol.PatrolEditor;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;
import java.util.Locale;

public final class PatrolCommand extends AdvancedCommand {
    private final String action;

    public PatrolCommand(String action) {
        super(List.of("patrol"));
        this.action = action.toLowerCase(Locale.ROOT);
        addLiteral(this.action);
        if (this.action.equals("mode"))
            addArgument("mode", new ListStringCommandArgument(List.of("LOOP", "REVERSE"), "<LOOP|REVERSE>"));
        setUsage("/em patrol " + this.action + (this.action.equals("mode") ? " <LOOP|REVERSE>" : ""));
        setPermission("elitemobs.patrol.admin");
        setSenderType(SenderType.PLAYER);
        setDescription("Authors and inspects native mob patrol routes.");
    }

    @Override
    public void execute(CommandData commandData) {
        switch (action) {
            case "edit" -> PatrolEditor.edit(commandData.getPlayerSender());
            case "add" -> PatrolEditor.add(commandData.getPlayerSender());
            case "remove" -> PatrolEditor.remove(commandData.getPlayerSender());
            case "undo" -> PatrolEditor.undo(commandData.getPlayerSender());
            case "mode" -> PatrolEditor.mode(commandData.getPlayerSender(), commandData.getStringArgument("mode"));
            case "save" -> PatrolEditor.save(commandData.getPlayerSender());
            case "cancel" -> PatrolEditor.cancel(commandData.getPlayerSender());
            case "status" -> PatrolEditor.status(commandData.getPlayerSender());
            default -> throw new IllegalStateException("Unknown patrol action " + action);
        }
    }
}
