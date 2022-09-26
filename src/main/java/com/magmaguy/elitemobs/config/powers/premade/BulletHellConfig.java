package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.BulletHell;

public class BulletHellConfig extends PowersConfigFields {
    public BulletHellConfig() {
        super("bullet_hell",
                true,
                null,
                BulletHell.class,
                PowerType.MAJOR_SKELETON);
    }
}
