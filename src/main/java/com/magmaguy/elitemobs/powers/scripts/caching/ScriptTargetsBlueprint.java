package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.enums.Target;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class ScriptTargetsBlueprint {
    private String filename;
    @Getter
    protected Target targetType = Target.SELF;
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
    @Getter
    private boolean track = true;
    @Getter
    private double coverage = 1.0;

    public ScriptTargetsBlueprint(Map<?, ?> entry, String scriptName, String filename) {
        this.scriptName = scriptName;
        this.filename = filename;
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
            case "targettype" -> targetType = parseEnum(key, value, Target.class, scriptName);
            case "range" -> range = parseDouble(key, value, scriptName);
            case "offset" -> offset = parseVector(key, value, scriptName);
            case "track" -> track = parseBoolean(key, value, scriptName);
            case "coverage" -> coverage = parseDouble(key,value,scriptName);
        }
    }
}
