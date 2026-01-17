package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class InvasionTeleporter extends NPCsConfigFields {
    public InvasionTeleporter() {
        super("invasion_teleporter",
                true,
                "Invasion Scout",
                "<Invasion Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,297.5,92,302.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp invasion_minidungeon.yml");
    }
}
