package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackPoisonLuaConfig extends PlayerDamagedPotionLuaPowerConfig {
    public AttackPoisonLuaConfig() {
        super("attack_poison",
                Material.EMERALD.toString(),
                PowersConfigFields.PowerType.OFFENSIVE,
                "poison",
                60,
                0,
                0,
                0);
    }
}
