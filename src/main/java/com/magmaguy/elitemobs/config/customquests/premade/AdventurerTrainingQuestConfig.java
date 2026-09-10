package com.magmaguy.elitemobs.config.customquests.premade;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;

import java.util.List;
import java.util.Map;

public final class AdventurerTrainingQuestConfig extends CustomQuestsConfigFields {
    public AdventurerTrainingQuestConfig() {
        super("ag_adventurer_training.yml", true,
                Map.of("Objective1", Map.of("objectiveType", "DIALOG",
                                "filename", "class_trainer_adventurer.yml", "npcName", "Rowan, Adventurer Instructor",
                                "location", "beside Charles, near the main building",
                                "dialog", List.of("&8[&aRowan&8]&f Ready to become an Adventurer? Talk to me again to open your training menu.",
                                        "Choose my trial. It costs one coin and takes place in the Wood League arena.",
                                        "Defeat the instructor to unlock Adventurer, then return to Casus.")),
                        "Objective2", Map.of("objectiveType", "CLASS_UNLOCK", "class", "adventurer",
                                "filename", "class_trainer_adventurer.yml")),
                List.of("helmet", "chestplate", "leggings", "boots", "sword", "axe", "bow", "crossbow",
                                "trident", "scythe", "mace", "spear", "staff", "wand").stream()
                        .map(item -> "filename=ag_adventurer_" + item + ".yml:amount=1:chance=1").toList(),
                1, "&2Your First Class",
                List.of("&aTalk to Rowan, the Adventurer Instructor, and complete his trial.",
                        "&aReturn to Casus after unlocking Adventurer.",
                        "&aEarn level-1 armor and one of every weapon type."));
        setQuestAcceptPermission("elitequest.ag_welcome_quest_1.yml");
        setQuestLockoutPermission();
        setTurnInNPC("guide_1.yml");
        setQuestAcceptDialog(List.of("&8[&aCasus&8]&f Now that you know the guild, it is time to earn your first class.",
                "Find Rowan with the Adventurer Instructor title beside Charles. His trial costs one coin.",
                "Once you have unlocked Adventurer, come back for your equipment."));
        setQuestCompleteDialog(List.of("&8[&aCasus&8]&f Welcome to the Adventurers! Here is your armor and a weapon of every type.",
                "Try them all. Adventurers are proficient with every weapon!"));
    }
}
