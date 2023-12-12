package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class StoryDungeonsQuestGiver extends NPCsConfigFields {
    public StoryDungeonsQuestGiver() {
        super("story_dungeons_quest_giver",
                true,
                "&eManager Wallitz",
                "&e<Story Mode Quests>",
                Villager.Profession.NITWIT,
                "em_adventurers_guild,303.5,78.0,212.5,-17.84,3.6",
                List.of("Salutations!", "Good day."),
                List.of("I'm a bit busy..."),
                List.of("Bye.", "Until next time."),
                true,
                4.0,
                NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER);
        setDisguise("custom:elitemobs_story_dungeons_quest_npc");
        setCustomDisguiseData("player elitemobs_story_dungeons_quest_npc setskin {\"id\":\"ddc4255f-68b3-43d2-b253-c39d2e726314\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY5OTU3NzAxMzU1NywKICAicHJvZmlsZUlkIiA6ICI1ZWQ4OTJiN2UyZGU0ZjYyYjIyNmFjNjQwZDA0YmJiOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJmcm9zdGVkc3Vuc2hpbmUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWM2YzhhYzZmOTAwN2Y5NWE5YWI5YTBmZDY3MWEzMjgyODdhNGI2ODNiZDMyYzgyNmZkOWE5YTQ3YjRiMmNlMSIKICAgIH0KICB9Cn0=\",\"signature\":\"WM1SCqPKcMHM1e9me8QLQ0vs0EOda4aa57/Qo5vl7oBITM0LdbcO2BehBGrY+y3HfPjymwv8cyGA3JxP9jNeDzUQTECvjMsCOx9P0RteSjtRfZ4N/n2noDxv70et8BUOFs4p/bOZeEpFhu+pq3kdgfukwerCJCe4EJKCdwSdP9M1xZXHGp33rIHcQAGw5hsGRA77RVBFPs1iS0z7tMRbZYBOh99Rs6Da+fGOzK2QcReZSQBntP9xtDY2wXxIM71ePvf4mNAZ+FO8FosaXkJNKvRr7GorRLrMehgSOK0Veft/m5iZj8ozDAhhiGCn1qB82vg03qq3kyFCS48krQiT3dcTN+PvkbM9N84D6Xp4+nRm4jHHPpJcZeg3SkyTOpUOLAww18eSEMoJ2N0Fc3eR6HSMZacI5iYleNWFqnENk3Wpu2+MFRKDQk5NJJNRjDzwWSxIzW9jywg9pL2y7yqZddeCecIQSrFF+mVP2ruDjBSaf5r7oHnhMi0EtjM0JUBTTFrEeXjJjKoyLzFwjyVoymDm9DB27al2Vlcp4lidhl37rwadxRIgZPighVkJ74dcB1uCm9IeQv9LENaRHPqzhTW8C6V2dCat+jYFiCHTq23qIgIi6OpLL3ZRMoLtEKC6fV8v84sPZZ04eidXBRbirGiI9cBebqZSqcUZjslFTkA=\"}],\"legacy\":false}");
        setQuestFilenames(List.of(
                "story_dungeons_quest_1_undead_attack.yml",
                "story_dungeons_quest_2_broiled.yml",
                "story_dungeons_quest_3_the_soulweaver.yml",
                "story_dungeons_quest_4_bridged.yml",
                "story_dungeons_quest_5_the_underground.yml"
                        //,"story_dungeons_quest_6_the_monarch.yml"
                        //,"story_dungeons_quest_7_down_below.yml"
                        //,"story_dungeons_quest_8_deep_down.yml"
                        //,"story_dungeons_quest_9_nether_vacation.yml"
                        //,"story_dungeons_quest_10_the_void_bell.yml"
                ));
    }
}
