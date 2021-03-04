package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.events.timedevents.DeadMoonEvent;
import com.magmaguy.elitemobs.events.timedevents.MeteorEvent;
import com.magmaguy.elitemobs.events.timedevents.SmallTreasureGoblinEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventCommand {

    public static void trigger(CommandSender commandSender, String eventName) {

        switch (eventName) {
            case "balrog.yml":
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    CustomBossEntity.constructCustomBoss("balrog.yml", player.getTargetBlockExact(20).getLocation());
                    commandSender.sendMessage("Spawned the Balrog!");
                    break;
                }
                commandSender.sendMessage("This command must be used as a player!");
                break;
            case "dead_moon.yml":
                new DeadMoonEvent();
                commandSender.sendMessage("Queued dead moon event for the next dead moon phase during night time!");
                break;
            case "fae.yml":
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    CustomBossEntity.constructCustomBoss("fire_fae.yml", player.getTargetBlockExact(20).getLocation());
                    CustomBossEntity.constructCustomBoss("ice_fae.yml", player.getTargetBlockExact(20).getLocation());
                    CustomBossEntity.constructCustomBoss("lightning_fae.yml", player.getTargetBlockExact(20).getLocation());
                    commandSender.sendMessage("Spawned the Fae!");
                    break;
                }
                commandSender.sendMessage("This command must be used as a player!");
                break;
            case "kraken.yml":
                commandSender.sendMessage("This event is under maintenance, try again later!");
                break;
            case "meteor.yml":
                commandSender.sendMessage("Queued meteor event for the next zombie spawn!");
                new MeteorEvent();
                break;
            case "small_treasure_goblin.yml":
                commandSender.sendMessage("Queued small treasure goblin event for hte next zombie spawn!");
                new SmallTreasureGoblinEvent();
                break;
            default:
        }

    }

}
