package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.presentation.actionbar.CombatHudProbe;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/** Explicit per-player experiment; does not change inventory or the combat input bindings. */
public final class CombatHudProbeCommand extends AdvancedCommand {
    private final boolean show;

    public CombatHudProbeCommand(boolean show) {
        super(List.of("hudprobe"));
        this.show = show;
        addLiteral(show ? "show" : "off");
        addArgument("player", new PlayerCommandArgument());
        if (show) {
            addArgument("x", new IntegerCommandArgument("<x>"));
            addArgument("y", new IntegerCommandArgument("<y>"));
        }
        setPermission("elitemobs.admin");
        setUsage("/em hudprobe " + (show ? "show <player> <x> <y>" : "off <player>"));
        setDescription("Calibrates the experimental combat HUD bitmap. Requires the mods probe resource pack.");
    }

    @Override
    public void execute(CommandData data) {
        Player player = Bukkit.getPlayerExact(data.getStringArgument("player"));
        if (player == null) {
            data.getCommandSender().sendMessage("Player must be online.");
            return;
        }
        try {
            CombatHudProbe probe = show
                    ? new CombatHudProbe(data.getIntegerArgument("x"), data.getIntegerArgument("y")) : null;
            ActionBarCompositor.setHudProbe(player, probe);
            data.getCommandSender().sendMessage(show
                    ? "HUD probe enabled: 190x54 GUI pixels. Positive x moves right; positive y moves down. "
                      + "Wood/brass HUD with live vitals; skill cards follow the F ability window. Requires enabled skill controls. "
                      + "The probe ends on logout or /em hudprobe off " + player.getName()
                    : "HUD probe disabled for " + player.getName() + ".");
        } catch (IllegalArgumentException exception) {
            data.getCommandSender().sendMessage(exception.getMessage());
        }
    }
}
