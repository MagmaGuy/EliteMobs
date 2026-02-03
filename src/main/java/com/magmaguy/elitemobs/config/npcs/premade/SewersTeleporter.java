package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class SewersTeleporter extends NPCsConfigFields {
    public SewersTeleporter() {
        super("sewers_teleporter",
                true,
                "<g:#5A6A4A:#6A7A5A>Sewer Rat</g>",
                "<g:#4A5A3A:#5A6A4A><Sewers></g>",
                Villager.Profession.LEATHERWORKER,
                "em_adventurers_guild,297.5,92,259.5,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp sewers_minidungeon.yml");
        setCustomModel("em_ag_sewerrat");
        setSyncMovement(false);
    }
}
