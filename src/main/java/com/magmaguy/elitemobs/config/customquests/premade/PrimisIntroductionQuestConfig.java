package com.magmaguy.elitemobs.config.customquests.premade;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;

import java.util.List;
import java.util.Map;

public final class PrimisIntroductionQuestConfig extends CustomQuestsConfigFields {
    public PrimisIntroductionQuestConfig() {
        super("ag_primis_introduction.yml", true,
                Map.of("Objective1", Map.of("objectiveType", "DIALOG",
                        "filename", "primis_captain.yml", "npcName", "Captain Squalus",
                        "location", "by the beached ship at the starting area in Primis",
                        "dialog", List.of("&8[&aCaptain Squalus&8]&f Casus sent you? We could use an Adventurer around here!",
                                "Talk to me again to finish this introduction. If you are new to our expedition, take my Shipwrecked! quest to begin."))),
                List.of(), 1, "&2An Expedition to Primis",
                List.of("&aTravel to Primis and speak to Captain Squalus near the starting area.",
                        "&aHe gives the first quest in the Primis main story, Shipwrecked!"));
        setQuestAcceptPermission("elitequest.ag_adventurer_training.yml");
        setQuestLockoutPermission();
        setTurnInNPC("primis_captain.yml");
        setQuestAcceptDialog(List.of("&8[&aCasus&8]&f You are ready to explore Primis. Travel there using the dungeon menu.",
                "Look for Captain Squalus beside the beached ship near the starting area. He begins the main story with Shipwrecked!",
                "This journey is available alongside my Story Mode introduction. You can pursue either one first."));
        setQuestCompleteDialog(List.of("&8[&aCaptain Squalus&8]&f Welcome to the expedition! Speak to me again to begin Shipwrecked! if you have not already taken it."));
    }
}
