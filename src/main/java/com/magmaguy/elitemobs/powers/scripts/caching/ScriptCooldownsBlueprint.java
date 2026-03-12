package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptInteger;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;

import java.util.Locale;
import java.util.Map;

public class ScriptCooldownsBlueprint {
    @Getter
    private ScriptInteger localCooldown = new ScriptInteger(0);
    @Getter
    private ScriptInteger globalCooldown = new ScriptInteger(0);

    public ScriptCooldownsBlueprint(Map<?, ?> values, String scriptName, String filename) {
        if (values == null) return;
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            String key = entry.getKey().toString();
            switch (key.toLowerCase(Locale.ROOT)) {
                case "local" ->
                        localCooldown = MapListInterpreter.parseScriptInteger(key, entry.getValue(), scriptName);
                case "global" ->
                        globalCooldown = MapListInterpreter.parseScriptInteger(key, entry.getValue(), scriptName);
                default ->
                        Logger.warn("Failed to parse cooldown entry for script name " + scriptName + " in config file " + filename);
            }
        }
    }
}
