package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class SetupDoneCommand extends AdvancedCommand {
    public SetupDoneCommand() {
        super(List.of("setup"));
        addLiteral("done");
        setUsage("/em setup done");
        setPermission("elitemobs.setup");
        setDescription("Toggles whether the setup message will show up.");
    }

    @Override
    public void execute(CommandData commandData) {
        DefaultConfig.toggleSetupDone();
        if (DefaultConfig.isSetupDone())
            Logger.sendMessage(commandData.getCommandSender(), "&aEliteMobs will no longer send messages on login. You can do [/em setup done] again to revert this.");
        else
            Logger.sendMessage(commandData.getCommandSender(), "&aEliteMobs will once again send messages on login. You can do [/em setup done] again to revert this.");
    }
}
