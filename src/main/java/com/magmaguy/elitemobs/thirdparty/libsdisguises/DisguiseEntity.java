package com.magmaguy.elitemobs.thirdparty.libsdisguises;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.utils.WarningMessage;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class DisguiseEntity {

    public static void initialize() {
        DisguiseConfig.setPlayerNameType(DisguiseConfig.PlayerNameType.ARMORSTANDS);
    }

    /**
     * This method disguises the entity based on a config entry. It should be invoked after checking if libsdisguises
     * is in the server.
     *
     * @param disguiseName Raw name following config format
     */
    public static void disguise(String disguiseName, Entity entity, String customDisguiseData, String filename) {

        if (disguiseName.contains("player:")) {
            playerDisguise(disguiseName.replace("player:", ""), entity);
            return;
        }

        if (disguiseName.contains("custom")) {
            try {
                customDisguise(disguiseName.replace("custom:", ""), entity, customDisguiseData, filename);
            } catch (Exception ex) {
                new WarningMessage("Failed to assign custom disguise " + disguiseName + "! Did you configure the disguise correctly?");
            }
            return;
        }


        boolean baby = false;
        if (disguiseName.contains(":baby")) {
            disguiseName = disguiseName.replace(":baby", "");
            baby = true;
        }

        DisguiseType disguiseType;

        try {
            disguiseType = DisguiseType.valueOf(disguiseName);
        } catch (Exception ex) {
            new WarningMessage("Disguise " + disguiseName + " is not a valid disguise name! Entity " + entity.getCustomName() + " will not have a disguise.");
            return;
        }

        if (disguiseType.isMob())
            livingEntityDisguise(disguiseType, entity, baby);
        else if (disguiseType.isMisc())
            miscEntityDisguise(disguiseType, entity);
        else
            new WarningMessage("Disguise " + disguiseName + " is not a valid disguise name! Entity " + entity.getCustomName() + " will not have a disguise.");
    }

    private static void playerDisguise(String playerName, Entity entity) {
        PlayerDisguise playerDisguise = new PlayerDisguise(playerName);
        playerDisguise.setEntity(entity);
        playerDisguise.setName(entity.getCustomName());
        playerDisguise.setNameVisible(DefaultConfig.isAlwaysShowNametags() || entity.getType().equals(EntityType.VILLAGER));
        playerDisguise.setDynamicName(true);
        playerDisguise.startDisguise();
    }

    private static void livingEntityDisguise(DisguiseType disguiseType, Entity entity, boolean baby) {
        MobDisguise mobDisguise = new MobDisguise(disguiseType, !baby);
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

    private static void customDisguise(String customDisguise, Entity entity, String customDisguiseData, String filename) {
        Disguise disguise = DisguiseAPI.getCustomDisguise(customDisguise);
        try {
            if (disguise == null)
                if (customDisguiseData != null) {
                    DisguiseAPI.addCustomDisguise(customDisguise, customDisguiseData);
                    disguise = DisguiseAPI.getCustomDisguise(customDisguise);
                }
            if (disguise == null)
                throw new NullPointerException();
            disguise.setEntity(entity);
            disguise.setDisguiseName(entity.getCustomName());
            disguise.setDynamicName(true);
            if ((DefaultConfig.isAlwaysShowNametags() || entity.getType().equals(EntityType.VILLAGER))
                    && disguise instanceof PlayerDisguise) {
                ((PlayerDisguise) disguise).setNameVisible(true);
            } else if (disguise instanceof PlayerDisguise) {
                ((PlayerDisguise) disguise).setNameVisible(false);
            }
            disguise.startDisguise();
        } catch (Exception ex) {
            new WarningMessage("Failed to set custom disguise for " + filename + " !");
            new WarningMessage("Does the disguise exist? Is LibsDisguises up-to-date?");
        }
    }

    public static void setDisguiseNameVisibility(boolean disguiseNameVisibility, Entity entity, String name) {
        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) return;
        Disguise disguise = DisguiseAPI.getDisguise(entity);
        if (disguise == null) return;
        if (disguise instanceof PlayerDisguise) {
            /*
            Currently broken, waiting for libs to fix it
            if (disguiseNameVisibility) {
                entity.setCustomName(name);
                disguise.setDisguiseName(name);
                disguise.setDynamicName(true);
            }
             */
            ((PlayerDisguise) disguise).setNameVisible(disguiseNameVisibility);
        }
        //todo: This doesn't work yes, check with libs later to see if he found a solution
        /*
        if (!(disguise.getWatcher() instanceof PlayerWatcher)) return;
        PlayerWatcher playerWatcher = (PlayerWatcher) disguise.getWatcher();
        playerWatcher.setNameVisible(disguiseNameVisibility);
        playerWatcher.setCustomNameVisible(disguiseNameVisibility);
         */
    }

}
