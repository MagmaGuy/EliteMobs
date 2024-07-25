package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.MemorySection;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class ScriptTargetsBlueprint {
    private final String filename;
    @Getter
    private final String scriptName;
    @Getter
    protected TargetType targetType = TargetType.SELF;
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
            Logger.warn("Coverage for script " + scriptName + " in file " + filename + " was less than 1.0 but the targetType is neither ZONE_FULL nor ZONE_BORDER! Coverage should only be used for ZONE_FULL or ZONE_BORDER");
            coverage = 1.0;
        }
    }

    public boolean isZoneTarget() {
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
        switch (key.toLowerCase(Locale.ROOT)) {
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
            case "relativeoffset" -> {
                if (value instanceof MemorySection)
                    new ScriptRelativeVectorBlueprint(scriptName, filename, ((MemorySection) value).getValues(false));
                else if (value instanceof LinkedHashMap<?, ?>)
                    scriptRelativeVectorBlueprint = new ScriptRelativeVectorBlueprint(scriptName, filename, ((LinkedHashMap) value));
                else
                    Logger.warn("Failed to get valid format for relative offset in " + scriptName + " for file " + filename);
            }
        }
    }
}
