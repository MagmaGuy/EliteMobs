package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.transport.NetworkEditor;
import com.magmaguy.elitemobs.transport.TransportModule;
import com.magmaguy.magmacore.command.*;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/** {@code /em transport <network|node> <action> [arguments...]}. The group literal keeps these apart from route commands. */
public final class TransportNetworkCommand extends AdvancedCommand {
    private final String action;

    public TransportNetworkCommand(String group, String action) {
        super(List.of("transport"));
        this.action = action;
        addLiteral(group);
        addLiteral(action);
        // Every argument is optional so a missing one reaches the editor, which names it.
        // The declarations only drive suggestions and the usage line.
        switch (action) {
            case "edit" -> addOptionalArgument("network", suggesting("<network>", NetworkEditor::networkIds));
            case "preview", "generate", "ride", "link", "unlink" -> {
                addOptionalArgument("first", node());
                addOptionalArgument("second", node());
            }
            case "select", "move", "remove", "yaw" -> addOptionalArgument("node", node());
            case "add" -> {
                addOptionalArgument("id", text("<new node id>"));
                addOptionalVarargsArgument("neighbors", node());
            }
            case "destination" -> {
                addOptionalArgument("node", node());
                addOptionalVarargsArgument("name", text("<destination name>"));
            }
            case "hint" -> {
                addOptionalArgument("node", node());
                addOptionalVarargsArgument("hint", text("<locked hint>"));
            }
            case "permission" -> {
                addOptionalArgument("node", node());
                addOptionalArgument("permission", suggesting("<permission>", editor -> List.of("none")));
            }
            default -> { }
        }
        setPermission("elitemobs.transport.admin");
        setSenderType(SenderType.PLAYER);
        setUsage("/em transport " + group + " " + action + getArgumentsList().stream().skip(2)
                .map(argument -> " " + argument.hint()).collect(java.util.stream.Collectors.joining()));
        setDescription("Authors and tests flight networks.");
    }

    @Override public void execute(CommandData data) {
        TransportModule module = TransportModule.get();
        if (module == null) { data.getPlayerSender().sendMessage("Transport is unavailable."); return; }
        // Command args are [transport, group, action, ...]; the editor reads everything after the action literal.
        String[] args = data.getArgs();
        module.networkEditor().execute(data.getPlayerSender(), action, Arrays.stream(args).skip(3)
                .filter(token -> !token.isBlank()).toArray(String[]::new));
    }

    private static ListStringCommandArgument node() {
        return suggesting("<node>", null);
    }

    private static ListStringCommandArgument text(String hint) {
        return new ListStringCommandArgument(hint) {
            @Override public boolean matchesInput(String input) { return input != null && !input.isBlank(); }
        };
    }

    /** Suggests the given values, or the nodes of the sender's open draft when there are none. Any token is accepted. */
    private static ListStringCommandArgument suggesting(String hint, Function<NetworkEditor, List<String>> values) {
        return new ListStringCommandArgument(hint) {
            @Override public boolean matchesInput(String input) { return input != null && !input.isBlank(); }

            @Override public List<String> getSuggestions(CommandSender sender, String partialInput) {
                TransportModule module = TransportModule.get();
                if (module == null || !(sender instanceof Player player)) return List.of();
                List<String> candidates = values == null ? module.networkEditor().draftNodeIds(player) : values.apply(module.networkEditor());
                String typed = partialInput.toLowerCase(Locale.ROOT);
                List<String> matches = candidates.stream().filter(value -> value.startsWith(typed)).toList();
                return matches.isEmpty() && typed.isEmpty() ? List.of(hint) : matches;
            }
        };
    }
}
