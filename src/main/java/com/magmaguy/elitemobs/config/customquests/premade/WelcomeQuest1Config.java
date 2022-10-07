package com.magmaguy.elitemobs.config.customquests.premade;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;

import java.util.*;

public class WelcomeQuest1Config extends CustomQuestsConfigFields {
    public WelcomeQuest1Config() {
        super("ag_welcome_quest_1.yml",
                true,
                new HashMap<>(),
                Arrays.asList(
                        "filename=magmaguys_toothpick.yml:amount=1:chance=1",
                        "currencyAmount=750:amount=1:chance=1"
                ),
                1,
                "&2Welcome to the AG!",
                Collections.singletonList("&aMeet the Adventurer's Guild NPCs!")
        );
        //Huff my shorts java, you and your 10 Map.of limit
        super.customObjectives.put("Objective1", Map.of("objectiveType", "DIALOG", "filename", "back_teleporter.yml", "npcName", "Hermes", "location", "by the gate", "dialog", List.of("&8[&aHermes&8]&f I take you back to where you were before! Just talk to me again!")));
        super.customObjectives.put("Objective2", Map.of("objectiveType", "DIALOG", "filename", "barkeep.yml", "npcName", "Bartley", "location", "in the main building", "dialog", List.of("&8[&aBartley&8]&f Need a stiff drink? My drinks pack an extra punch!")));
        super.customObjectives.put("Objective3", Map.of("objectiveType", "DIALOG", "filename", "blacksmith.yml", "npcName", "Greg", "location", "in the main building", "dialog", List.of("&8[&aGreg&8]&f Want to buy or sell something? I've got a little of everything!")));
        super.customObjectives.put("Objective4", Map.of("objectiveType", "DIALOG", "filename", "combat_instructor.yml", "npcName", "Charles", "location", "next to the main building", "dialog", List.of("&8[&aCharles&8]&f Want to learn about fighting elites? Just talk to me!")));
        super.customObjectives.put("Objective5", Map.of("objectiveType", "DIALOG", "filename", "enhancer_config.yml", "npcName", "Tim", "location", "under the main building", "dialog", List.of("&8[&aTim&8]&f Want to upgrade your Elite Items? Just get an upgrade orb from Sam over there!")));
        super.customObjectives.put("Objective6", Map.of("objectiveType", "DIALOG", "filename", "guild_attendant.yml", "npcName", "Gillian", "location", "in the main building", "dialog", List.of("&8[&aGillian&8]&f Do you seek power? Do you desire to reach new heights? Talk to me to unlock new guild ranks, and reach levels of power you never thought to be possible!")));
        super.customObjectives.put("Objective7", Map.of("objectiveType", "DIALOG", "filename", "quest_giver.yml", "npcName", "Qel'Thuzad", "location", "in the main building", "dialog", List.of("&8[&aQel'Thuzad&8]&f These Elite monsters, where do they keep coming from?", "Ah, never mind that for now, we are always in need of more people to assist us in slaying quests!", "Just talk to me to get one!")));
        super.customObjectives.put("Objective8", Map.of("objectiveType", "DIALOG", "filename", "refiner_config.yml", "npcName", "Gillian", "location", "under the main building", "dialog", List.of("&8[&aRalph&8]&f Get more out of your scrap by upgrading it! Just talk to me at any time.")));
        super.customObjectives.put("Objective9", Map.of("objectiveType", "DIALOG", "filename", "repairman_config.yml", "npcName", "Gillian", "location", "under the main building", "dialog", List.of("&8[&aReggie&8]&f Need your Elite items repaired? Just bring me some scrap, and I'll make them as good as new!", "Remember, the higher the level of your scrap, the better I can repair you Elite item!")));
        super.customObjectives.put("Objective10", Map.of("objectiveType", "DIALOG", "filename", "scrapper_config.yml", "npcName", "Kelly", "location", "under the main building", "dialog", List.of("&8[&aKelly&8]&f Got extra Elite items? Don't know what to do with them? Give them to me, and I'll turn them into scrap!", "You can use scrap to do lots of things, like repairing or upgrading Elite items!")));
        super.customObjectives.put("Objective11", Map.of("objectiveType", "DIALOG", "filename", "smelter.yml", "npcName", "Sam", "location", "under the main building", "dialog", List.of("&8[&aSam&8]&f Got extra scrap? I can refine that into Upgrade Orbs, which you can give to Tim to upgrade your Elite items with!")));
        super.customObjectives.put("Objective12", Map.of("objectiveType", "DIALOG", "filename", "special_blacksmith.yml", "npcName", "Grog:", "location", "in the main building", "dialog", List.of("&8[&aGrog&8]&f Want to buy special items? I've got just the thing!")));
        super.customObjectives.put("Objective13", Map.of("objectiveType", "DIALOG", "filename", "unbinder.yml", "npcName", "Ulfric:", "location", "under the main building", "dialog", List.of("&8[&aUlfric&8]&f If you bring me a very special and rare item, I can unbind your Elite items.", "You don't yet look prepared to take this challenge on, but you can make killing the Binder of Worlds your ultimate goal, if you dare.", "Objective2", Map.of("objectiveType", "DIALOG", "filename", "barkeep.yml", "npcName", "Bartley", "location", "in the main building", "dialog", List.of("&8[&aBartley&8]&f Need a stiff drink? My drinks pack an extra punch!")))));
        super.customObjectives.put("Objective3", Map.of("objectiveType", "DIALOG", "filename", "blacksmith.yml", "npcName", "Greg", "location", "in the main building", "dialog", List.of("&8[&aGreg&8]&f Want to buy or sell something? I've got a little of everything!")));
        super.customObjectives.put("Objective4", Map.of("objectiveType", "DIALOG", "filename", "combat_instructor.yml", "npcName", "Charles", "location", "next to the main building", "dialog", List.of("&8[&aCharles&8]&f Want to learn about fighting elites? Just talk to me!")));
        super.customObjectives.put("Objective5", Map.of("objectiveType", "DIALOG", "filename", "enhancer_config.yml", "npcName", "Tim", "location", "under the main building", "dialog", List.of("&8[&aTim&8]&f Want to upgrade your Elite Items? Just get an upgrade orb from Sam over there!")));
        super.customObjectives.put("Objective6", Map.of("objectiveType", "DIALOG", "filename", "guild_attendant.yml", "npcName", "Gillian", "location", "in the main building", "dialog", List.of("&8[&aGillian&8]&f Do you seek power? Do you desire to reach new heights? Talk to me to unlock new guild ranks, and reach levels of power you never thought to be possible!")));
        super.customObjectives.put("Objective7", Map.of("objectiveType", "DIALOG", "filename", "quest_giver.yml", "npcName", "Qel'Thuzad", "location", "in the main building", "dialog", List.of("&8[&aQel'Thuzad&8]&f These Elite monsters, where do they keep coming from?", "Ah, never mind that for now, we are always in need of more people to assist us in slaying quests!", "Just talk to me to get one!")));
        super.customObjectives.put("Objective8", Map.of("objectiveType", "DIALOG", "filename", "refiner_config.yml", "npcName", "Gillian", "location", "under the main building", "dialog", List.of("&8[&aRalph&8]&f Get more out of your scrap by upgrading it! Just talk to me at any time.")));
        super.customObjectives.put("Objective9", Map.of("objectiveType", "DIALOG", "filename", "repairman_config.yml", "npcName", "Gillian", "location", "under the main building", "dialog", List.of("&8[&aReggie&8]&f Need your Elite items repaired? Just bring me some scrap, and I'll make them as good as new!", "Remember, the higher the level of your scrap, the better I can repair you Elite item!")));
        super.customObjectives.put("Objective10", Map.of("objectiveType", "DIALOG", "filename", "scrapper_config.yml", "npcName", "Kelly", "location", "under the main building", "dialog", List.of("&8[&aKelly&8]&f Got extra Elite items? Don't know what to do with them? Give them to me, and I'll turn them into scrap!", "You can use scrap to do lots of things, like repairing or upgrading Elite items!")));
        super.customObjectives.put("Objective11", Map.of("objectiveType", "DIALOG", "filename", "smelter.yml", "npcName", "Sam", "location", "under the main building", "dialog", List.of("&8[&aSam&8]&f Got extra scrap? I can refine that into Upgrade Orbs, which you can give to Tim to upgrade your Elite items with!")));
        super.customObjectives.put("Objective12", Map.of("objectiveType", "DIALOG", "filename", "special_blacksmith.yml", "npcName", "Grog:", "location", "in the main building", "dialog", List.of("&8[&aGrog&8]&f Want to buy special items? I've got just the thing!")));
        super.customObjectives.put("Objective13", Map.of("objectiveType", "DIALOG", "filename", "unbinder.yml", "npcName", "Ulfric:", "location", "under the main building", "dialog", List.of("&8[&aUlfric&8]&f If you bring me a very special and rare item, I can unbind your Elite items.", "You don't yet look prepared to take this challenge on, but you can make killing the Binder of Worlds your ultimate goal, if you dare.")));
        setQuestLockoutPermission();
    }
}
