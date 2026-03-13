package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackWeaknessLuaConfig extends PlayerDamagedPotionLuaPowerConfig {
    public AttackWeaknessLuaConfig() {
        super("attack_weakness",
                Material.TOTEM_OF_UNDYING.toString(),
                PowersConfigFields.PowerType.MISCELLANEOUS,
                "weakness",
                60,
                0,
                0,
                0);
    }
}
