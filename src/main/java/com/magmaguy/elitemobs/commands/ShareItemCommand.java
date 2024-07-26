package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class ShareItemCommand extends AdvancedCommand {
    public ShareItemCommand() {
        super(List.of("shareItem"));
        setUsage("/em shareItem");
        setPermission("elitemobs.shareitem");
        setSenderType(SenderType.PLAYER);
        setDescription("Shares the stats of the currently held EliteMobs item in chat.");
    }

    @Override
    public void execute(CommandData commandData) {
        ShareItem.showOnChat(commandData.getPlayerSender());
    }
}
