package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class KnightsCastleTeleporter extends NPCsConfigFields {
    public KnightsCastleTeleporter() {
        super("knights_castle_teleporter",
                true,
                "<g:#7A8A9A:#8A9AAA>Castle Keeper</g>",
                "<g:#6A7A8A:#7A8A9A><Knight's Castle></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,299.5,92,261.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp knights_castle_lair.yml");
        setCustomModel("em_ag_castlekeeper");
        setSyncMovement(false);
    }
}
