package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Particle;

public class AttackConfusingLuaConfig extends PlayerDamagedPotionLuaPowerConfig {
    public AttackConfusingLuaConfig() {
        super("attack_confusing",
                Particle.WITCH.toString(),
                PowersConfigFields.PowerType.MISCELLANEOUS,
                "nausea",
                60,
                0,
                0,
                0);
    }
}
