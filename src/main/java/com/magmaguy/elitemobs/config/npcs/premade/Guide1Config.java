package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Guide1Config extends NPCsConfigFields {
    public Guide1Config() {
        super("guide_1.yml",
                true,
                "Casus",
                "<Guide>",
                Villager.Profession.NITWIT,
                "em_adventurers_guild,213.5,87,239.5,126,0",
                new ArrayList<>(List.of(
                        "Need guidance?",
                        "Need a hint",
                        "Are you lost?",
                        "Need help?")),
                new ArrayList<>(List.of(
                        "I know everything about\\nthis place!",
                        "Want to meet the other members\\nof the Adventurer's Guild?",
                        "Need to learn your way around\\nthis place?")),
                new ArrayList<>(List.of(
                        "Come back anytime",
                        "I'll be here if you need me!",
                        "I'll be around!",
                        "See you later!")),
                true,
                5,
                NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER);
        super.setQuestFilenames(Collections.singletonList("ag_welcome_quest_1.yml"));
        setDisguise("custom:ag_guide_1");
        setCustomDisguiseData("player ag_guide_1 setskin {\"id\":\"e784faf0-86a5-4e8a-bb38-961f4250fc01\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY3NzE3MDYyODE3OCwKICAicHJvZmlsZUlkIiA6ICIwOTZkYWUzZWY1MmU0YWU4ODk3ODY2N2EyOGIwZWFhNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBYml5QWphIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU3YTViYmU1MzlmMmYyOGU3OGM0OTE1Y2IxYmE5N2EyY2YxNDUzOTIzNDQ0M2U3ZDZlZDZkMzdhYmFiYzEzMTUiCiAgICB9CiAgfQp9\",\"signature\":\"Jf/RqgTr38zW29YGlOUebXelzu4rZ/oZ8TgltaRi2JyOa+V3ZfN4VelBXM582J/0S/f4Zw3oOF7QCLtXl6jjBsu27VqUL/zJ2XGsqLe2xiQ9iJ01ZsV+sV89pm37kJ9IvkyJvRuE//Gej8PpAoaE6VMfCyQQloztNF/OIGlOn5KO8Ut/IFoS8w8k0q3My2K4Iz3rMl+ws4mN9qpOLEZfIGR2z77sQ188/ysZnZqg7i4l8Z/fQ02fiYYOj50l6rhbyD5nC01zakr9OWcXQ1EsKKwdl0UhYKKUgqnqdpn9Sy04RlwvqCmBwnelsFOWhI1SCrXIQ+cKpw6BgzGzGJOCXOOscnmNgIE3yIILoS/wl1DPMT2wuP7hxw5Xi59JEYrF6yUVOdjKbIATZr0uWHzjEW8G25jA9UWxciM4M/NNPBe5RlL3JVhRmu7fbR0kGtW5ydcQuX7Mtjrkj3VqhPlTl/9MpdZR8FzhhKhxvqVE1Rc0Yrsv7MyHtlhkbBnyxVmx+yFe3T+PpeiH3ji/wtWQ3NBjuZG/M6cYCa2zsd9PoPr8qayUTyW73V08oB0X/xClxjk+TPqf6triTO9uXEqaOpGJE/+hk+FTBhJ0ctDTfdoH/6gr0TyyAy61ZFkwvwSuvmStP5CduEGaOLWbn0cNB7n9guG/TKBuwjTZzVyvJZU=\"}],\"legacy\":false}");
    }
}
