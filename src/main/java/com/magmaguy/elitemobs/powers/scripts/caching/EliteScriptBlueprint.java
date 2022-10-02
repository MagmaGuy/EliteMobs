package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class EliteScriptBlueprint {
    @Getter
    private static HashMap<CustomConfigFields, EliteScriptBlueprint> blueprints = new HashMap();

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
                                ConfigurationSection configurationSection,
                                String scriptName) {
        this.filename = customConfigFields.getFilename();
        this.scriptName = configurationSection.getName();
        this.customConfigFields = customConfigFields;
        this.scriptEventsBlueprint = new ScriptEventsBlueprint(configurationSection, scriptName, filename);
        this.scriptConditionsBlueprint = new ScriptConditionsBlueprint(configurationSection, scriptName, filename);
        this.scriptZoneBlueprint = new ScriptZoneBlueprint(configurationSection, scriptName, filename);
        this.scriptActionsBlueprint = new ScriptActionsBlueprint(configurationSection, scriptName, filename);
        this.scriptCooldownsBlueprint = new ScriptCooldownsBlueprint(configurationSection, scriptName, filename);
        blueprints.put(customConfigFields, this);
    }

    public static List<EliteScriptBlueprint> parseBossScripts(ConfigurationSection configurationSection,
                                                              CustomConfigFields customConfigFields) {
        if (configurationSection == null) return Collections.emptyList();
        List<EliteScriptBlueprint> eliteScripts = new ArrayList<>();
        Map<String, EliteScriptBlueprint> permanentMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : configurationSection.getValues(false).entrySet()) {
            EliteScriptBlueprint eliteScript = new EliteScriptBlueprint(
                    customConfigFields, configurationSection.getConfigurationSection(entry.getKey()), entry.getKey());
            eliteScripts.add(eliteScript);
            permanentMap.put(entry.getKey(), eliteScript);
        }
        return eliteScripts;
    }
}
