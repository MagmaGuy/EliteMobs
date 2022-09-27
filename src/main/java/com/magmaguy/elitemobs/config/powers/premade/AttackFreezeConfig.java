package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class AttackFreezeConfig extends PowersConfigFields {

    public AttackFreezeConfig() {
        super("attack_freeze",
                true,
                Material.PACKED_ICE.toString(),
                addScriptEntry("FreezePlayer",
                        List.of("PlayerDamagedByEliteMobEvent"),
                        null,
                        List.of(Map.of(
                                        "action", "POTION_EFFECT",
                                        "duration", 60,
                                        "target", "DIRECT_TARGET",
                                        "amplifier", 5,
                                        "potionEffectType", "slowness"),
                                Map.of(
                                        "action", "PLACE_BLOCK",
                                        "target", "DIRECT_TARGET",
                                        "duration", 60,
                                        "material", "PACKED_ICE",
                                        "conditions", Map.of("locationIsAir", true)),
                                Map.of(
                                        "action", "VISUAL_FREEZE",
                                        "target", "DIRECT_TARGET",
                                        "repeatEvery", 1,
                                        "times", 60)),
                        Map.of("local", 300,
                                "global", 60)),
                PowerType.OFFENSIVE);
    }
}