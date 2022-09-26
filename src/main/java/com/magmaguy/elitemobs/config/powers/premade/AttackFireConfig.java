package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class AttackFireConfig extends PowersConfigFields {
    public AttackFireConfig() {
        super("attack_fire",
                true,
                Material.BLAZE_POWDER.toString(),
                addScriptEntry("SetOnFire",
                        List.of("PlayerDamagedByEliteMobEvent"),
                        null,
                        List.of(Map.of(
                                "action", "SET_ON_FIRE",
                                "duration", 60,
                                "target", "DIRECT_TARGET")),
                        null),
                PowerType.OFFENSIVE);

    }
}