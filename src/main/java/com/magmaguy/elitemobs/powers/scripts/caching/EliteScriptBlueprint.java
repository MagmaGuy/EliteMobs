package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.powers.scripts.loading.ScriptValueNormalizer;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class EliteScriptBlueprint {
    @Getter
    private static final HashMap<CustomConfigFields, EliteScriptBlueprint> blueprints = new HashMap();

    @Getter
    private final CustomConfigFields customConfigFields;
    @Getter
    private final ScriptActionsBlueprint scriptActionsBlueprint;
    @Getter
    private final ScriptConditionsBlueprint scriptConditionsBlueprint;
    @Getter
    private final ScriptZoneBlueprint scriptZoneBlueprint;
    @Getter
    private final ScriptEventsBlueprint scriptEventsBlueprint;
    @Getter
    private final ScriptCooldownsBlueprint scriptCooldownsBlueprint;
    private final String filename;
    @Getter
    private final String scriptName;


    public EliteScriptBlueprint(CustomConfigFields customConfigFields,
                                Map<String, Object> configurationValues,
                                String scriptName) {
        this.filename = customConfigFields.getFilename();
        this.scriptName = scriptName;
        this.customConfigFields = customConfigFields;
        this.scriptEventsBlueprint = new ScriptEventsBlueprint(configurationValues, scriptName, filename);
        this.scriptConditionsBlueprint = new ScriptConditionsBlueprint((Map<?, ?>) configurationValues.get("Conditions"), scriptName, filename);
        this.scriptZoneBlueprint = new ScriptZoneBlueprint(configurationValues, scriptName, filename);
        this.scriptActionsBlueprint = new ScriptActionsBlueprint(configurationValues, scriptName, filename);
        this.scriptCooldownsBlueprint = new ScriptCooldownsBlueprint((Map<?, ?>) configurationValues.get("Cooldowns"), scriptName, filename);
        blueprints.put(customConfigFields, this);
    }

    public EliteScriptBlueprint(CustomConfigFields customConfigFields,
                                ConfigurationSection configurationSection,
                                String scriptName) {
        this(customConfigFields, ScriptValueNormalizer.normalizeSection(configurationSection), scriptName);
    }

    public static void shutdown() {
        blueprints.clear();
    }

    public static List<EliteScriptBlueprint> parseBossScripts(ConfigurationSection configurationSection,
                                                              CustomConfigFields customConfigFields) {
        if (configurationSection == null) return Collections.emptyList();
        //Check for legacy scripts, old target formatting
        checkLegacyFormat(configurationSection, customConfigFields);
        return parseBossScripts(ScriptValueNormalizer.normalizeSection(configurationSection), customConfigFields);
    }

    public static List<EliteScriptBlueprint> parseBossScripts(Map<String, Object> configurationValues,
                                                              CustomConfigFields customConfigFields) {
        if (configurationValues == null || configurationValues.isEmpty()) return Collections.emptyList();
        List<EliteScriptBlueprint> eliteScripts = new ArrayList<>();
        for (Map.Entry<String, Object> entry : configurationValues.entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?> scriptValues)) {
                Logger.warn("Script " + entry.getKey() + " in " + customConfigFields.getFilename() + " is not a valid map!");
                continue;
            }
            eliteScripts.add(new EliteScriptBlueprint(customConfigFields, ScriptValueNormalizer.normalizeMap(scriptValues), entry.getKey()));
        }
        return eliteScripts;
    }

    private static void checkLegacyFormat(ConfigurationSection configurationSection,
                                          CustomConfigFields customConfigFields) {
        boolean updated = false;
        for (String key : configurationSection.getKeys(true)) {
            String[] splitKeys = key.split("\\.");
            if (splitKeys.length < 1) continue;
            String truncatedKey = splitKeys[splitKeys.length - 1];
            if (truncatedKey.equalsIgnoreCase("target") && configurationSection.get(key) instanceof String) {
                updated = true;
                String overallKey = key.replace(".target", "");
                new OldDataContainer(overallKey, configurationSection, overallKey);
            }
            //Actions are specific because they have "anonymous" keys since they are just in a list
            if (truncatedKey.equalsIgnoreCase("Actions")) {
                List<Map<?, ?>> immutableMapList = new ArrayList<>(configurationSection.getMapList(key));
                List<Map<?, ?>> mutableMapList = new ArrayList<>();
                immutableMapList.forEach(map -> mutableMapList.add(new HashMap<>(map)));
                for (Map<?, ?> map : mutableMapList) {
                    Map<?, ?> clonedMap = new HashMap<>(map);
                    for (Map.Entry<?, ?> entry : clonedMap.entrySet()) {
                        if (((String) entry.getKey()).equalsIgnoreCase("target") &&
                                !(entry.getValue() instanceof Map)) {
                            updated = true;
                            new OldDataContainer(map, customConfigFields.getFilename());
                        }
                    }
                }
                if (updated) configurationSection.set(key, mutableMapList);
            }
        }

        if (!updated) return;
        try {
            customConfigFields.getFileConfiguration().set("eliteScript", configurationSection.getValues(false));
            customConfigFields.getFileConfiguration().save(customConfigFields.getFile());
        } catch (Exception exception) {
            Logger.warn("Failed to update old script targets! Report this to the dev.");
        }
    }

    private static class OldDataContainer {
        String location = null, targetType = null, offset = null;
        List<String> locations = null;
        Double range = null;
        Boolean track = null;
        String newKey;
        String scriptName;

        private OldDataContainer(String overallKey, ConfigurationSection configurationSection, String scriptName) {
            processMapList(configurationSection.getConfigurationSection(overallKey).getValues(false).entrySet());
            this.newKey = overallKey + ".Target";
            this.scriptName = scriptName;
            if (targetType != null) updateKey("targetType", targetType, configurationSection);
            if (location != null) updateKey("location", location, configurationSection);
            if (locations != null) updateKey("locations", locations, configurationSection);
            if (range != null) updateKey("range", range, configurationSection);
            if (offset != null) updateKey("offset", offset, configurationSection);
            if (track != null) updateKey("track", track, configurationSection);
        }

        private OldDataContainer(Map<?, ?> map, String scriptName) {
            processMapList(((Map<String, Object>) map).entrySet());
            this.scriptName = scriptName;
            Map<String, Object> newMap = new HashMap<>();
            if (targetType != null) updateKey("targetType", targetType, (Map<String, Object>) map, newMap);
            if (location != null) updateKey("location", location, (Map<String, Object>) map, newMap);
            if (locations != null) updateKey("locations", locations, (Map<String, Object>) map, newMap);
            if (range != null) updateKey("range", range, (Map<String, Object>) map, newMap);
            if (offset != null) updateKey("offset", offset, (Map<String, Object>) map, newMap);
            if (track != null) updateKey("track", track, (Map<String, Object>) map, newMap);
            ((Map<String, Object>) map).put("Target", newMap);
        }

        private void updateKey(String key, Object value, ConfigurationSection configurationSection) {
            configurationSection.set(newKey + "." + key, value);
            configurationSection.set(newKey.replace(".Target", ".") + key, null);
            if (key.contains("targetType"))
                configurationSection.set(newKey.replace(".Target", ".") + key.replace("targetType", "target"), null);
        }

        private void updateKey(String key, Object value, Map<String, Object> oldMap, Map<String, Object> newMap) {
            oldMap.remove(key);
            if (key.equals("targetType")) oldMap.remove("target");
            newMap.put(key, value);
        }

        private void processMapList(Set<Map.Entry<String, Object>> entry) {
            for (Map.Entry entrySet : entry) {
                String key = (String) entrySet.getKey();
                processKeyAndValue(key, entrySet.getValue());
            }
        }


        protected void processKeyAndValue(String key, Object value) {
            switch (key.toLowerCase(Locale.ROOT)) {
                case "location" -> location = parseString(key, value, scriptName);
                case "locations" -> locations = parseStringList(key, value, scriptName);
                case "target" -> targetType = parseString(key, value, scriptName);
                case "range" -> range = parseDouble(key, value, scriptName);
                case "offset" -> offset = parseString(key, value, scriptName);
                case "track" -> track = parseBoolean(key, value, scriptName);
            }
        }
    }

}

