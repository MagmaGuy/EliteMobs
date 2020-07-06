package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.custombosses.RegionalBossEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CustomBossCommandHandler {

    public static void handleCommand(Player player, String[] args) {

        if (args.length < 2) {
            player.sendMessage("[EliteMobs] Possible command syntax:");
            player.sendMessage("- /elitemobs customboss [filename] setSpawnLocation");
            player.sendMessage("- /elitemobs customboss [filename] addSpawnLocation");
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
            case "addspawnlocation":
                addSpawnLocation(customBossConfigFields, player.getLocation());
                player.sendMessage("[EliteMobs] An additional spawn location was set to where you are standing!");
                return;
            case "setleashradius":
                setLeashRadius(customBossConfigFields, player, args);
            default:
                return;

        }

    }

    private static void addSpawnLocation(CustomBossConfigFields customBossConfigFields, Location location) {
        customBossConfigFields.addSpawnLocation(location.clone().add(new Vector(0, 0.2, 0)));
        new RegionalBossEntity(customBossConfigFields, customBossConfigFields.new ConfigRegionalEntity(location, 0));
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
