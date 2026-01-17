package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class AirshipTeleporter extends NPCsConfigFields {
    public AirshipTeleporter() {
        super("airship_teleporter",
                true,
                "Sky Captain",
                "<Airship Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,283.5,92,297.5,180,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp airship_minidungeon.yml");
    }
}
