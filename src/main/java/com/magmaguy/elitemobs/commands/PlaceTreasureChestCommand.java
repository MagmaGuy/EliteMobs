package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class PlaceTreasureChestCommand extends AdvancedCommand {
    public PlaceTreasureChestCommand() {
        super(List.of("place"));
        addLiteral("treasureChest");
        addArgument("filename", new ArrayList<>(CustomTreasureChestsConfig.getCustomTreasureChestConfigFields().keySet()));
        setUsage("/em place treasureChest <filename>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Permanently adds a treasure chest to the location the user is standing on.");
    }

    @Override
    public void execute() {
        CustomBossCommandHandler.addSpawnLocation(getStringArgument("filename"), getCurrentPlayerSender());
    }
}