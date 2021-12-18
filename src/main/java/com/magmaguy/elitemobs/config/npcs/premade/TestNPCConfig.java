package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class TestNPCConfig extends NPCsConfigFields {
    public TestNPCConfig() {
        super("test_npc",
                true,
                 "Test NPC",
                "Testing!",
                Villager.Profession.NITWIT,
                null,
                Arrays.asList("Test Greeting!"),
                Arrays.asList("Test Dialog!"),
                Arrays.asList("Test Farewell!"),
                true,
                3,
                NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER);
        super.setQuestFilenames(Arrays.asList("test_quest.yml"));
    }
}
