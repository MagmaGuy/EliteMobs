package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class FireworksTeleporter extends NPCsConfigFields {
    public FireworksTeleporter() {
        super("fireworks_teleporter",
                true,
                "<g:#AA7A40:#BA8A50>Pyrotechnician Pete</g>",
                "<g:#9A6A30:#AA7A40><Fireworks></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,299.5,92,265.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp fireworks_lair.yml");
        setCustomModel("em_ag_pyrotechnicianpete");
        setSyncMovement(false);
    }
}
