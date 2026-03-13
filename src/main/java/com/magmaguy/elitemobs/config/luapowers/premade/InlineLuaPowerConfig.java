package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public abstract class InlineLuaPowerConfig extends LuaPowersConfigFields {

    private final String source;

    protected InlineLuaPowerConfig(String baseFileName,
                                   String effect,
                                   PowersConfigFields.PowerType powerType,
                                   String source) {
        super(baseFileName, effect, powerType);
        this.source = source;
    }

    @Override
    public String getSource() {
        return source;
    }
}
