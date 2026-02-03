package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class DarkCathedralTeleporter extends NPCsConfigFields {
    public DarkCathedralTeleporter() {
        super("dark_cathedral_teleporter",
                true,
                "<g:#4A4A6A:#5A5A7A>Dark Acolyte</g>",
                "<g:#3A3A5A:#4A4A6A><Dark Cathedral></g>",
                Villager.Profession.CLERIC,
                "em_adventurers_guild,289.5,92,258.5,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp dark_cathedral_lair.yml");
        setCustomModel("em_ag_darkacolyte");
        setSyncMovement(false);
    }
}
