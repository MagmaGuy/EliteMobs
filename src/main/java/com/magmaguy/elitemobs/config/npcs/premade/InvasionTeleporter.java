package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class InvasionTeleporter extends NPCsConfigFields {
    public InvasionTeleporter() {
        super("invasion_teleporter",
                true,
                "<g:#2E8B57:#3CB371>Invasion Scout</g>",
                "<g:#228B22:#6B8E23><Invasion></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,295.5,92,304.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp invasion_minidungeon.yml");
        setCustomModel("em_ag_invasionscout");
        setSyncMovement(false);
    }
}
