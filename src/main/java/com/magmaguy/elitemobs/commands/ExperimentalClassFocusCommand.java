package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatRuntime;
import com.magmaguy.elitemobs.experimentalcombat.input.ClassAbilityInputRouter;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import org.bukkit.entity.Player;

import java.util.List;

public final class ExperimentalClassFocusCommand extends AdvancedCommand {
    public ExperimentalClassFocusCommand() {
        super(List.of("class"));
        addLiteral("focus");
        setUsage("/em class focus");
        setDescription("Safely gives the universal Experimental Combat Focus item.");
        setPermission("elitemobs.command");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        Player player = commandData.getPlayerSender();
        if (!ExperimentalClassCommandSupport.requireModule(player)) return;
        if (!ExperimentalCombatRuntime.isActive(player)) {
            ExperimentalClassCommandSupport.send(player,
                    "&cThe Class Focus is only available inside Experimental Combat content.");
            return;
        }
        ClassAbilityInputRouter.FocusItemGiveResult result = ExperimentalCombatModule.get().giveFocusItem(player);
        switch (result.status()) {
            case GIVEN_TO_PREFERRED_SLOT -> ExperimentalClassCommandSupport.send(player,
                    "&aClass Focus placed in hotbar slot &f" + (result.slot() + 1) + "&a.");
            case GIVEN_TO_FALLBACK_SLOT -> ExperimentalClassCommandSupport.send(player,
                    "&eYour preferred slot was occupied; Class Focus was placed in inventory slot &f"
                            + (result.slot() + 1) + "&e without replacing anything.");
            case ALREADY_PRESENT -> ExperimentalClassCommandSupport.send(player,
                    result.slot() < 0
                            ? "&7You already have a Class Focus on your cursor."
                            : "&7You already have a Class Focus in inventory slot &f"
                            + (result.slot() + 1) + "&7.");
            case INVENTORY_FULL -> ExperimentalClassCommandSupport.send(player,
                    "&cYour inventory is full. Free a slot and run &f/em class focus&c again.");
            case INVALID_PREFERRED_SLOT -> ExperimentalClassCommandSupport.send(player,
                    "&cYour saved Focus slot is invalid. Please report this to the developer.");
        }
    }
}
