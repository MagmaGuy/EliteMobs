package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class QuestCompleteQuestCommand extends AdvancedCommand {
    public QuestCompleteQuestCommand() {
        super(List.of("quest"));
        addLiteral("complete");
        addArgument("id", new ArrayList<>());
        setUsage("/em quest complete");
        setPermission("elitemobs.quest.complete");
        setSenderType(SenderType.PLAYER);
        setDescription("Completes a quest for a player. Meant to be used via menu, not directly.");
    }

    @Override
    public void execute(CommandData commandData) {
        QuestCommand.completeQuest(commandData.getStringArgument("id"), commandData.getPlayerSender());
    }
}