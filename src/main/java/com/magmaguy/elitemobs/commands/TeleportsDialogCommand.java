package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreenDialog;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

/** Opens the teleports dialog, or one of its groups; the group buttons in that dialog run it. */
public class TeleportsDialogCommand extends AdvancedCommand {
    public TeleportsDialogCommand() {
        super(List.of("teleportsdialog"));
        addOptionalArgument("group", new ListStringCommandArgument("Teleport group (dialog navigation only)"));
        setPermission("elitemobs.command");
        setDescription("Internal dialog teleport navigation command.");
        setUsage("/em teleportsdialog [group]");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        String group = commandData.getStringArgument("group");
        if (group == null) PlayerStatusScreenDialog.showTeleportsDialog(commandData.getPlayerSender());
        else PlayerStatusScreenDialog.showTeleportGroupDialog(commandData.getPlayerSender(), group);
    }
}
