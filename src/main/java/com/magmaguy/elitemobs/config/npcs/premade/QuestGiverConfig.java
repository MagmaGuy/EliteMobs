package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;

import java.util.Arrays;

public class QuestGiverConfig extends NPCsConfigFields {
    public QuestGiverConfig() {
        super("quest_giver.yml",
                true,
                "Qel'Thuzad",
                "<Quest Giver>",
                "FLETCHER",
                "em_adventurers_guild,278.5,91,215.5,0,0",
                Arrays.asList(
                        "Greetings, adventurer!\\nFancy a quest?",
                        "You! I've got a quest!",
                        "Feeling... adventurous?"),
                Arrays.asList(
                        "Complete guild quests\\nfor cool rewards!",
                        "Higher tier quests have\\nbetter rewards!",
                        "Higher tier quests make\\nyou hunt higher level mobs!",
                        "Want a harder challenge?\\nIncrease your guild rank!",
                        "Make sure you're well equipped\\nfor these quests!"),
                Arrays.asList(
                        "Safe travels, friend.",
                        "Happy hunting!",
                        "Live long and prosper!",
                        "Come back with your shield,\\n or on it.",
                        "Life before death!",
                        "Strength before weakness!",
                        "Journey before destination!"),
                false,
                true,
                3,
                true,
                "QUEST_GIVER"
        );
    }
}
