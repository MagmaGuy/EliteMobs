package com.magmaguy.elitemobs.thirdparty.libsdisguises;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.utils.WarningMessage;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class DisguiseEntity {

    /**
     * This method disguises the entity based on a config entry. It should be invoked after checking if libsdisguises
     * is in the server.
     *
     * @param disguiseName Raw name following config format
     */
    public static void disguise(String disguiseName, Entity entity, CustomBossConfigFields customBossConfigFields) {

        if (disguiseName.contains("player:")) {
            playerDisguise(disguiseName.replace("player:", ""), entity);
            return;
        }

        if (disguiseName.contains("custom")) {
            try {
                customDisguise(disguiseName.replace("custom:", ""), entity, customBossConfigFields);
            } catch (Exception ex) {
                new WarningMessage("Failed to assign custom disguise " + disguiseName + "! Did you configure the disguise correctly?");
            }
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
        playerDisguise.setName(entity.getCustomName());
        playerDisguise.setDynamicName(true);
        playerDisguise.startDisguise();
    }

    private static void livingEntityDisguise(DisguiseType disguiseType, Entity entity) {
        MobDisguise mobDisguise = new MobDisguise(disguiseType);
        mobDisguise.setEntity(entity);
        mobDisguise.setDisguiseName(entity.getCustomName());
        mobDisguise.setDynamicName(true);
        mobDisguise.startDisguise();
    }

    private static void miscEntityDisguise(DisguiseType disguiseType, Entity entity) {
        MiscDisguise miscDisguise = new MiscDisguise(disguiseType);
        miscDisguise.setDisguiseName(entity.getCustomName());
        miscDisguise.setDynamicName(true);
        miscDisguise.setEntity(entity);
        miscDisguise.startDisguise();
    }

    private static void customDisguise(String customDisguise, Entity entity, CustomBossConfigFields customBossConfigFields) {
        Disguise disguise = DisguiseAPI.getCustomDisguise(customDisguise);
        try {
            if (disguise == null)
                if (customBossConfigFields.getCustomDisguiseData() != null) {
                    DisguiseAPI.addCustomDisguise(customDisguise, customBossConfigFields.getCustomDisguiseData());
                    disguise = DisguiseAPI.getCustomDisguise(customDisguise);
                }
            if (disguise == null)
                throw new NullPointerException();
            disguise.setEntity(entity);
            disguise.setDisguiseName(entity.getCustomName());
            disguise.setDynamicName(true);
            disguise.startDisguise();
        } catch (Exception ex) {
            new WarningMessage("Failed to set custom disguise for boss " + customBossConfigFields.getFileName() + " !");
            new WarningMessage("Does the disguise exist?");
            ex.printStackTrace();
        }
    }

    public static void setDisguiseNameVisibility(boolean disguiseNameVisibility, Entity entity) {
        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) return;
        Disguise disguise = DisguiseAPI.getDisguise(entity);
        if (disguise == null) return;
        if (!(disguise.getWatcher() instanceof PlayerWatcher)) return;
        PlayerWatcher playerWatcher = (PlayerWatcher) disguise.getWatcher();
        playerWatcher.setCustomNameVisible(disguiseNameVisibility);
    }

}
