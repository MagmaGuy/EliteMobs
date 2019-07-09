package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackWeaknessConfig extends PowersConfigFields {
    public AttackWeaknessConfig() {
        super("attack_weakness",
                true,
                "Weakening",
                Material.TOTEM_OF_UNDYING.toString());
    }
}