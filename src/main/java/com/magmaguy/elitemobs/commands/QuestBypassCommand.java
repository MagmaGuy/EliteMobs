package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;

import java.util.List;

public class QuestBypassCommand extends AdvancedCommand {
    public QuestBypassCommand() {
        super(List.of("quest"));
        addLiteral("bypass");
        setUsage("/em quest bypass");
        setPermission("elitemobs.quest.bypass");
        setDescription("Bypasses permission restrictions for elite quests.");
    }

    @Override
    public void execute() {
        QuestCommand.bypassQuestRequirements(getCurrentPlayerSender());
    }
}