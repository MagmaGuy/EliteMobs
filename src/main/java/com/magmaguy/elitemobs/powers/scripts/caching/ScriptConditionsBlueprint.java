package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.enums.ConditionType;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScriptConditionsBlueprint {

    private final String scriptName;
    private final String filename;

    @Getter private Boolean isAlive = null;
    @Getter private Boolean runIfIsAliveIs = true;

    @Getter private List<String> hasTags = null;
    @Getter private Boolean runIfHasTagIs = true;

    @Getter private Boolean locationIsAir = null;
    @Getter private Boolean runIfLocationIsAirIs = true;

    @Getter private Boolean isOnFloor = null;
    @Getter private Boolean runIfIsOnFloorIs = true;

    @Getter private List<String> doesNotHaveTags = null;
    @Getter private Boolean runIfDoesNotHaveTagIs = true;

    @Getter private ScriptTargetsBlueprint scriptTargets = null;
    @Getter private Boolean runIfScriptTargetsIs = true;

    @Getter private Double randomChance = null;
    @Getter private Boolean runIfRandomChanceIs = true;

    @Getter @Setter private ConditionType conditionType = null;
    @Getter private Boolean runIfConditionTypeIs = true;

    @Getter @Setter private Integer targetCountLowerThan = null;
    @Getter private Boolean runIfTargetCountLowerThanIs = true;

    @Getter @Setter private Integer targetCountGreaterThan = null;
    @Getter private Boolean runIfTargetCountGreaterThanIs = true;

    @Getter @Setter private Material isStandingOnMaterial = null;
    @Getter private Boolean runIfIsStandingOnMaterialIs = true;

    // Process from a script
    public ScriptConditionsBlueprint(ConfigurationSection configurationSection, String scriptName, String filename) {
        this.scriptName = scriptName;
        this.filename = filename;

        if (configurationSection.get("Conditions") == null) return;
        Map<?, ?> values = ((ConfigurationSection) configurationSection.get("Conditions")).getValues(false);
        processMapList(values);

        if (scriptTargets == null) {
            scriptTargets = new ScriptTargetsBlueprint(new HashMap<>(), scriptName, filename);
        }
    }

    // Process from an action
    public ScriptConditionsBlueprint(Map<?, ?> values, String scriptName, String filename) {
        this.scriptName = scriptName;
        this.filename = filename;

        if (values != null) {
            processMapList(values);
        }

        if (scriptTargets == null) {
            scriptTargets = new ScriptTargetsBlueprint(new HashMap<>(), scriptName, filename);
        }
    }

    private void processKeyAndValue(String key, Object value) {
        switch (key.toLowerCase(Locale.ROOT)) {
            case "isalive" ->
                    isAlive = MapListInterpreter.parseBoolean(key, value, scriptName);
            case "runifaliveis" ->
                    runIfIsAliveIs = MapListInterpreter.parseBoolean(key, value, scriptName);

            case "hastags" ->
                    hasTags = MapListInterpreter.parseStringList(key, value, scriptName);
            case "runifhastagis" ->
                    runIfHasTagIs = MapListInterpreter.parseBoolean(key, value, scriptName);

            case "locationisair" ->
                    locationIsAir = MapListInterpreter.parseBoolean(key, value, scriptName);
            case "runiflocationisairis" ->
                    runIfLocationIsAirIs = MapListInterpreter.parseBoolean(key, value, scriptName);

            case "isonfloor" ->
                    isOnFloor = MapListInterpreter.parseBoolean(key, value, scriptName);
            case "runifisonflooris" ->
                    runIfIsOnFloorIs = MapListInterpreter.parseBoolean(key, value, scriptName);

            case "doesnothavetags" ->
                    doesNotHaveTags = MapListInterpreter.parseStringList(key, value, scriptName);
            case "runifdoesnothavetagis" ->
                    runIfDoesNotHaveTagIs = MapListInterpreter.parseBoolean(key, value, scriptName);

            case "randomchance" ->
                    randomChance = MapListInterpreter.parseDouble(key, value, scriptName);
            case "runifrandomchanceis" ->
                    runIfRandomChanceIs = MapListInterpreter.parseBoolean(key, value, scriptName);

            case "targetcountlowerthan" ->
                    targetCountLowerThan = MapListInterpreter.parseInteger(key, value, scriptName);
            case "runiftargetcountlowerthanis" ->
                    runIfTargetCountLowerThanIs = MapListInterpreter.parseBoolean(key, value, scriptName);

            case "targetcountgreaterthan" ->
                    targetCountGreaterThan = MapListInterpreter.parseInteger(key, value, scriptName);
            case "runiftargetcountgreaterthanis" ->
                    runIfTargetCountGreaterThanIs = MapListInterpreter.parseBoolean(key, value, scriptName);

            case "conditiontype" ->
                    conditionType = MapListInterpreter.parseEnum(key, value, ConditionType.class, scriptName);
            case "runifconditiontypeis" ->
                    runIfConditionTypeIs = MapListInterpreter.parseBoolean(key, value, scriptName);

            case "isstandingonmaterial" ->
                    isStandingOnMaterial = MapListInterpreter.parseEnum(key, value, Material.class, scriptName);
            case "runifisstandingonmaterialis" ->
                    runIfIsStandingOnMaterialIs = MapListInterpreter.parseBoolean(key, value, scriptName);

            case "target" -> {
                if (value instanceof MemorySection ms) {
                    value = ms.getValues(false);
                }
                scriptTargets = new ScriptTargetsBlueprint((Map<?, ?>) value, scriptName, filename);
            }

            default ->
                    Logger.warn("Failed to read key " + key + " for script " + scriptName);
        }
    }

    private void processMapList(Map<?, ?> entries) {
        for (Map.Entry<?, ?> entry : entries.entrySet()) {
            processKeyAndValue(entry.getKey().toString(), entry.getValue());
        }
    }
}
