package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.NPCCommands;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

public class PlaceNPCCommand extends AdvancedCommand {
    public PlaceNPCCommand() {
        super(List.of("place"));
        addLiteral("npc");
        addArgument("filename", new ListStringCommandArgument(NPCsConfig.npcEntities.keySet().stream().toList(),"<filename>"));
        setUsage("/em place npc <filename>");
        setPermission("elitemobs.place.npc");
        setSenderType(SenderType.PLAYER);
        setDescription("Permanently adds an npc to the location the user is standing on.");
    }

    @Override
    public void execute(CommandData commandData) {
        NPCCommands.set(commandData.getPlayerSender(), commandData.getStringArgument("filename"));
    }
}