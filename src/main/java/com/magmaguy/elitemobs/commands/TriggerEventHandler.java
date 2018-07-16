package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.events.DeadMoon;
import com.magmaguy.elitemobs.events.SmallTreasureGoblin;
import org.bukkit.command.CommandSender;

public class TriggerEventHandler {

    public static void triggerEventCommand(CommandSender commandSender, String[] args){

        if (CommandHandler.permCheck(CommandHandler.EVENT_LAUNCH_SMALLTREASUREGOBLIN, commandSender) && args[1].equalsIgnoreCase("smalltreasuregoblin")) {
            SmallTreasureGoblin.initializeEvent();
            commandSender.sendMessage("Queued small treasure goblin event for next valid zombie spawn");
        }
        if (CommandHandler.permCheck(CommandHandler.EVENT_LAUNCH_DEADMOON, commandSender) && args[1].equalsIgnoreCase("deadmoon")) {
            DeadMoon.initializeEvent();
            commandSender.sendMessage("Queued deadmoon event for next new moon");
        }

    }

}
