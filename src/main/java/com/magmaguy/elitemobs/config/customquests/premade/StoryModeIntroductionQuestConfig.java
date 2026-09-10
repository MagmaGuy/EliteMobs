package com.magmaguy.elitemobs.config.customquests.premade;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;

import java.util.List;
import java.util.Map;

public final class StoryModeIntroductionQuestConfig extends CustomQuestsConfigFields {
    public StoryModeIntroductionQuestConfig() {
        super("ag_story_mode_introduction.yml", true,
                Map.of("Objective1", Map.of("objectiveType", "DIALOG",
                        "filename", "story_dungeons_quest_giver.yml", "npcName", "Manager Wallitz",
                        "location", "on the lower floor of the Adventurer's Guild",
                        "dialog", List.of("&8[&aManager Wallitz&8]&f Casus says you have completed your class training. Good! You are ready for Story Mode.",
                                "Talk to me again to finish this introduction and see the story quests I have for you."))),
                List.of(), 1, "&2Your Story Begins",
                List.of("&aVisit Manager Wallitz at the Adventurer's Guild.",
                        "&aBegin your journey through the Story Mode dungeon quests."));
        setQuestAcceptPermission("elitequest.ag_adventurer_training.yml");
        setQuestLockoutPermission();
        setTurnInNPC("story_dungeons_quest_giver.yml");
        setQuestAcceptDialog(List.of("&8[&aCasus&8]&f With your Adventurer training complete, you are ready for the Story Mode dungeons.",
                "Speak to Manager Wallitz on the lower floor of the guild. He will guide you into the story.",
                "You can also take my Primis introduction. The two journeys are yours to choose between."));
        setQuestCompleteDialog(List.of("&8[&aManager Wallitz&8]&f Let us get you started. Speak to me again and choose your next Story Mode quest."));
    }
}
