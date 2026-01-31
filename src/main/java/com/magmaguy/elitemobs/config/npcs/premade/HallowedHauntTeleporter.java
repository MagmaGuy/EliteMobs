package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class HallowedHauntTeleporter extends NPCsConfigFields {
    public HallowedHauntTeleporter() {
        super("hallowed_haunt_teleporter",
                true,
                "Alaric Greystone",
                "<Hallowed Haunt Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,291.5,92,295.5,180,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_hallowed_haunt_dynamic_dungeon.yml");
        setCustomModel("em_ag_alaricgreystone");
        setDisguise("custom:em_the_hallowed_haunt_alaric");
        setCustomDisguiseData("player em_the_hallowed_haunt_alaric setskin {\"uuid\":\"899defd5-a0c9-4fa1-ac81-c24d597a2553\",\"name\":\"Unknown\",\"textureProperties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTc1NDg4MjkzOTUzNiwKICAicHJvZmlsZUlkIiA6ICJkYjQwYmNjNWUzMDE0ZmZjOGVlOWQxNDU5MTcyYjdhNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJhWGUxOCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xODQ5NTI1NmZiZWViYjBkY2M3MTI4Mjg2MTljMzBjNTBmNjM1MGZjOWZlM2E4ZGQ4YjI5YWY2YmJkNTEyMDU0IgogICAgfQogIH0KfQ==\",\"signature\":\"FLLx/2pMyQYHZlHWR33WttxatUO9su4sUJ/NO0xFXLEla\"}]}");
    }
}
