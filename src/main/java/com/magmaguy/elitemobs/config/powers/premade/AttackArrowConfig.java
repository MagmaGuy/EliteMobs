package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.AttackArrow;
import org.bukkit.Material;

public class AttackArrowConfig extends PowersConfigFields {
    public AttackArrowConfig() {
        super("attack_arrow",
                true,
                Material.DISPENSER.toString(),
                AttackArrow.class,
                PowerType.OFFENSIVE);
    }
}
