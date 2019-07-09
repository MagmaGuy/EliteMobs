package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Particle;

public class AttackConfusingConfig extends PowersConfigFields {
    public AttackConfusingConfig() {
        super("attack_confusing",
                true,
                "Confusing",
                Particle.SPELL_MOB.toString());
    }
}
