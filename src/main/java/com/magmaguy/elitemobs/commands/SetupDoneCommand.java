package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class SetupDoneCommand extends AdvancedCommand {
    public SetupDoneCommand() {
        super(List.of("setup"));
        addLiteral("done");
        setUsage("/setup done");
        setPermission("elitemobs.*");
        setDescription("Toggles whether the setup message will show up.");
    }

    @Override
    public void execute() {
        DefaultConfig.toggleSetupDone();
        if (DefaultConfig.isSetupDone())
            Logger.sendMessage(getCurrentCommandSender(), "&aEliteMobs will no longer send messages on login. You can do [/em setup done] again to revert this.");
        else
            Logger.sendMessage(getCurrentCommandSender(), "&aEliteMobs will once again send messages on login. You can do [/em setup done] again to revert this.");
    }
}
