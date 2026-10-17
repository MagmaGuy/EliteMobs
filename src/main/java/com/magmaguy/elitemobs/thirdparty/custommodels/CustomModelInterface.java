package com.magmaguy.elitemobs.thirdparty.custommodels;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.Location;

import java.util.Set;
import java.util.UUID;

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
     * FMM also supplies a nameplate above the hitbox when no authored anchor exists.
     * Other providers may still require a nametag bone. This capability check keeps
     * missing-name warnings limited to providers which cannot render a fallback.
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

    /**
     * Whether Java clients place this model's bones as passengers of its base entity, so other packet text can
     * ride that entity too. False while the bones are teleported or the model plugin cannot tell.
     */
    default boolean bonesRideEntity() {
        return false;
    }

    /**
     * Players the model is shown to. While {@link #bonesRideEntity()}, these are the only Java players whose clients
     * track the base entity the bones ride.
     */
    default Set<UUID> getModelViewers() {
        return Set.of();
    }
}
