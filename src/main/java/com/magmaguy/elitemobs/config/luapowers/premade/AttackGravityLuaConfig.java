package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackGravityLuaConfig extends PlayerDamagedPotionLuaPowerConfig {
    public AttackGravityLuaConfig() {
        super("attack_gravity",
                Material.ELYTRA.toString(),
                PowersConfigFields.PowerType.OFFENSIVE,
                "levitation",
                60,
                0,
                0,
                0);
    }
}
