package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.progression.SelectionResult;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.DynamicListStringCommandArgument;
import org.bukkit.entity.Player;

import java.util.List;

public final class AdvancedClassSelectCommand extends AdvancedCommand {
    public AdvancedClassSelectCommand() {
        super(List.of("class"));
        addLiteral("select");
        addArgument("class", new DynamicListStringCommandArgument(
                () -> AdvancedClassCommandSupport.catalog().forms().stream()
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
        if (!AdvancedClassCommandSupport.requireModule(player)) return;
        String formId = commandData.getStringArgument("class");
        SelectionResult result = AdvancedCombatModule.get().selectForm(player, formId);
        AdvancedClassCommandSupport.reportSelection(player, formId, result);
    }
}
