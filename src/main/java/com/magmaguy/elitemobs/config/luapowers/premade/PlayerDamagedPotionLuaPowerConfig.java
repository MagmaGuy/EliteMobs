package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public abstract class PlayerDamagedPotionLuaPowerConfig extends LuaPowersConfigFields {

    private final String potionEffectType;
    private final int duration;
    private final int amplifier;
    private final int localCooldown;
    private final int globalCooldown;

    protected PlayerDamagedPotionLuaPowerConfig(String baseFileName,
                                                String effect,
                                                PowersConfigFields.PowerType powerType,
                                                String potionEffectType,
                                                int duration,
                                                int amplifier,
                                                int localCooldown,
                                                int globalCooldown) {
        super(baseFileName, effect, powerType);
        this.potionEffectType = potionEffectType;
        this.duration = duration;
        this.amplifier = amplifier;
        this.localCooldown = localCooldown;
        this.globalCooldown = globalCooldown;
    }

    @Override
    public String getSource() {
        return playerDamagedPotion(potionEffectType, duration, amplifier, localCooldown, globalCooldown);
    }
}
