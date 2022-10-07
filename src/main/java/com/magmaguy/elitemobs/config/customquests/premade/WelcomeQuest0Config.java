package com.magmaguy.elitemobs.config.customquests.premade;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WelcomeQuest0Config extends CustomQuestsConfigFields {
    public WelcomeQuest0Config() {
        super("ag_welcome_quest_0.yml",
                true,
                Map.of("Objective1", Map.of("objectiveType", "DIALOG",
                        "filename", "guide_1.yml",
                        "npcName", "Odigos",
                        "location", "at the Adventurer's Guild",
                        "dialog", List.of(
                                "&8[&aCasus&8]&f Welcome to the Adventurer's Guild Hub, the hub for all things EliteMobs!",
                                "You can always get back here using &2/ag&f!",
                                "Talk to me again for a quest!"))),
                List.of("currencyAmount=50:amount=1:chance=1"),
                1,
                "&2Finding the AG!",
                List.of("Discover the Adventurer's Guild Hub using the command &2/ag &0or &2/adventurersguild !")
        );
        setQuestLockoutPermission();
        setTurnInNPC("guide_1.yml");
        setQuestAcceptDialog(Arrays.asList("&8[Odigos]&f Alright, make sure you use &2/ag &f or &2/adventurersguild&f to head off to the Adventurer's Guild Hub!",
                "Make sure you talk to Casus when you're there!"));
    }
}
