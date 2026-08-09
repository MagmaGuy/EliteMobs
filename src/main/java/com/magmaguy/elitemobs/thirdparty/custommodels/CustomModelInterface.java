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
     * Whether this model is capable of rendering a nametag at all.
     * <p>
     * A model that defines no nametag anchor can never show a name: the underlying
     * living entity is hidden by the model plugin, so its vanilla nametag is not
     * rendered either, and the model has nowhere to draw one. A boss configured with
     * {@code alwaysShowName: true} on such a model is therefore silently nameless.
     * This exists so that misconfiguration can be reported instead of being invisible.
     *
     * @return false only when it is known that no nametag can ever render; true when
     * a nametag anchor exists or when the model plugin cannot report the capability.
     */
    boolean hasNametagBone();

    /**
     * Sets whether the model should sync movement with the base entity.
     * @param syncMovement true to sync movement, false otherwise
     */
    void setSyncMovement(boolean syncMovement);
}
