package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class AirshipTeleporter extends NPCsConfigFields {
    public AirshipTeleporter() {
        super("airship_teleporter",
                true,
                "<g:#6A9AB0:#7AAAC0>Sky Captain</g>",
                "<g:#5A8AA0:#6A9AB0><Airship></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,291.5,92,301.5,-90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_airship.yml");
        setCustomModel("em_ag_skycaptain");
        setSyncMovement(false);
    }
}
