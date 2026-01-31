package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class SteamworksTeleporter extends NPCsConfigFields {
    public SteamworksTeleporter() {
        super("steamworks_teleporter",
                true,
                "Steam Engineer",
                "<Steamworks Teleporter>",
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
