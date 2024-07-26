package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class QuestAcceptCommand extends AdvancedCommand {
    public QuestAcceptCommand() {
        super(List.of("quest"));
        addLiteral("accept");
        addArgument("questID", new ArrayList<>());
        setUsage("/em quest accept <questID>");
        setPermission("elitemobs.quest.command");
        setSenderType(SenderType.PLAYER);
        setDescription("Accepts a quest. Used via menu, can't be directly used.");
    }

    @Override
    public void execute(CommandData commandData) {
        QuestCommand.joinQuest(commandData.getStringArgument("questID"), commandData.getPlayerSender());
    }
}
