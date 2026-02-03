package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class NorthPoleTeleporter extends NPCsConfigFields {
    public NorthPoleTeleporter() {
        super("north_pole_teleporter",
                true,
                "<g:#8AB8C8:#A0D0E0>Frosty Guide</g>",
                "<g:#6A98A8:#8AB8C8><North Pole></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,289.5,92,277.5,180,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_north_pole.yml");
        setCustomModel("em_ag_frostyguide");
        setSyncMovement(false);
    }
}
