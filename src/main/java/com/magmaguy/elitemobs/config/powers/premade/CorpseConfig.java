package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class CorpseConfig extends PowersConfigFields {
    public CorpseConfig() {
        super("corpse",
                true,
                Material.BONE_BLOCK.toString(),
                addScriptEntry("SpawnBoneBlock",
                        List.of("EliteMobDeathEvent"),
                        null,
                        List.of(Map.of(
                                "action", "PLACE_BLOCK",
                                "target", "SELF",
                                "duration", 2400,
                                "material", "BONE_BLOCK",
                                "conditions", Map.of("locationIsAir", true))),
                        null),
                PowerType.MISCELLANEOUS);
    }
}