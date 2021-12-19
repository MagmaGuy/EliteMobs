package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;
import java.util.Collections;

public class Guide0Config extends NPCsConfigFields {
    public Guide0Config() {
        super("guide_0.yml",
                true,
                "Dux",
                "<Guide>",
                Villager.Profession.NITWIT,
                "null",
                Arrays.asList(
                        "Heard about the AG?",
                        "Been to the AG?",
                        "Heard of the Adventurer's Guild?",
                        "Know about the Adventurer's Guild?"),
                Arrays.asList(
                        "Heard about the AG?",
                        "Been to the AG?",
                        "Heard of the Adventurer's Guild?",
                        "Know about the Adventurer's Guild?"),
                Arrays.asList(
                        "Come back anytime",
                        "I'll be here if you need me!",
                        "I'll be around!",
                        "See you later!"),
                true,
                5,
                NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER);
        super.setQuestFilenames(Collections.singletonList("ag_welcome_quest_0.yml"));
    }
}
