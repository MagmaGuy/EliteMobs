package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Particle;

public class AttackBlindingConfig extends PowersConfigFields {
    public AttackBlindingConfig() {
        super("attack_blinding",
                true,
                "Blinding",
                Particle.SPELL_MOB.toString());
    }
}
