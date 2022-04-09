package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class CustomLootEntry implements Serializable {
    @Getter
    @Setter
    private double chance = 1;
    @Getter
    @Setter
    private int amount = 1;
    @Getter
    @Setter
    private String permission = "";
    @Getter
    @Setter
    private int wave = -1;
    @Getter
    @Setter
    private int itemLevel = 1;

    public CustomLootEntry() {
    }

    public static void errorMessage(String rawString, String configFilename, String reason) {
        new WarningMessage("Failed to parse entry " + rawString + " for file " + configFilename + " due to invalid: " + reason);
    }

    public boolean willDrop(Player player) {
        if (!permission.isEmpty() && !player.hasPermission(permission)) return false;
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    public void locationDrop(int itemTier, Player player, Location dropLocation) {
        //meant to be overriden by the classes that extend it
    }

    public void directDrop(int itemTier, Player player) {
        //meant to be overriden by the classes that extend it
    }

    //used specifically so loot can be attributed to the right source
    public void locationDrop(int itemTier, Player player, Location dropLocation, EliteEntity eliteEntity) {
        //meant to be overriden by the classes that extend it
    }

    //used specifically so loot can be attributed to the right source
    public void directDrop(int itemTier, Player player, EliteEntity eliteEntity) {
        //meant to be overriden by the classes that extend it
    }
}
