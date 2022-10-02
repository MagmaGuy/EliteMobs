package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.enums.Target;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class ScriptTargetsBlueprint {
    @Getter
    protected Target targetType;
    @Getter
    private String scriptName;
    @Getter
    private List<String> locations;
    @Getter
    private String location;
    @Getter
    private Vector offset = new Vector(0, 0, 0);
    @Getter
    private double range = 20;

    public ScriptTargetsBlueprint(Map<?, ?> entry, String scriptName) {
        this.scriptName = scriptName;
        processMapList(entry);
    }

    private void processMapList(Map<?, ?> entry) {
        for (Map.Entry entrySet : entry.entrySet()) {
            String key = (String) entrySet.getKey();
            processKeyAndValue(key, entrySet.getValue());
        }
    }

    protected void processKeyAndValue(String key, Object value) {
        switch (key.toLowerCase()) {
            case "location" -> location = parseString(key, value, scriptName);
            case "locations" -> locations = parseStringList(key, value, scriptName);
            case "target" -> targetType = parseEnum(key, value, Target.class, scriptName);
            case "range" -> range = parseDouble(key, value, scriptName);
            case "offset" -> offset = parseVector(key, value, scriptName);
        }
    }
}
