package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Particle;

public class AttackBlindingLuaConfig extends PlayerDamagedPotionLuaPowerConfig {
    public AttackBlindingLuaConfig() {
        super("attack_blinding",
                Particle.WITCH.toString(),
                PowersConfigFields.PowerType.MISCELLANEOUS,
                "blindness",
                60,
                0,
                0,
                0);
    }
}
