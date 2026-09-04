package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.progression.InputProfile;
import com.magmaguy.elitemobs.experimentalcombat.progression.SelectionResult;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public final class ExperimentalClassInputCommand extends AdvancedCommand {
    public ExperimentalClassInputCommand() {
        super(List.of("class"));
        addLiteral("input");
        addArgument("profile", new ListStringCommandArgument(
                Arrays.stream(InputProfile.values()).map(InputProfile::storedId).toList(),
                "<profile>"));
        setUsage("/em class input <java_hotbar_layer|focus_item>");
        setDescription("Selects an Experimental Combat ability input profile.");
        setPermission("elitemobs.command");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        Player player = commandData.getPlayerSender();
        if (!ExperimentalClassCommandSupport.requireModule(player)) return;
        String requested = commandData.getStringArgument("profile");
        InputProfile profile = InputProfile.fromStoredId(requested).orElseThrow();
        SelectionResult result = ExperimentalCombatModule.get().selectInput(player, profile);
        ExperimentalClassCommandSupport.reportInputSelection(player, result);
        InputProfile appliedProfile = result.accepted()
                ? ExperimentalCombatModule.get().activeInputProfile(player)
                : profile;
        if (result.accepted() && appliedProfile == InputProfile.JAVA_HOTBAR_LAYER)
            ExperimentalClassCommandSupport.send(player,
                    "&fF then 1 &7" + ExperimentalCombatModule.get().abilityName(player, AbilitySlot.MOBILITY)
                            + " &8| &fF then 2 &7"
                            + ExperimentalCombatModule.get().abilityName(player, AbilitySlot.SIGNATURE)
                            + " &8| &fF then 3 &7"
                            + ExperimentalCombatModule.get().abilityName(player, AbilitySlot.UTILITY));
        else if (result.accepted())
            ExperimentalClassCommandSupport.send(player,
                    "&7Use &f/em class focus &7to receive the universal Focus item.");
    }
}
