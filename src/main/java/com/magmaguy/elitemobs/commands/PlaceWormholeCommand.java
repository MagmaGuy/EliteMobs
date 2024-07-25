package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfig;
import com.magmaguy.elitemobs.wormhole.Wormhole;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class PlaceWormholeCommand extends AdvancedCommand {
    public PlaceWormholeCommand() {
        super(List.of("place"));
        addLiteral("wormhole");
        addArgument("filename", new ArrayList<>(WormholeConfig.getWormholes().keySet()));
        addArgument("wormholeOption", List.of(1, 2));
        setUsage("/em wormhole <filename> <1/2>");
        setPermission("elitemobs.place.wormhole");
        setSenderType(SenderType.PLAYER);
        setDescription("Place a wormhole teleport at your current location.");
    }

    @Override
    public void execute() {
        for (Wormhole wormhole : Wormhole.getWormholes()) {
            if (wormhole.getWormholeConfigFields().getFilename().equals(getStringArgument("wormholeFilename")))
                switch (getStringArgument("wormholeOption")) {
                    case "1":
                        wormhole.getWormholeEntry1().updateLocation(getCurrentPlayerSender());
                        return;
                    case "2":
                        wormhole.getWormholeEntry2().updateLocation(getCurrentPlayerSender());
                        return;
                    default:
                        Logger.sendMessage(getCurrentCommandSender(), "Not a valid wormhole option! Pick 1 or 2 to set either end of the wormhole.");
                }
        }
        Logger.sendMessage(getCurrentCommandSender(), "Failed to set location for this wormhole.");
    }
}