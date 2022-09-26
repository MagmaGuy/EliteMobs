package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.Firestorm;

public class FirestormConfig extends PowersConfigFields {
    public FirestormConfig() {
        super("firestorm",
                true,
                null,
                Firestorm.class,
                PowerType.MAJOR_BLAZE);
    }
}
