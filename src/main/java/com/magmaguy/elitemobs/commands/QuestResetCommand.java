package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;

import java.util.ArrayList;
import java.util.List;

public class QuestResetCommand extends AdvancedCommand {
    public QuestResetCommand() {
        super(List.of("quest"));
        addLiteral("reset");
        addArgument("player", new ArrayList<>());
        addArgument("questName", new ArrayList<>(CustomQuestsConfig.getCustomQuests().keySet()));
        setUsage("/em quest reset <player> <quest filename>");
        setPermission("elitemobs.quest.reset.quest");
        setDescription("Resets a specific quest for a specific player.");
    }

    @Override
    public void execute() {
        QuestCommand.resetQuest(getCurrentCommandSender(), getStringArgument("player"), getStringArgument("questName"));
    }
}