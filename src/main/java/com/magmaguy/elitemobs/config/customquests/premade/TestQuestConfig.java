package com.magmaguy.elitemobs.config.customquests.premade;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;

import java.util.List;
import java.util.Map;

public class TestQuestConfig extends CustomQuestsConfigFields {
    public TestQuestConfig() {
        super("test_quest",
                true,
                Map.of("Objective1",Map.of("objectiveType", "KILL_CUSTOM",
                        "filename", "test_boss.yml",
                        "amount", 1)),
                List.of("filename=magmaguys_toothpick.yml:amount=1:chance=1"),
                0,
                "Kill the Test Boss",
                List.of("&cEnd the test boss' reign of terror!"));
    }
}
