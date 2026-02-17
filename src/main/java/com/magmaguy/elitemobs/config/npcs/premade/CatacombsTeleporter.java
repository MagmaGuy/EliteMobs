package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class CatacombsTeleporter extends NPCsConfigFields {
    public CatacombsTeleporter() {
        super("catacombs_teleporter",
                true,
                "<g:#6A6A6A:#7A7A7A>Crypt Keeper</g>",
                "<g:#5A5A5A:#6A6A6A><Catacombs></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,295.5,92,307.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp catacombs_lair.yml");
        setCustomModel("em_ag_cryptkeeper");
        setSyncMovement(false);
    }
}
