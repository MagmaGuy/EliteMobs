package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.progression.SelectionResult;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.DynamicListStringCommandArgument;
import org.bukkit.entity.Player;

import java.util.List;

public final class ExperimentalClassSelectCommand extends AdvancedCommand {
    public ExperimentalClassSelectCommand() {
        super(List.of("class"));
        addLiteral("select");
        addArgument("class", new DynamicListStringCommandArgument(
                () -> ExperimentalClassCommandSupport.catalog().forms().stream()
                        .map(ClassFormDefinition::id).toList(),
                "<class>"));
        setUsage("/em class select <class>");
        setDescription("Selects an unlocked [Alpha] Advanced Combat System class form.");
        setPermission("elitemobs.command");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        Player player = commandData.getPlayerSender();
        if (!ExperimentalClassCommandSupport.requireModule(player)) return;
        String formId = commandData.getStringArgument("class");
        SelectionResult result = ExperimentalCombatModule.get().selectForm(player, formId);
        ExperimentalClassCommandSupport.reportSelection(player, formId, result);
    }
}
