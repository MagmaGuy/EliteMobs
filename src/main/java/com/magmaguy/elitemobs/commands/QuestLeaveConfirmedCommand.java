package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

/**
 * Second half of the abandon flow: {@code /em quest leave} now only sends the
 * confirmation prompt, and the prompt's click runs this command. Exists because
 * abandoning starts the quest's full cooldown, and players were losing quests to
 * a single misclick on [Abandon] while trying to turn them in.
 */
public class QuestLeaveConfirmedCommand extends AdvancedCommand {
    public QuestLeaveConfirmedCommand() {
        super(List.of("quest"));
        addLiteral("leaveconfirmed");
        addArgument("questID", new ListStringCommandArgument("<questID>"));
        setUsage("/em quest leaveconfirmed");
        setPermission("elitemobs.quest.leave");
        setSenderType(SenderType.PLAYER);
        setDescription("Confirms leaving a quest. Used via the confirmation prompt, can't be directly used.");
    }

    @Override
    public void execute(CommandData commandData) {
        QuestCommand.leaveQuestConfirmed(commandData.getPlayerSender(), commandData.getStringArgument("questID"));
    }
}
