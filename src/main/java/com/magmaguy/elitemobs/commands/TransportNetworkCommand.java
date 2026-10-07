package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.transport.TransportModule;
import com.magmaguy.magmacore.command.*;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import java.util.List;

/** {@code /em transport <network|node> <action> [arguments...]}. The group literal keeps these apart from route commands. */
public final class TransportNetworkCommand extends AdvancedCommand {
    private final String action;
    public TransportNetworkCommand(String group, String action) {
        super(List.of("transport"));
        this.action = action;
        addLiteral(group);
        addLiteral(action);
        addOptionalVarargsArgument("args", new ListStringCommandArgument("<arguments>") {
            @Override public boolean matchesInput(String input) { return input != null && !input.isBlank(); }
        });
        setPermission("elitemobs.transport.admin");
        setSenderType(SenderType.PLAYER);
        setUsage("/em transport " + group + " " + action + " <arguments>");
        setDescription("Authors and tests flight networks.");
    }
    @Override public void execute(CommandData data) {
        TransportModule module = TransportModule.get();
        if (module == null) { data.getPlayerSender().sendMessage("Transport is unavailable."); return; }
        // Command args are [transport, group, action, ...]; the sequence starts after the action literal.
        String raw = data.getStringSequenceArgument("args");
        module.networkEditor().execute(data.getPlayerSender(), action,
                raw == null || raw.isBlank() ? new String[0] : raw.trim().split("\\s+"));
    }
}
