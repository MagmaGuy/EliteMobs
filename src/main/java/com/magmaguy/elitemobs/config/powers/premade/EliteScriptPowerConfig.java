package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.scripts.EliteScript;

public class EliteScriptPowerConfig extends PowersConfigFields {
    public EliteScriptPowerConfig() {
        super("elite_script", true, null, EliteScript.class, PowerType.UNIQUE);
    }//todo probably delete
}
