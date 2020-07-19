package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.events.timedevents.DeadMoonEvent;
import com.magmaguy.elitemobs.events.timedevents.MeteorEvent;
import com.magmaguy.elitemobs.events.timedevents.SmallTreasureGoblinEvent;
import org.bukkit.command.CommandSender;

public class TriggerEventHandler {

    public static void triggerEventCommand(CommandSender commandSender, String[] args) {

        if (CommandHandler.permCheck(CommandHandler.EVENTS, commandSender) && args[1].equalsIgnoreCase("smalltreasuregoblin")) {
            new SmallTreasureGoblinEvent();
            commandSender.sendMessage("Queued small treasure goblin event for next valid zombie spawn");
        }
        if (CommandHandler.permCheck(CommandHandler.EVENTS, commandSender) && args[1].equalsIgnoreCase("deadmoon")) {
            new DeadMoonEvent();
            commandSender.sendMessage("Queued deadmoon event for next new moon");
        }

        if (CommandHandler.permCheck(CommandHandler.EVENTS, commandSender) && args[1].equalsIgnoreCase("meteor")) {
            new MeteorEvent();
            commandSender.sendMessage("Queued meteor event for next spawn");
        }

    }

}
