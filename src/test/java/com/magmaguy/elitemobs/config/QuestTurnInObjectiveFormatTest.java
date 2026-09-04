package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QuestTurnInObjectiveFormatTest {

    @Test
    void defaultTurnInObjectiveHasNoStrayProgressDigit() {
        assertEquals("&aTalk to $npcName", QuestsConfig.DEFAULT_QUEST_TURN_IN_OBJECTIVE);
    }

    @Test
    void repairsTheLegacyValueAlreadySavedInQuestsYaml() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("questTurnInObjective", "&a2Talk to $npcName");

        QuestsConfig.repairStoredLegacyTurnInObjective(configuration);

        assertEquals("&aTalk to $npcName", configuration.getString("questTurnInObjective"));
    }

    @Test
    void repairsTheLegacyPrefixInAColorConvertedTranslation() {
        assertEquals(
                "§aHabla con $npcName",
                QuestsConfig.repairLegacyTurnInObjective("§a2Habla con $npcName"));
    }

    @Test
    void leavesCustomFormattingAndNullValuesAlone() {
        assertEquals(
                "&b2 visits remaining for $npcName",
                QuestsConfig.repairLegacyTurnInObjective("&b2 visits remaining for $npcName"));
        assertNull(QuestsConfig.repairLegacyTurnInObjective(null));

        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("questTurnInObjective", "&b2 visits remaining for $npcName");
        QuestsConfig.repairStoredLegacyTurnInObjective(configuration);
        assertEquals("&b2 visits remaining for $npcName", configuration.getString("questTurnInObjective"));
    }
}
