package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;
import java.util.Collections;

public class Guide1Config extends NPCsConfigFields {
    public Guide1Config() {
        super("guide_1.yml",
                true,
                "Casus",
                "<Guide>",
                Villager.Profession.NITWIT,
                "em_adventurers_guild,215.5,87,239.5,131,0",
                Arrays.asList(
                        "Need guidance?",
                        "Need a hint",
                        "Are you lost?",
                        "Need help?"),
                Arrays.asList(
                        "I know everything about\\nthis place!",
                        "Want to meet the other members\\nof the Adventurer's Guild?",
                        "Need to learn your way around\\nthis place?"),
                Arrays.asList(
                        "Come back anytime",
                        "I'll be here if you need me!",
                        "I'll be around!",
                        "See you later!"),
                true,
                5,
                NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER);
        super.setQuestFilenames(Collections.singletonList("ag_welcome_quest_1.yml"));
    }
}
