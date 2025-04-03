package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.enums.Filter;
import com.magmaguy.elitemobs.powers.scripts.enums.ShapeType;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptFloat;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptInteger;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class ScriptZoneBlueprint {

    private final String scriptName;
    private final String filename;
    @Getter
    private ScriptTargetsBlueprint target = null;
    @Getter
    private ScriptTargetsBlueprint finalTarget = null;
    @Getter
    private ScriptTargetsBlueprint target2 = null;
    @Getter
    private ScriptTargetsBlueprint finalTarget2 = null;
    @Getter
    private ShapeType shapeTypeEnum = ShapeType.CYLINDER;
    @Getter
    private ScriptFloat height = new ScriptFloat(1);
    @Getter
    private Filter filter = Filter.PLAYER;
    @Getter
    private ScriptFloat radius = new ScriptFloat(5);
    @Getter
    private ScriptFloat borderRadius = new ScriptFloat(1);
    @Getter
    private ScriptFloat pointRadius = new ScriptFloat(0.5f);
    @Getter
    private ScriptFloat pitchRotation = new ScriptFloat(0);
    @Getter
    private ScriptFloat yawRotation = new ScriptFloat(0);
    @Getter
    private ScriptFloat pitchPreRotation = new ScriptFloat(0);
    @Getter
    private ScriptFloat yawPreRotation = new ScriptFloat(0);
    @Getter
    private ScriptInteger animationDuration = new ScriptInteger(0);
    @Getter
    private ScriptFloat x = new ScriptFloat(0);
    @Getter
    private ScriptFloat y = new ScriptFloat(0);
    @Getter
    private ScriptFloat z = new ScriptFloat(0);
    @Getter
    private ScriptFloat xBorder = new ScriptFloat(0);
    @Getter
    private ScriptFloat yBorder = new ScriptFloat(0);
    @Getter
    private ScriptFloat zBorder = new ScriptFloat(0);
    @Getter
    private boolean ignoresSolidBlocks = true;

    public ScriptZoneBlueprint(ConfigurationSection configurationSection, String scriptName, String filename) {
        this.scriptName = scriptName;
        this.filename = filename;
        ConfigurationSection subSection = configurationSection.getConfigurationSection("Zone");
        if (subSection == null) return;
        Map<?, ?> values = subSection.getValues(false);
        processMapList(values);
        if (target == null) target = new ScriptTargetsBlueprint(new HashMap<>(), scriptName, filename);
    }

    //Start processing entries
    private void processMapList(Map<?, ?> entry) {
        for (Map.Entry entrySet : entry.entrySet()) {
            String key = (String) entrySet.getKey();
            processKeyAndValue(key, entrySet.getValue());
        }
    }

    //Process entries
    protected void processKeyAndValue(String key, Object value) {
        switch (key.toLowerCase(Locale.ROOT)) {
            case "shape" -> shapeTypeEnum = parseEnum(key, value, ShapeType.class, scriptName);
            case "filter" -> filter = parseEnum(key, value, Filter.class, scriptName);
            case "height" -> height = parseScriptFloat(key, value, scriptName);
            case "radius" -> radius = parseScriptFloat(key, value, scriptName);
            case "borderradius" -> borderRadius = parseScriptFloat(key, value, scriptName);
            case "pointradius" -> pointRadius = parseScriptFloat(key, value, scriptName);
            case "pitchrotation" -> pitchRotation = parseScriptFloat(key, value, scriptName);
            case "pitchprerotation" -> pitchPreRotation = parseScriptFloat(key, value, scriptName);
            case "yawrotation" -> yawRotation = parseScriptFloat(key, value, scriptName);
            case "yawprerotation" -> yawPreRotation = parseScriptFloat(key, value, scriptName);
            case "target" ->
                    target = new ScriptTargetsBlueprint(((MemorySection) value).getValues(false), scriptName, filename);
            case "finaltarget" ->
                    finalTarget = new ScriptTargetsBlueprint(((MemorySection) value).getValues(false), scriptName, filename);
            case "target2" ->
                    target2 = new ScriptTargetsBlueprint(((MemorySection) value).getValues(false), scriptName, filename);
            case "finaltarget2" ->
                    finalTarget2 = new ScriptTargetsBlueprint(((MemorySection) value).getValues(false), scriptName, filename);
            case "animationduration" -> animationDuration = parseScriptInteger(key, value, scriptName);
            case "ignoressolidblocks" -> ignoresSolidBlocks = parseBoolean(key, value, scriptName);
            case "x" -> x = parseScriptFloat(key, value, scriptName);
            case "y" -> y = parseScriptFloat(key, value, scriptName);
            case "z" -> z = parseScriptFloat(key, value, scriptName);
            case "xborder" -> xBorder = parseScriptFloat(key, value, scriptName);
            case "yborder" -> yBorder = parseScriptFloat(key, value, scriptName);
            case "zborder" -> zBorder = parseScriptFloat(key, value, scriptName);
            default -> {
                Logger.warn("Failed to read key " + key + " for script " + scriptName + " in file " + filename);
            }
        }
    }
}
