package com.magmaguy.elitemobs.config.customquests;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CustomQuestsConfigFieldsTest {

    @Test
    void restoresTranslationStrippedObjectiveDefaults() {
        Map<String, Object> objectiveValues = new HashMap<>();
        objectiveValues.put("objectiveType", "DIALOG");
        objectiveValues.put("filename", "scrapper_config.yml");

        Map<String, Object> defaultObjectiveValues = new HashMap<>();
        defaultObjectiveValues.put("objectiveType", "DIALOG");
        defaultObjectiveValues.put("filename", "scrapper_config.yml");
        defaultObjectiveValues.put("npcName", "Kelly");
        defaultObjectiveValues.put("location", "under the main building");
        defaultObjectiveValues.put("dialog", List.of("Got extra Elite items?"));
        defaultObjectiveValues.put("amount", 3);

        Map<String, Object> restoredObjectiveValues = CustomQuestsConfigFields.restoreTranslatedObjectiveDefaults(
                objectiveValues, defaultObjectiveValues);

        assertEquals("Kelly", restoredObjectiveValues.get("npcName"));
        assertEquals("under the main building", restoredObjectiveValues.get("location"));
        assertEquals(List.of("Got extra Elite items?"), restoredObjectiveValues.get("dialog"));
        assertFalse(restoredObjectiveValues.containsKey("amount"));
        assertFalse(objectiveValues.containsKey("npcName"));
    }

    @Test
    void keepsConfiguredTranslatedObjectiveValues() {
        Map<String, Object> objectiveValues = new HashMap<>();
        objectiveValues.put("npcName", "Custom Kelly");

        Map<String, Object> defaultObjectiveValues = new HashMap<>();
        defaultObjectiveValues.put("npcName", "Kelly");
        defaultObjectiveValues.put("location", "under the main building");

        Map<String, Object> restoredObjectiveValues = CustomQuestsConfigFields.restoreTranslatedObjectiveDefaults(
                objectiveValues, defaultObjectiveValues);

        assertEquals("Custom Kelly", restoredObjectiveValues.get("npcName"));
        assertEquals("under the main building", restoredObjectiveValues.get("location"));
    }
}
