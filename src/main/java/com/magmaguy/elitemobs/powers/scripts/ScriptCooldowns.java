package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptCooldownsBlueprint;
import lombok.Getter;

public class ScriptCooldowns {
    @Getter
    private final ScriptCooldownsBlueprint scriptCooldownsBlueprint;

    public ScriptCooldowns(ScriptCooldownsBlueprint scriptCooldownsBlueprint, ElitePower elitePower) {
        this.scriptCooldownsBlueprint = scriptCooldownsBlueprint;
        elitePower.setGlobalCooldownTime(scriptCooldownsBlueprint.getGlobalCooldown().getValue());
        elitePower.setPowerCooldownTime(scriptCooldownsBlueprint.getLocalCooldown().getValue());
    }
}
