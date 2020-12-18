package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CustomBossCommandHandler {

    public static void handleCommand(Player player, String[] args) {

        if (args.length < 2) {
            player.sendMessage("[EliteMobs] Possible command syntax:");
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
                if (addSpawnLocation(customBossConfigFields, player.getLocation()))
                    player.sendMessage("[EliteMobs] An additional spawn location was set to where you are standing!");
                else {
                    player.sendMessage("Attempted to run command /em customboss " + customBossConfigFields.getFileName() + " addSpawnLocation ");
                    player.sendMessage("The file " + customBossConfigFields.getFileName() + " is not set to generate regional bosses and therefore no spawn locations can be added to the boss.");
                    player.sendMessage("Please refer to the EliteMobs wiki for documentation on World Bosses. If you're just trying to spawn a boss, use the command /em spawn");
                }
                return;
            //command: /em customboss [filename.yml] addRelativeLocation [minidungeonName]
            case "addrelativelocation":
                if (addRelativeSpawnLocation(customBossConfigFields, player.getLocation().clone(), args[3]))
                    player.sendMessage("[EliteMobs] Successfully added relative location!");
                else
                    player.sendMessage("[EliteMobs] Failed to add relative location!");
                return;
            case "setleashradius":
                setLeashRadius(customBossConfigFields, player, args);
                break;
            case "remove":
                break;
            default:
                return;
        }

    }

    private static boolean addSpawnLocation(CustomBossConfigFields customBossConfigFields, Location location) {
        if (!customBossConfigFields.isRegionalBoss()) {
            new WarningMessage("Attempted to run command /em customboss " + customBossConfigFields.getFileName() + " addSpawnLocation ");
            new WarningMessage("The file " + customBossConfigFields.getFileName() + " is not set to generate regional bosses and therefore no spawn locations can be added to the boss.");
            new WarningMessage("Please refer to the EliteMobs wiki for documentation on World Bosses. If you're just trying to spawn a boss, use the command /em spawn");
            return false;
        }
        CustomBossConfigFields.ConfigRegionalEntity configRegionalEntity = customBossConfigFields.addSpawnLocation(location.clone().add(new Vector(0, 0.2, 0)));
        new RegionalBossEntity(customBossConfigFields, configRegionalEntity);
        return true;
    }

    private static boolean addRelativeSpawnLocation(CustomBossConfigFields customBossConfigFields, Location location, String minidungeonString) {
        Minidungeon minidungeon = Minidungeon.minidungeons.get(minidungeonString);
        if (minidungeon == null)
            return false;
        return minidungeon.initializeRelativeLocationAddition(customBossConfigFields, location);
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

    private static void removeRegionalBoss() {

    }

}
