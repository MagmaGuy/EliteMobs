package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class AttackWitherConfig extends PowersConfigFields {
    public AttackWitherConfig() {
        super("attack_wither",
                true,
                Material.WITHER_SKELETON_SKULL.toString(),
                addScriptEntry("WitherPlayer",
                        List.of("PlayerDamagedByEliteMobEvent"),
                        null,
                        List.of(Map.of(
                                "action", "POTION_EFFECT",
                                "duration", 60,
                                "target", "DIRECT_TARGET",
                                "amplifier", 0,
                                "potionEffectType", "wither"
                        )),
                        null),
                PowerType.OFFENSIVE);
    }
}
