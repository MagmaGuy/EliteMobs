package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackWebConfig extends PowersConfigFields {
    public AttackWebConfig() {
        super("attack_web",
                true,
                "Webbing",
                Material.COBWEB.toString());
    }
}