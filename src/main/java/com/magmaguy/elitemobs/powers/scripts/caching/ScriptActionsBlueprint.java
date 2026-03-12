package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScriptActionsBlueprint {
    @Getter
    private final List<ScriptActionBlueprint> scriptActionsBlueprintList = new ArrayList<>();

    public ScriptActionsBlueprint(Map<String, Object> configurationValues, String scriptName, String filename) {
        Object rawActions = configurationValues.get("Actions");
        if (!(rawActions instanceof List<?> values) || values.isEmpty()) {
            if (!Boolean.TRUE.equals(configurationValues.get("__lua_synthetic"))) {
                Logger.warn("Script " + scriptName + " in file " + filename + " does not have any actions! You should probably fix this.");
            }
            return;
        }
        for (Object value : values) {
            if (!(value instanceof Map<?, ?> entry)) {
                Logger.warn("Script " + scriptName + " in file " + filename + " has an action entry that is not a valid map.");
                continue;
            }
            scriptActionsBlueprintList.add(new ScriptActionBlueprint(entry, scriptName, filename));
        }
    }
}
