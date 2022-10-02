package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScriptActionsBlueprint {
    @Getter
    private List<ScriptActionBlueprint> scriptActionsBlueprintList = new ArrayList<>();

    public ScriptActionsBlueprint(ConfigurationSection configurationSection, String scriptName, String filename) {
        List<Map<?, ?>> values = configurationSection.getMapList("Actions");
        if (values.isEmpty()) {
            new WarningMessage("Script " + scriptName + " in file " + filename + " does not have any actions! You should probably fix this.");
            return;
        }
        values.forEach(entry -> scriptActionsBlueprintList.add(new ScriptActionBlueprint(entry, scriptName, filename)));
    }
}
