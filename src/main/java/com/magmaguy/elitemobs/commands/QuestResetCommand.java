package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;

import java.util.List;

public class QuestResetCommand extends AdvancedCommand {
    public QuestResetCommand() {
        super(List.of("quest"));
        addLiteral("reset");
        addArgument("player", new PlayerCommandArgument());
        addArgument("questName", new ListStringCommandArgument(CustomQuestsConfig.getCustomQuests().keySet().stream().toList(),"<questName>"));
        setUsage("/em quest reset <player> <quest filename>");
        setPermission("elitemobs.quest.reset");
        setDescription("Resets a specific quest for a specific player.");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        QuestCommand.resetQuest(commandData.getCommandSender(), commandData.getStringArgument("player"), commandData.getStringArgument("questName"));
    }
}