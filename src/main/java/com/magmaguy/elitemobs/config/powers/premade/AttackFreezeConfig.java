package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackFreezeConfig extends PowersConfigFields {
    public AttackFreezeConfig() {
        super("attack_freeze",
                true,
                "Cryomancer",
                Material.PACKED_ICE.toString());
    }
}