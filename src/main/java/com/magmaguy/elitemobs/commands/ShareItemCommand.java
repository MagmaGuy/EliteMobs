package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class ShareItemCommand extends AdvancedCommand {
    public ShareItemCommand() {
        super(List.of("shareItem"));
        setUsage("/em shareItem");
        setPermission("elitemobs.shareitem");
        setSenderType(SenderType.PLAYER);
        setDescription("Teleports players to the Adventurer's Guild Hub or opens the Adventurer's Guild menu.");
    }

    @Override
    public void execute() {
        ShareItem.showOnChat(getCurrentPlayerSender());
    }
}
