package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.quests.QuestInteractionHandler;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;
import java.util.UUID;

public class NPCQuestList extends AdvancedCommand {
    public NPCQuestList() {
        super(List.of("npc"));
        addLiteral("questList");
        addArgument("npcUUID", new ListStringCommandArgument("npcUUID"));
        setSenderType(SenderType.PLAYER);
        setUsage("Not to be run manually!");
        setDescription("Internal command.");
    }

    @Override
    public void execute(CommandData commandData) {
        QuestInteractionHandler.processNPCQuests(commandData.getPlayerSender(), EntityTracker.getNpcEntities().get(UUID.fromString(commandData.getStringArgument("npcUUID"))));
    }
}