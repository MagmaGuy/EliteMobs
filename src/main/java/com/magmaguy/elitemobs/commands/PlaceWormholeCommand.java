package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.wormholes.WormholeConfig;
import com.magmaguy.elitemobs.wormhole.Wormhole;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class PlaceWormholeCommand extends AdvancedCommand {
    public PlaceWormholeCommand() {
        super(List.of("place"));
        addLiteral("wormhole");
        addArgument("filename", new ListStringCommandArgument(WormholeConfig.getWormholes().keySet().stream().toList(),"<filename>"));
        addArgument("wormholeOption", new IntegerCommandArgument(List.of(1, 2), "<1>/<2>"));
        setUsage("/em place wormhole <filename> <1/2>");
        setPermission("elitemobs.place.admin");
        setSenderType(SenderType.PLAYER);
        setDescription("Place a wormhole teleport at your current location.");
    }

    @Override
    public void execute(CommandData commandData) {
        for (Wormhole wormhole : Wormhole.getWormholes()) {
            if (wormhole.getWormholeConfigFields().getFilename().equals(commandData.getStringArgument("filename")))
                switch (commandData.getStringArgument("wormholeOption")) {
                    case "1":
                        wormhole.getWormholeEntry1().updateLocation(commandData.getPlayerSender());
                        return;
                    case "2":
                        wormhole.getWormholeEntry2().updateLocation(commandData.getPlayerSender());
                        return;
                    default:
                        Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getWormholeInvalidOptionMessage());
                }
        }
        Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getWormholeSetFailedMessage());
    }
}