package com.magmaguy.elitemobs.thirdparty.custommodels;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.Location;

public interface CustomModelInterface {
    void shoot();

    void melee();

    void playAnimationByName(String animationName);

    void setName(String nametagName, boolean visible);

    void setNameVisible(boolean visible);

    void addPassenger(CustomBossEntity passenger);

    void switchPhase();

    /**
     * Gets the location of the first nametag bone if one exists.
     * @return The location of the nametag bone, or null if none exists
     */
    Location getNametagBoneLocation();

    /**
     * Sets whether the model should sync movement with the base entity.
     * @param syncMovement true to sync movement, false otherwise
     */
    void setSyncMovement(boolean syncMovement);
}
