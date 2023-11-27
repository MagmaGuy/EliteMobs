package com.magmaguy.elitemobs.thirdparty.custommodels;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;

public interface CustomModelInterface {
    public void shoot();
    public void melee();
    public void playAnimationByName(String animationName);
    public void setName(String nametagName, boolean visible);
    public void setNameVisible(boolean visible);
    public void addPassenger(CustomBossEntity passenger);
    public void switchPhase();
}
