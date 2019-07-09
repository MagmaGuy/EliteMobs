package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackVacuumConfig extends PowersConfigFields {
    public AttackVacuumConfig() {
        super("attack_vacuum",
                true,
                "Vacuum",
                Material.LEAD.toString());
    }
}