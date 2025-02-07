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
        if (disguiseName.contains("player:")) {
            playerDisguise(disguiseName.replace("player:", ""), entity);
            return;
        }

        if (disguiseName.contains("custom")) {
            try {
                customDisguise(disguiseName.replace("custom:", ""), entity, customDisguiseData, filename);
            } catch (Exception ex) {
                Logger.warn("Failed to assign custom disguise " + disguiseName + "! Did you configure the disguise correctly?");
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
            Logger.warn("Disguise " + disguiseName + " is not a valid disguise name! Entity " + entity.getCustomName() + " will not have a disguise.");
            return;
        }

        if (disguiseType.isMob())
            livingEntityDisguise(disguiseType, entity, baby);
        else if (disguiseType.isMisc())
            miscEntityDisguise(disguiseType, entity);
        else
            Logger.warn("Disguise " + disguiseName + " is not a valid disguise name! Entity " + entity.getCustomName() + " will not have a disguise.");
    }

    private static void playerDisguise(String playerName, Entity entity) {
        PlayerDisguise playerDisguise = new PlayerDisguise(playerName);
        scheduleDisguise(playerDisguise, entity);
    }

    private static void livingEntityDisguise(DisguiseType disguiseType, Entity entity, boolean baby) {
        MobDisguise mobDisguise = new MobDisguise(disguiseType, !baby);
        scheduleDisguise(mobDisguise, entity);
    }

    private static void miscEntityDisguise(DisguiseType disguiseType, Entity entity) {
        MiscDisguise miscDisguise = new MiscDisguise(disguiseType);
        scheduleDisguise(miscDisguise, entity);
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
            scheduleDisguise(disguise, entity);
        } catch (Exception ex) {
            Logger.warn("Failed to set custom disguise for " + filename + " !");
            Logger.warn("Does the disguise exist? Is LibsDisguises up-to-date?");
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
        disguise.setDisguiseName(entity.getCustomName());
        disguise.setDynamicName(true);
        if ((DefaultConfig.isAlwaysShowNametags() || entity.getType().equals(EntityType.VILLAGER))
                && disguise instanceof PlayerDisguise) {
            ((PlayerDisguise) disguise).setNameVisible(true);
        } else if (disguise instanceof PlayerDisguise) {
            ((PlayerDisguise) disguise).setNameVisible(false);
        }
        disguise.startDisguise();
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
