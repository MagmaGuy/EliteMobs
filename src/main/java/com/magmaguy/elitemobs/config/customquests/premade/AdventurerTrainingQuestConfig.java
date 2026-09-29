package com.magmaguy.elitemobs.config.customquests.premade;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;

import java.util.List;
import java.util.Map;

public final class AdventurerTrainingQuestConfig extends CustomQuestsConfigFields {
    public AdventurerTrainingQuestConfig() {
        super("ag_adventurer_training.yml", true,
                Map.of("Objective1", Map.of("objectiveType", "DIALOG",
                                "filename", "class_trainer_adventurer.yml", "npcName", "Rowan, Adventurer Instructor",
                                "location", "inside the main building",
                                "dialog", List.of("&8[&aRowan&8]&f Ready to become an Adventurer and unlock your first EliteMobs class? Talk to me again to open your training menu.",
                                        "Select the Challenge Instructor. It costs one Elite Coin and takes place in the Wood League arena.",
                                        "Defeat the instructor to unlock the Adventurer starter class, then return to Casus for your equipment.")),
                        "Objective2", Map.of("objectiveType", "CLASS_UNLOCK", "class", "adventurer",
                                "filename", "class_trainer_adventurer.yml")),
                List.of("helmet", "chestplate", "leggings", "boots", "sword", "axe", "bow", "crossbow",
                                "trident", "scythe", "mace", "spear", "staff", "wand").stream()
                        .map(item -> "filename=ag_adventurer_" + item + ".yml:amount=1:chance=1").toList(),
                1, "&2Your First Class",
                List.of("&aTalk to Rowan, the Adventurer Instructor, and complete his trial.",
                        "&aReturn to Casus after unlocking the Adventurer class.",
                        "&aComplete this quest to earn level-one armor and one of every weapon type."));
        setQuestAcceptPermission("elitequest.ag_welcome_quest_1.yml");
        setQuestLockoutPermission();
        setTurnInNPC("guide_1.yml");
        setQuestAcceptDialog(List.of("&8[&aCasus&8]&f Now that you know your way around the guild, it is time to unlock your first EliteMobs class.",
                "Look for Rowan, the Adventurer Instructor, seated at a large table inside the main building, near Gillian, the Guild Attendant.",
                "His trial costs one Elite Coin. Once you have unlocked the Adventurer class, return to me to collect your equipment."));
        setQuestCompleteDialog(List.of("&8[&aCasus&8]&f Well done, Adventurer! Here is your armor and a weapon of every type.",
                "Try them all and see what suits you! Adventurers are proficient with every weapon type.",
                "Talk to me again for two new journeys: the Story Mode dungeons and the Primis expedition."));
    }
}
