package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptCooldownsBlueprint;
import lombok.Getter;

public class ScriptCooldowns {
    @Getter
    private ScriptCooldownsBlueprint scriptCooldownsBlueprint;

    public ScriptCooldowns(ScriptCooldownsBlueprint scriptCooldownsBlueprint, ElitePower elitePower) {
        this.scriptCooldownsBlueprint = scriptCooldownsBlueprint;
        elitePower.setGlobalCooldownTime(scriptCooldownsBlueprint.getGlobalCooldown());
        elitePower.setPowerCooldownTime(scriptCooldownsBlueprint.getLocalCooldown());
    }
}
