package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class AttackPoisonConfig extends PowersConfigFields {
    public AttackPoisonConfig() {
        super("attack_poison",
                true,
                Material.EMERALD.toString(),
                addScriptEntry("PoisonPlayer",
                        List.of("PlayerDamagedByEliteMobEvent"),
                        null,
                        List.of(Map.of(
                                "action", "POTION_EFFECT",
                                "duration", 60,
                                "target", "DIRECT_TARGET",
                                "amplifier", 0,
                                "potionEffectType", "poison"
                        )),
                        null),
                PowerType.OFFENSIVE);
    }
}