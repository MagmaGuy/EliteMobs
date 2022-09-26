package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class AttackWeaknessConfig extends PowersConfigFields {
    public AttackWeaknessConfig() {
        super("attack_weakness",
                true,
                Material.TOTEM_OF_UNDYING.toString(),
                addScriptEntry("WeakenPlayer",
                        List.of("PlayerDamagedByEliteMobEvent"),
                        null,
                        List.of(Map.of(
                                "action", "POTION_EFFECT",
                                "duration", 60,
                                "target", "DIRECT_TARGET",
                                "amplifier", 0,
                                "potionEffectType", "weakness"
                        )),
                        null),
                PowerType.MISCELLANEOUS);
    }
}