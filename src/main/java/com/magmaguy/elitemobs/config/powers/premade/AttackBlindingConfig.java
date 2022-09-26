package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Particle;

import java.util.List;
import java.util.Map;

public class AttackBlindingConfig extends PowersConfigFields {
    public AttackBlindingConfig() {
        super("attack_blinding",
                true,
                Particle.SPELL_MOB.toString(),
                addScriptEntry("BlindPlayer",
                        List.of("PlayerDamagedByEliteMobEvent"),
                        null,
                        List.of(Map.of(
                                "action", "POTION_EFFECT",
                                "duration", 60,
                                "target", "DIRECT_TARGET",
                                "amplifier", 0,
                                "potionEffectType", "blindness"
                        )),
                        null),
                PowerType.MISCELLANEOUS);
    }
}
