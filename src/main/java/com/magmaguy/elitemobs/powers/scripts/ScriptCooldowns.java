package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;

import java.util.Map;

public class ScriptCooldowns {
    @Getter
    private int localCooldown = 0;
    @Getter
    private int globalCooldown = 0;

    public ScriptCooldowns(Map<String, Object> values, String scriptName, ElitePower elitePower) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            switch (entry.getKey().toLowerCase()) {
                case "local" ->
                        localCooldown = MapListInterpreter.parseInteger(entry.getKey(), entry.getValue(), scriptName);
                case "global" ->
                        globalCooldown = MapListInterpreter.parseInteger(entry.getKey(), entry.getValue(), scriptName);
                default ->
                        new WarningMessage("Failed to parse cooldown entry for script name " + scriptName + " in config file " + elitePower.getPowersConfigFields().getFilename());
            }
        }
        elitePower.setGlobalCooldownTime(globalCooldown);
        elitePower.setPowerCooldownTime(localCooldown);
    }
}
