package com.magmaguy.elitemobs.config.npcs;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NPCsConfigFieldsTest {

    @Test
    void missingScriptsDefaultToEmptyList() {
        NPCsConfigFields fields = processWithScripts(null);

        assertTrue(fields.getScripts().isEmpty());
    }

    @Test
    void emptyScriptsRemainEmptyList() {
        NPCsConfigFields fields = processWithScripts(List.of());

        assertTrue(fields.getScripts().isEmpty());
    }

    @Test
    void populatedScriptsAreParsedByFilename() {
        NPCsConfigFields fields = processWithScripts(List.of("wave.lua", "idle.lua"));

        assertEquals(List.of("wave.lua", "idle.lua"), fields.getScripts());
    }

    private NPCsConfigFields processWithScripts(List<String> scripts) {
        NPCsConfigFields fields = new NPCsConfigFields("test_npc.yml", true);
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        if (scripts != null) yamlConfiguration.set("scripts", scripts);
        fields.setFileConfiguration(yamlConfiguration);
        fields.processConfigFields();
        return fields;
    }
}
