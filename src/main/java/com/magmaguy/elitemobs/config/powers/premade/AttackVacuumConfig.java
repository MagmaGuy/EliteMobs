package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.AttackVacuum;
import org.bukkit.Material;

public class AttackVacuumConfig extends PowersConfigFields {
    public AttackVacuumConfig() {
        super("attack_vacuum",
                true,
                Material.LEAD.toString(),
                AttackVacuum.class,
                PowerType.OFFENSIVE);
    }
}