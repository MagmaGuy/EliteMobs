package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

public class QuestAcceptCommand extends AdvancedCommand {
    public QuestAcceptCommand() {
        super(List.of("quest"));
        addLiteral("accept");
        addArgument("questID", new ListStringCommandArgument("<questID>"));
        setUsage("/em quest accept <questID>");
        setPermission("elitemobs.quest.accept");
        setSenderType(SenderType.PLAYER);
        setDescription("Accepts a quest. Used via menu, can't be directly used.");
    }

    @Override
    public void execute(CommandData commandData) {
        QuestCommand.joinQuest(commandData.getStringArgument("questID"), commandData.getPlayerSender());
    }
}
