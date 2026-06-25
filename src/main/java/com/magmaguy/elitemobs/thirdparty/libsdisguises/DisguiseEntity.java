package com.magmaguy.elitemobs.thirdparty.libsdisguises;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.magmacore.util.Logger;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class DisguiseEntity {

    /**
     * This method disguises the entity based on a config entry. It should be invoked after checking if libsdisguises
     * is in the server.
     *
     * @param disguiseName Raw name following config format
     */
    public static void disguise(String disguiseName, Entity entity, String customDisguiseData, String filename) {
        Disguise disguise = buildDisguise(disguiseName, customDisguiseData, filename, entity.getCustomName());
        if (disguise != null)
            scheduleDisguise(disguise, entity);
    }

    public static boolean disguiseNext(String disguiseName, String displayName, boolean nameVisible, String customDisguiseData, String filename) {
        Disguise disguise = buildDisguise(disguiseName, customDisguiseData, filename, displayName);
        if (disguise == null) return false;
        configureDisguiseName(disguise, displayName, nameVisible);
        try {
            DisguiseAPI.disguiseNextEntity(disguise);
            return true;
        } catch (Throwable throwable) {
            Logger.warn("Failed to queue LibsDisguises disguise for " + filename + " !");
            Logger.warn("Does the installed LibsDisguises version support disguiseNextEntity?");
            return false;
        }
    }

    private static Disguise buildDisguise(String disguiseName, String customDisguiseData, String filename, String entityName) {
        if (disguiseName == null) return null;
        if (disguiseName.contains("player:"))
            return new PlayerDisguise(disguiseName.replace("player:", ""));

        if (disguiseName.contains("custom"))
            return customDisguise(disguiseName.replace("custom:", ""), customDisguiseData, filename);

        boolean baby = false;
        if (disguiseName.contains(":baby")) {
            disguiseName = disguiseName.replace(":baby", "");
            baby = true;
        }

        DisguiseType disguiseType;

        try {
            disguiseType = DisguiseType.valueOf(disguiseName);
        } catch (Exception ex) {
            Logger.warn("Disguise " + disguiseName + " is not a valid disguise name! Entity " + entityName + " will not have a disguise.");
            return null;
        }

        if (disguiseType.isMob())
            return new MobDisguise(disguiseType, !baby);
        else if (disguiseType.isMisc())
            return new MiscDisguise(disguiseType);
        else
            Logger.warn("Disguise " + disguiseName + " is not a valid disguise name! Entity " + entityName + " will not have a disguise.");
        return null;
    }

    private static Disguise customDisguise(String customDisguise, String customDisguiseData, String filename) {
        Disguise disguise = DisguiseAPI.getCustomDisguise(customDisguise);
        try {
            if (disguise == null)
                if (customDisguiseData != null) {
                    DisguiseAPI.addCustomDisguise(customDisguise, customDisguiseData);
                    disguise = DisguiseAPI.getCustomDisguise(customDisguise);
                }
            if (disguise == null)
                throw new NullPointerException();
            return disguise;
        } catch (Exception ex) {
            Logger.warn("Failed to set custom disguise for " + filename + " !");
            Logger.warn("Does the disguise exist? Is LibsDisguises up-to-date?");
            return null;
        }
    }

    private static void scheduleDisguise(Disguise disguise, Entity entity) {
        applyDisguise(disguise, entity);
        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
            applyDisguise(disguise, entity);
        }, 20);
    }

    private static void applyDisguise(Disguise disguise, Entity entity) {
        disguise.setEntity(entity);
        configureDisguiseName(disguise, entity.getCustomName(), DefaultConfig.isAlwaysShowNametags() || entity.getType().equals(EntityType.VILLAGER));
        disguise.startDisguise();
    }

    private static void configureDisguiseName(Disguise disguise, String displayName, boolean nameVisible) {
        disguise.setDisguiseName(displayName);
        disguise.setDynamicName(true);
        if (disguise instanceof PlayerDisguise)
            ((PlayerDisguise) disguise).setNameVisible(nameVisible);
    }

    public static void setDisguiseNameVisibility(boolean disguiseNameVisibility, Entity entity, String name) {
        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) return;
        Disguise disguise = DisguiseAPI.getDisguise(entity);
        if (disguise == null) return;
        if (disguise instanceof PlayerDisguise) {
            ((PlayerDisguise) disguise).setNameVisible(true);
        }
    }
}
