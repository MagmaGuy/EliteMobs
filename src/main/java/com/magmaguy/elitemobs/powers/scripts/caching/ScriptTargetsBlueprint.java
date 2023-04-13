package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.MemorySection;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class ScriptTargetsBlueprint {
    private final String filename;
    @Getter
    protected TargetType targetType = TargetType.SELF;
    @Getter
    private final String scriptName;
    @Getter
    private List<String> locations;
    @Getter
    private String location;
    @Getter
    private Vector offset = new Vector(0, 0, 0);
    @Getter
    private double range = 20;
    @Getter
    @Setter
    private boolean track = true;
    @Getter
    @Setter
    private double coverage = 1.0;
    @Getter
    private boolean isCustomCoverage = false;
    @Getter
    private ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint = null;

    public ScriptTargetsBlueprint(Map<?, ?> entry, String scriptName, String filename) {
        this.scriptName = scriptName;
        this.filename = filename;
        processMapList(entry);
        if (!isZoneTarget() && coverage < 1.0) {
                new WarningMessage("Coverage for script " + scriptName + " in file " + filename + " was less than 1.0 but the targetType is neither ZONE_FULL nor ZONE_BORDER! Coverage should only be used for ZONE_FULL or ZONE_BORDER");
                coverage = 1.0;
            }
    }

    public boolean isZoneTarget(){
        return targetType == TargetType.ZONE_FULL ||
                targetType == TargetType.ZONE_BORDER ||
                targetType == TargetType.INHERIT_SCRIPT_ZONE_FULL ||
                targetType == TargetType.INHERIT_SCRIPT_ZONE_BORDER;
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
            case "targettype" -> targetType = parseEnum(key, value, TargetType.class, scriptName);
            case "range" -> range = parseDouble(key, value, scriptName);
            case "offset" -> offset = parseVector(key, value, scriptName);
            case "track" -> track = parseBoolean(key, value, scriptName);
            case "coverage" -> {
                isCustomCoverage = true;
                coverage = parseDouble(key, value, scriptName);
            }
            case "relativeoffset" -> scriptRelativeVectorBlueprint = new ScriptRelativeVectorBlueprint(scriptName, filename, ((MemorySection) value).getValues(false));
        }
    }
}
