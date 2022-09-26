package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Particle;

import java.util.List;
import java.util.Map;

public class AttackConfusingConfig extends PowersConfigFields {
    public AttackConfusingConfig() {
        super("attack_confusing",
                true,
                Particle.SPELL_MOB.toString(),
                addScriptEntry("ConfusePlayer",
                        List.of("PlayerDamagedByEliteMobEvent"),
                        null,
                        List.of(Map.of(
                                "action", "POTION_EFFECT",
                                "duration", 60,
                                "target", "DIRECT_TARGET",
                                "amplifier", 0,
                                "potionEffectType", "nausea"
                        )),
                        null),
                PowerType.MISCELLANEOUS);
    }
}
