package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.transport.TransportModule;
import com.magmaguy.magmacore.command.*;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import java.util.List;

public final class TransportCommand extends AdvancedCommand {
    private final String action;
    private final boolean argument;
    public TransportCommand(String action) {
        super(List.of("transport"));
        this.action = action;
        addLiteral(action);
        argument = List.of("create", "edit", "start", "move").contains(action);
        if (argument) addArgument("value", new ListStringCommandArgument(action.equals("move") ? "<waypoint number>" : "<route id>") {
            @Override public boolean matchesInput(String input) { return input != null && !input.isBlank(); }
        });
        setPermission("elitemobs.transport.admin");
        setSenderType(SenderType.PLAYER);
        setUsage("/em transport " + action + (argument ? " <value>" : ""));
        setDescription("Authors, previews and tests curved transport routes.");
    }
    @Override public void execute(CommandData data) {
        TransportModule module = TransportModule.get();
        if (module == null) { data.getPlayerSender().sendMessage("Transport is unavailable."); return; }
        module.editor().execute(data.getPlayerSender(), action, argument ? data.getStringArgument("value") : null);
    }
}
