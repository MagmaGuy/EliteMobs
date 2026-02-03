package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class SteamworksTeleporter extends NPCsConfigFields {
    public SteamworksTeleporter() {
        super("steamworks_teleporter",
                true,
                "<g:#8A6A4A:#9A7A5A>Steam Engineer</g>",
                "<g:#7A5A3A:#8A6A4A><Steamworks></g>",
                Villager.Profession.TOOLSMITH,
                "em_adventurers_guild,297.5,92,267.5,-180,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp steamworks_lair.yml");
        setCustomModel("em_ag_steamengineer");
        setSyncMovement(false);
    }
}
