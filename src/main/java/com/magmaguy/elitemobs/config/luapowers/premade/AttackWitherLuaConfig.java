package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackWitherLuaConfig extends PlayerDamagedPotionLuaPowerConfig {
    public AttackWitherLuaConfig() {
        super("attack_wither",
                Material.WITHER_SKELETON_SKULL.toString(),
                PowersConfigFields.PowerType.OFFENSIVE,
                "wither",
                60,
                0,
                0,
                0);
    }
}
