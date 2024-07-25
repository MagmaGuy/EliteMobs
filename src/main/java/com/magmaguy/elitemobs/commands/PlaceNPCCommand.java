package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.NPCCommands;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class PlaceNPCCommand extends AdvancedCommand {
    public PlaceNPCCommand() {
        super(List.of("place"));
        addLiteral("npc");
        addArgument("filename", new ArrayList<>(NPCsConfig.npcEntities.keySet()));
        setUsage("/em place npc <filename>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Permanently adds an npc to the location the user is standing on.");
    }

    @Override
    public void execute() {
        NPCCommands.set(getCurrentPlayerSender(), getStringArgument("filename"));
    }
}