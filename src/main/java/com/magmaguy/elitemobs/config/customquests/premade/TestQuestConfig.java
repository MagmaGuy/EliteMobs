package com.magmaguy.elitemobs.config.customquests.premade;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;

import java.util.ArrayList;
import java.util.Arrays;

public class TestQuestConfig extends CustomQuestsConfigFields {
    public TestQuestConfig() {
        super("test_quest",
                true,
                Arrays.asList("KILL_CUSTOM:filename=test_boss.yml:amount=1"),
                Arrays.asList("filename=magmaguys_toothpick.yml:amount=1:chance=1"),
                0,
                "Kill the Test Boss",
                "&cEnd the test boss' reign of terror!");
    }
}
