package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.peacebanner.PeaceBannerManager;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;

import java.util.List;
import java.util.Map;

public class PeaceBannerListCommand extends AdvancedCommand {
    public PeaceBannerListCommand() {
        super(List.of("peacebanner"));
        addLiteral("list");
        setUsage("/em peacebanner list");
        setPermission("elitemobs.peacebanner.admin");
        setDescription("Lists all placed Peace Banners with their coordinates.");
    }

    @Override
    public void execute(CommandData commandData) {
        var bannerData = PeaceBannerManager.getAllBannerData();
        if (bannerData.isEmpty()) {
            commandData.getCommandSender().sendMessage("No Peace Banners are currently placed.");
            return;
        }
        commandData.getCommandSender().sendMessage("Peace Banners (" + bannerData.size() + " total):");
        for (Map.Entry<String, PeaceBannerManager.BannerData> entry : bannerData.entrySet()) {
            PeaceBannerManager.BannerData data = entry.getValue();
            commandData.getCommandSender().sendMessage(
                    "  - " + data.worldName() + " @ " + data.x() + ", " + data.y() + ", " + data.z() +
                            " (radius: " + data.chunkRadius() + " chunks)");
        }
    }
}
