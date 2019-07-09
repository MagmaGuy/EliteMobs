package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.EliteMobs;
import org.bukkit.command.CommandSender;

public class ReloadHandler {

    public static void reload(CommandSender commandSender) {
        EliteMobs.validWorldList.clear();
        EliteMobs.worldScanner();
        EliteMobs.initializeConfigs();
        commandSender.sendMessage("[EliteMobs] Configuration files reloaded!");
    }

}
