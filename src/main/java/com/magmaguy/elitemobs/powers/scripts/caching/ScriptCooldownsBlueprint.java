package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public class ScriptCooldownsBlueprint {
    @Getter
    private int localCooldown = 0;
    @Getter
    private int globalCooldown = 0;

    public ScriptCooldownsBlueprint(ConfigurationSection configurationSection, String scriptName, String filename) {
        ConfigurationSection subSection = configurationSection.getConfigurationSection("Cooldowns");
        if (subSection == null) return;
        Map<String, Object> values = subSection.getValues(false);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            switch (entry.getKey().toLowerCase()) {
                case "local" ->
                        localCooldown = MapListInterpreter.parseInteger(entry.getKey(), entry.getValue(), scriptName);
                case "global" ->
                        globalCooldown = MapListInterpreter.parseInteger(entry.getKey(), entry.getValue(), scriptName);
                default ->
                        new WarningMessage("Failed to parse cooldown entry for script name " + scriptName + " in config file " + filename);
            }
        }
    }
}
