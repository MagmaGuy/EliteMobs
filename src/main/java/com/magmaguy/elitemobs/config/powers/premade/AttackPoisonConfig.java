package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackPoisonConfig extends PowersConfigFields {
    public AttackPoisonConfig() {
        super("attack_poison",
                true,
                "Poisonous",
                Material.EMERALD.toString());
    }
}