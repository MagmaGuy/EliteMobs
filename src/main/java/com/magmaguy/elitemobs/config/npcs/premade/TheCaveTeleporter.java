package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheCaveTeleporter extends NPCsConfigFields {
    public TheCaveTeleporter() {
        super("the_cave_teleporter",
                true,
                "Village Mother Martha",
                "<The Cave Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,308.4,78,206.4,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_cave_sanctum.yml");
        setCustomModel("em_ag_villagemothermartha");
        setSyncMovement(false);
    }
}
