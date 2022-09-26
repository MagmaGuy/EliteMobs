package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.AttackPush;
import org.bukkit.Material;

public class AttackPushConfig extends PowersConfigFields {
    public AttackPushConfig() {
        super("attack_push",
                true,
                Material.PISTON.toString(),
                AttackPush.class,
                PowerType.OFFENSIVE);
    }
}
