package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.custombosses.RegionalBossEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CustomBossCommandHandler {

    public static void handleCommand(Player player, String[] args) {

        if (args.length < 2) {
            player.sendMessage("[EliteMobs] Possible command syntax:");
            player.sendMessage("- /elitemobs customboss [filename] setSpawnLocation");
            player.sendMessage("- /elitemobs customboss [filename] setLeashRadius [radius]");
            return;
        }

        //Check which Custom Boss will be edited
        CustomBossConfigFields customBossConfigFields = CustomBossesConfig.getCustomBoss(args[1]);

        if (customBossConfigFields == null) {
            player.sendMessage("[EliteMobs] Invalid Custom Boss filename. List of valid Custom Bosses:");
            StringBuilder stringBuilder = new StringBuilder();
            for (CustomBossConfigFields customBossConfigFields1 : CustomBossesConfig.getCustomBosses().values())
                stringBuilder.append(customBossConfigFields1.getFileName()).append(", ");
            player.sendMessage(stringBuilder.toString());
            player.sendMessage("[EliteMobs] File names are CaSe SeNsItIvE!");
            return;
        }

        switch (args[2].toLowerCase()) {
            case "setspawnlocation":
                setLocation(customBossConfigFields, player.getLocation());
                player.sendMessage("[EliteMobs] New spawn location set to where you are standing!");
                return;
            case "setleashradius":
                setLeashRadius(customBossConfigFields, player, args);
            default:
                return;

        }

    }

    private static void setLocation(CustomBossConfigFields customBossConfigFields, Location location) {
        customBossConfigFields.setSpawnLocation(location);
        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntityList())
            if (customBossConfigFields.getFileName().equals(regionalBossEntity.getCustomBossConfigFields().getFileName()))
                regionalBossEntity.setSpawnLocation(location);
    }

    private static void setLeashRadius(CustomBossConfigFields customBossConfigFields, Player player, String[] args) {

        if (args.length < 3) {
            player.sendMessage("[EliteMobs] Possible command syntax:");
            player.sendMessage("- /elitemobs customboss [filename] setLeashRadius [radius]");
            return;
        }

        double leashRadius;

        try {
            leashRadius = Double.valueOf(args[3]);
        } catch (Exception ex) {
            player.sendMessage("[EliteMobs] Expected a number, got " + args[2] + " (not a valid number!)");
            player.sendMessage("[EliteMobs] Possible command syntax:");
            player.sendMessage("- /elitemobs customboss [filename] setLeashRadius [radius]");
            return;
        }

        customBossConfigFields.setLeashRadius(leashRadius);
        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntityList())
            if (customBossConfigFields.getFileName().equals(regionalBossEntity.getCustomBossConfigFields().getFileName()))
                regionalBossEntity.setLeashRadius(leashRadius);

    }

}
