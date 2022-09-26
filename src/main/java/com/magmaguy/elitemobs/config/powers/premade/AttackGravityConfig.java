package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class AttackGravityConfig extends PowersConfigFields {
    public AttackGravityConfig() {
        super("attack_gravity",
                true,
                Material.ELYTRA.toString(),
                addScriptEntry("LevitatePlayer",
                        List.of("PlayerDamagedByEliteMobEvent"),
                        null,
                        List.of(Map.of(
                                "action", "POTION_EFFECT",
                                "duration", 60,
                                "target", "DIRECT_TARGET",
                                "amplifier", 0,
                                "potionEffectType", "levitation"
                        )),
                        null),
                PowerType.OFFENSIVE);
    }
}