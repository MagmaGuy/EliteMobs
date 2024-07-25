package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;

import java.util.ArrayList;
import java.util.List;

public class QuestResetAllCommand extends AdvancedCommand {
    public QuestResetAllCommand() {
        super(List.of("quest"));
        addLiteral("reset");
        addLiteral("all");
        addArgument("player", new ArrayList<>());
        addArgument("questName", new ArrayList<>(CustomQuestsConfig.getCustomQuests().keySet()));
        setUsage("/em quest reset <player>");
        setPermission("elitemobs.*");
        setDescription("Resets all quests for a specific player.");
    }

    @Override
    public void execute() {
        QuestCommand.resetQuests(getCurrentCommandSender(), getStringArgument("player"));
    }
}