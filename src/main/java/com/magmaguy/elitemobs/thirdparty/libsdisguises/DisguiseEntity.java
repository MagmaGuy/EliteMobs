package com.magmaguy.elitemobs.thirdparty.libsdisguises;

import com.magmaguy.elitemobs.utils.WarningMessage;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.*;
import org.bukkit.entity.Entity;

public class DisguiseEntity {

    /**
     * This method disguises the entity based on a config entry. It should be invoked after checking if libsdisguises
     * is in the server.
     *
     * @param disguiseName Raw name following config format
     */
    public static void disguise(String disguiseName, Entity entity) {

        if (disguiseName.contains("player:")) {
            playerDisguise(disguiseName.replace("player:", ""), entity);
            return;
        }

        if (disguiseName.contains("custom")) {
            customDisguise(disguiseName.replace("custom:", ""), entity);
            return;
        }

        DisguiseType disguiseType;

        try {
            disguiseType = DisguiseType.valueOf(disguiseName);
        } catch (Exception ex) {
            new WarningMessage("Disguise " + disguiseName + " is not a valid disguise name! Entity " + entity.getCustomName() + " will not have a disguise.");
            return;
        }

        if (disguiseType.isMob())
            livingEntityDisguise(disguiseType, entity);
        else if (disguiseType.isMisc())
            miscEntityDisguise(disguiseType, entity);
        else
            new WarningMessage("Disguise " + disguiseName + " is not a valid disguise name! Entity " + entity.getCustomName() + " will not have a disguise.");
    }

    private static void playerDisguise(String playerName, Entity entity) {
        PlayerDisguise playerDisguise = new PlayerDisguise(playerName);
        playerDisguise.setEntity(entity);
        playerDisguise.setName(entity.getName());
        playerDisguise.setDynamicName(true);
        playerDisguise.startDisguise();
    }

    private static void livingEntityDisguise(DisguiseType disguiseType, Entity entity) {
        MobDisguise mobDisguise = new MobDisguise(disguiseType);
        mobDisguise.setEntity(entity);
        mobDisguise.setDisguiseName(entity.getName());
        mobDisguise.setDynamicName(true);
        mobDisguise.startDisguise();
    }

    private static void miscEntityDisguise(DisguiseType disguiseType, Entity entity) {
        MiscDisguise miscDisguise = new MiscDisguise(disguiseType);
        miscDisguise.setDisguiseName(entity.getName());
        miscDisguise.setDynamicName(true);
        miscDisguise.setEntity(entity);
        miscDisguise.startDisguise();


    }

    private static void customDisguise(String customDisguise, Entity entity) {
        Disguise disguise = DisguiseAPI.getCustomDisguise(customDisguise);
        disguise.setEntity(entity);
        disguise.setDisguiseName(entity.getName());
        disguise.setDynamicName(true);
        disguise.startDisguise();
    }

}
