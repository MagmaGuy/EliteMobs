package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

public class QuestTrackCommand extends AdvancedCommand {
    public QuestTrackCommand() {
        super(List.of("quest"));
        addLiteral("track");
        addArgument("questID", new ListStringCommandArgument("<questID>"));
        setUsage("/em quest track <questID>");
        setPermission("elitemobs.quest.track");
        setSenderType(SenderType.PLAYER);
        setDescription("Tracks a quest. Used via menu, can't be directly used.");
    }

    @Override
    public void execute(CommandData commandData) {
        QuestCommand.trackQuest(commandData.getStringArgument("questID"), commandData.getPlayerSender());
    }
}
