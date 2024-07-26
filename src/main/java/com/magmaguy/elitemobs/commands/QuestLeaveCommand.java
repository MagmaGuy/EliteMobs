package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class QuestLeaveCommand extends AdvancedCommand {
    public QuestLeaveCommand() {
        super(List.of("quest"));
        addLiteral("leave");
        setUsage("/em quest leave");
        setPermission("elitemobs.quest.leave");
        setSenderType(SenderType.PLAYER);
        setDescription("Leaves a quest. Used via menu, can't be directly used.");
    }

    @Override
    public void execute(CommandData commandData) {
        QuestCommand.leaveQuest(commandData.getPlayerSender(), commandData.getStringArgument("questID"));
    }
}