package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import org.bukkit.entity.Player;

import java.util.List;

/** Toggles class controls outside EliteMobs content; the command form of sneak + double-tap F, which Bedrock lacks. */
public final class AdvancedClassControlsCommand extends AdvancedCommand {
    public AdvancedClassControlsCommand() {
        super(List.of("class"));
        addLiteral("controls");
        setUsage("/em class controls");
        setDescription("Toggles [Alpha] Advanced Combat System class controls outside EliteMobs content.");
        setPermission("elitemobs.command");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        Player player = commandData.getPlayerSender();
        if (!AdvancedClassCommandSupport.requireModule(player)) return;
        AdvancedCombatModule.get().toggleOutsideControls(player);
    }
}
