package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.Taze;

public class TazeConfig extends PowersConfigFields {
    public TazeConfig() {
        super("taze", true, null, 60, 20, Taze.class,
                PowerType.OFFENSIVE);
    }
}
