package com.magmaguy.elitemobs.commands.npc;

import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.utils.Round;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.magmaguy.elitemobs.commands.CommandHandler.permCheck;

public class NPCCommands {

    public static void parseNPCCommand(CommandSender commandSender, String[] args) {

        if (args.length <= 1) {
            commandSender.sendMessage("[EliteMobs] Invalid command syntax. Valid options:");
            commandSender.sendMessage("/em npc set [npc filename]");
            commandSender.sendMessage("/em npc remove [npc filename]");
            return;
        }

        switch (args[1]) {
            case "set":
                if (permCheck("elitemobs.npc.set", commandSender))
                    setNPC((Player) commandSender, args);
                break;
            case "remove":
                if (permCheck("elitemobs.npc.remove", commandSender))
                    removeNPC((Player) commandSender, args);
                break;
            default:
        }

    }

    private static void setNPC(Player player, String[] args) {

        if (args.length == 2) {
            player.sendMessage("[EliteMobs] Invalid command syntax. Valid options:");
            player.sendMessage("/em npc set [filename]");
            keysMessage(player);
            return;
        }

        NPCsConfigFields npCsConfigFields = NPCsConfig.getNPCsList().get(args[2]);
        if (npCsConfigFields == null) {
            player.sendMessage("[EliteMobs] Invalid NPC filename.");
            return;
        }

        Location playerLocation = player.getLocation();

        String location = playerLocation.getWorld().getName() + ","
                + Round.twoDecimalPlaces(playerLocation.getX()) + ","
                + Round.twoDecimalPlaces(playerLocation.getY()) + ","
                + Round.twoDecimalPlaces(playerLocation.getZ()) + ","
                + Round.twoDecimalPlaces(playerLocation.getYaw()) + ","
                + Round.twoDecimalPlaces(playerLocation.getPitch());

        try {
            NPCEntity.removeNPCEntity(NPCEntity.getNPCEntityFromFields(npCsConfigFields));
            npCsConfigFields.setEnabled(true);
            npCsConfigFields.setLocation(location);
            new NPCEntity(npCsConfigFields);
        } catch (Exception e) {
            player.sendMessage("[EliteMobs] Invalid NPC filename.");
        }

    }

    public static void removeNPC(Player player, String[] args) {
        if (args.length == 2) {
            player.sendMessage("[EliteMobs] Invalid command syntax. Valid options:");
            player.sendMessage("/em npc remove [filename]");
            keysMessage(player);
            return;
        }

        NPCsConfigFields npCsConfigFields = NPCsConfig.getNPCsList().get(args[2]);
        if (npCsConfigFields == null) {
            player.sendMessage("[EliteMobs] Invalid NPC filename.");
            return;
        }

        try {
            NPCEntity.removeNPCEntity(NPCEntity.getNPCEntityFromFields(npCsConfigFields));
            npCsConfigFields.setEnabled(false);
            new NPCEntity(npCsConfigFields);
        } catch (Exception e) {
            player.sendMessage("[EliteMobs] Invalid NPC filename.");
        }
    }

    private static void keysMessage(Player player) {
        player.sendMessage("Valid file names: ");
        for (NPCsConfigFields npCsConfigFields : NPCsConfig.getNPCsList().values())
            player.sendMessage(npCsConfigFields.getFileName());
    }

}
