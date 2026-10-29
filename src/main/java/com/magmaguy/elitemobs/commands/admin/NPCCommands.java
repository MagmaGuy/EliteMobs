package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.magmacore.util.Round;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NPCCommands {

    public static void set(Player player, String npcFileName) {

        NPCsConfigFields npCsConfigFields = NPCsConfig.getNpcEntities().get(npcFileName);
        // Retired scrap roles cannot be placed; enabling one would bring back an NPC with nothing to do.
        if (npCsConfigFields == null || npCsConfigFields.isRetiredRole()) {
            player.sendMessage(CommandMessagesConfig.getInvalidNpcFilenameMessage());
            return;
        }

        Location playerLocation = player.getLocation();

        String location = playerLocation.getWorld().getName() + ","
                + Round.twoDecimalPlaces(playerLocation.getX()) + ","
                + Round.twoDecimalPlaces(playerLocation.getY()) + ","
                + Round.twoDecimalPlaces(playerLocation.getZ()) + ","
                + Round.twoDecimalPlaces(playerLocation.getYaw()) + ","
                + Round.twoDecimalPlaces(playerLocation.getPitch());

        npCsConfigFields.setEnabled(true);
        boolean addedLocation = npCsConfigFields.addSpawnLocation(location);
        if (addedLocation || !NPCEntity.hasLoadedNPC(npCsConfigFields, location))
            new NPCEntity(npCsConfigFields, location);

    }

}
