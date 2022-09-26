package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.AttackFireball;
import org.bukkit.Material;

public class AttackFireballConfig extends PowersConfigFields {
    public AttackFireballConfig() {
        super("attack_fireball",
                true,
                Material.FIRE_CHARGE.toString(),
                AttackFireball.class,
                PowerType.OFFENSIVE);
    }
}