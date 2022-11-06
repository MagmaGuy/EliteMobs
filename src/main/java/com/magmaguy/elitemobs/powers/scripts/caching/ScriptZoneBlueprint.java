package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.enums.Filter;
import com.magmaguy.elitemobs.powers.scripts.enums.ShapeType;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.parseDouble;
import static com.magmaguy.elitemobs.utils.MapListInterpreter.parseEnum;

public class ScriptZoneBlueprint {

    private String scriptName;
    private String filename;
    @Getter
    private ScriptTargetsBlueprint scriptTargetsBlueprint;
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

    public ScriptZoneBlueprint(ConfigurationSection configurationSection, String scriptName, String filename) {
        this.scriptName = scriptName;
        this.filename = filename;
        ConfigurationSection subSection = configurationSection.getConfigurationSection("Zone");
        if (subSection == null) return;
        Map<?, ?> values = subSection.getValues(false);
        processMapList(values);
        this.scriptTargetsBlueprint = new ScriptTargetsBlueprint(values, scriptName);
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
            case "target", "locations", "range", "track", "location" -> {
            }
            default -> {
                new WarningMessage("Failed to read key " + key + " for script " + scriptName + " in file " + filename);
            }
        }
    }
}
