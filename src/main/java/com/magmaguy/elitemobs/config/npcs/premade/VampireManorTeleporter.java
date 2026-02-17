package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class VampireManorTeleporter extends NPCsConfigFields {
    public VampireManorTeleporter() {
        super("vampire_manor_teleporter",
                true,
                "<g:#6A2A2A:#7A3A3A>Blood Servant</g>",
                "<g:#5A1A1A:#6A2A2A><Vampire Manor></g>",
                Villager.Profession.BUTCHER,
                "em_adventurers_guild,298.5,92,288.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp vampire_manor_lair.yml");
        setCustomModel("em_ag_bloodservant");
        setSyncMovement(false);
    }
}
