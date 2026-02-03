package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class PirateShipTeleporter extends NPCsConfigFields {
    public PirateShipTeleporter() {
        super("pirate_ship_teleporter",
                true,
                "<g:#4A7A9A:#5A8AAA>Sea Dog</g>",
                "<g:#3A6A8A:#4A7A9A><Pirate Ship></g>",
                Villager.Profession.FISHERMAN,
                "em_adventurers_guild,302.7,92,288.5,-90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp pirate_ship_minidungeon.yml");
        setCustomModel("em_ag_seadog");
        setSyncMovement(false);
    }
}
