package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheBridgeTeleporter extends NPCsConfigFields {
    public TheBridgeTeleporter() {
        super("the_bridge_teleporter",
                true,
                "Old Dwarf Jotun",
                "<The Bridge Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,308.4,78,202.4,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_bridge_sanctum.yml");
        setCustomModel("em_ag_olddwarfjotun");
        setSyncMovement(false);
    }
}
