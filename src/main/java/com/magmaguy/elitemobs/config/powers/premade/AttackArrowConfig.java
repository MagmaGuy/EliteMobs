package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackArrowConfig extends PowersConfigFields {
    public AttackArrowConfig() {
        super("attack_arrow",
                true,
                "Arrow",
                Material.DISPENSER.toString());
    }
}
