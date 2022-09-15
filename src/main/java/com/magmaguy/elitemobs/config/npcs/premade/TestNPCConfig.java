package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TestNPCConfig extends NPCsConfigFields {
    public TestNPCConfig() {
        super("test_npc",
                true,
                "Test NPC",
                "Testing!",
                Villager.Profession.NITWIT,
                null,
                List.of("Test Greeting!"),
                List.of("Test Dialog!"),
                List.of("Test Farewell!"),
                true,
                3,
                NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER);
        super.setQuestFilenames(List.of("test_quest.yml"));
    }
}
