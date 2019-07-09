package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackFireConfig extends PowersConfigFields {
    public AttackFireConfig() {
        super("attack_fire",
                true,
                "Immolate",
                Material.BLAZE_POWDER.toString());
    }
}