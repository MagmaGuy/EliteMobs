package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class AttackWebConfig extends PowersConfigFields {
    public AttackWebConfig() {
        super("attack_web",
                true,
                Material.COBWEB.toString(),
                addScriptEntry("WebPlayer",
                        List.of("PlayerDamagedByEliteMobEvent"),
                        null,
                        List.of(Map.of(
                                "action", "PLACE_BLOCK",
                                "target", "DIRECT_TARGET",
                                "duration", 120,
                                "material", "COBWEB",
                                "conditions", Map.of("locationIsAir", true)
                        )),
                        Map.of("local", 60,
                                "global", 20)),
                PowerType.MISCELLANEOUS);
    }
}