package com.magmaguy.elitemobs.thirdparty.custommodels;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;

public interface CustomModelInterface {
    void shoot();

    void melee();

    void playAnimationByName(String animationName);

    void setName(String nametagName, boolean visible);

    void setNameVisible(boolean visible);

    void addPassenger(CustomBossEntity passenger);

    void switchPhase();
}
