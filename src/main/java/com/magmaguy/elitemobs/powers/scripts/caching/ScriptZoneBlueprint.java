package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.enums.Filter;
import com.magmaguy.elitemobs.powers.scripts.enums.ShapeType;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import java.util.HashMap;
import java.util.Map;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class ScriptZoneBlueprint {

    private String scriptName;
    private String filename;
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
    private double height = 1;
    @Getter
    private Filter filter = Filter.PLAYER;
    @Getter
    private double radius = 5;
    @Getter
    private double borderRadius = 1;
    @Getter
    private double pointRadius = 0.5;
    @Getter
    private double pitchRotation = 0D;
    @Getter
    private double yawRotation = 0D;
    @Getter
    private double pitchPreRotation = 0D;
    @Getter
    private double yawPreRotation = 0D;
    @Getter
    private int animationDuration = 0;
    @Getter
    private double x = 0;
    @Getter
    private double y = 0;
    @Getter
    private double z = 0;
    @Getter
    private double xBorder = 0;
    @Getter
    private double yBorder = 0;
    @Getter
    private double zBorder = 0;
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
        switch (key.toLowerCase()) {
            case "shape" -> shapeTypeEnum = parseEnum(key, value, ShapeType.class, scriptName);
            case "filter" -> filter = parseEnum(key, value, Filter.class, scriptName);
            case "height" -> height = parseDouble(key, value, scriptName);
            case "radius" -> radius = parseDouble(key, value, scriptName);
            case "borderradius" -> borderRadius = parseDouble(key, value, scriptName);
            case "pointradius" -> pointRadius = parseDouble(key, value, scriptName);
            case "pitchrotation" -> pitchRotation = parseDouble(key, value, scriptName);
            case "pitchprerotation" -> pitchPreRotation = parseDouble(key, value, scriptName);
            case "yawrotation" -> yawRotation = parseDouble(key, value, scriptName);
            case "yawprerotation" -> yawPreRotation = parseDouble(key, value, scriptName);
            case "target" ->
                    target = new ScriptTargetsBlueprint(((MemorySection) value).getValues(false), scriptName, filename);
            case "finaltarget" ->
                    finalTarget = new ScriptTargetsBlueprint(((MemorySection) value).getValues(false), scriptName, filename);
            case "target2" ->
                    target2 = new ScriptTargetsBlueprint(((MemorySection) value).getValues(false), scriptName, filename);
            case "finaltarget2" ->
                    finalTarget2 = new ScriptTargetsBlueprint(((MemorySection) value).getValues(false), scriptName, filename);
            case "animationduration" -> animationDuration = parseInteger(key, value, scriptName);
            case "ignoressolidblocks" -> ignoresSolidBlocks = parseBoolean(key, value, scriptName);
            case "x" -> x = parseDouble(key, value, scriptName);
            case "y" -> y = parseDouble(key, value, scriptName);
            case "z" -> z = parseDouble(key, value, scriptName);
            case "xborder" -> xBorder = parseDouble(key, value, scriptName);
            case "yborder" -> yBorder = parseDouble(key, value, scriptName);
            case "zborder" -> zBorder = parseDouble(key, value, scriptName);
            default -> {
                new WarningMessage("Failed to read key " + key + " for script " + scriptName + " in file " + filename);
            }
        }
    }
}
