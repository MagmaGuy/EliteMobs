package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class QuestCompleteCommand extends AdvancedCommand {
    public QuestCompleteCommand() {
        super(List.of("quest"));
        addLiteral("complete");
        setUsage("/em quest complete");
        setPermission("elitemobs.quest.complete");
        setSenderType(SenderType.PLAYER);
        setDescription("Forces all of your currently active elite quests to be completed. For debugging use only.");
    }

    @Override
    public void execute(CommandData commandData) {
        QuestCommand.completeQuest(commandData.getPlayerSender());
    }
}