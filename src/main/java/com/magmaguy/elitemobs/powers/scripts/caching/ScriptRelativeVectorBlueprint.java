package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptFloat;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptVector;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.configuration.MemorySection;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ScriptRelativeVectorBlueprint {

    private final String scriptName;
    private final String scriptFilename;

    @Getter
    private ScriptTargetsBlueprint sourceTarget = null;
    @Getter
    private ScriptTargetsBlueprint destinationTarget = null;
    @Getter
    private boolean normalize = false;
    @Getter
    private ScriptFloat multiplier = new ScriptFloat(1.0f);
    @Getter
    private ScriptVector offset = new ScriptVector(new ScriptFloat(0), new ScriptFloat(0), new ScriptFloat(0));

    public ScriptRelativeVectorBlueprint(String scriptName, String scriptFilename, Map<String, ?> configurationValues) {
        this.scriptName = scriptName;
        this.scriptFilename = scriptFilename;
        configurationValues.entrySet().forEach(entry -> processKeyAndValue(entry.getKey(), entry.getValue()));
    }

    public ScriptRelativeVectorBlueprint(String scriptName, String scriptFilename, LinkedHashMap<String, ?> configurationValues) {
        this.scriptName = scriptName;
        this.scriptFilename = scriptFilename;
        configurationValues.entrySet().forEach(entry -> processKeyAndValue(entry.getKey(), entry.getValue()));
    }

    protected void processKeyAndValue(String key, Object value) {
        switch (key.toLowerCase(Locale.ROOT)) {
            case "sourcetarget" -> {
                if (value instanceof MemorySection)
                    sourceTarget = new ScriptTargetsBlueprint(((MemorySection) value).getValues(false), scriptName, scriptFilename);
                else
                    sourceTarget = new ScriptTargetsBlueprint((Map) value, scriptName, scriptFilename);
            }
            case "destinationtarget" -> {
                if (value instanceof MemorySection)
                    destinationTarget = new ScriptTargetsBlueprint(((MemorySection) value).getValues(false), scriptName, scriptFilename);
                else
                    destinationTarget = new ScriptTargetsBlueprint((Map) value, scriptName, scriptFilename);
            }
            case "multiplier" -> multiplier = MapListInterpreter.parseScriptFloat(key, value, scriptName);
            case "normalize" -> normalize = MapListInterpreter.parseBoolean(key, value, scriptName);
            case "offset" -> offset = MapListInterpreter.parseScriptVector(key, value, scriptName);
            default -> Logger.warn("Failed to read key " + key + " for script " + scriptName + " in " + scriptFilename);
        }
    }
}
