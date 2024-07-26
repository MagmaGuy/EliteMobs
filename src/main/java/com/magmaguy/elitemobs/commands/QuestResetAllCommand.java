package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;

import java.util.ArrayList;
import java.util.List;

public class QuestResetAllCommand extends AdvancedCommand {
    public QuestResetAllCommand() {
        super(List.of("quest"));
        addLiteral("reset");
        addLiteral("all");
        addArgument("player", new ArrayList<>());
        setUsage("/em quest reset all <player>");
        setPermission("elitemobs.quest.reset.all");
        setDescription("Resets all quests for a specific player.");
    }

    @Override
    public void execute(CommandData commandData) {
        QuestCommand.resetQuests(commandData.getCommandSender(), commandData.getStringArgument("player"));
    }
}