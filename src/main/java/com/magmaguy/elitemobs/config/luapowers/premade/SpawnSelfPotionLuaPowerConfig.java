package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public abstract class SpawnSelfPotionLuaPowerConfig extends LuaPowersConfigFields {

    private final String potionEffectType;
    private final int duration;
    private final int amplifier;

    protected SpawnSelfPotionLuaPowerConfig(String baseFileName,
                                            String effect,
                                            PowersConfigFields.PowerType powerType,
                                            String potionEffectType,
                                            int duration,
                                            int amplifier) {
        super(baseFileName, effect, powerType);
        this.potionEffectType = potionEffectType;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    @Override
    public String getSource() {
        return spawnSelfPotion(potionEffectType, duration, amplifier);
    }
}
