package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class YggdrasilTeleporter extends NPCsConfigFields {
    public YggdrasilTeleporter() {
        super("yggdrasil_teleporter",
                true,
                "<g:#5A8A5A:#6A9A6A>Norse Guide</g>",
                "<g:#4A7A4A:#5A8A5A><Yggdrasil></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,288.5,92,288.5,-90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp yggdrasil_realm.yml");
        setCustomModel("em_ag_norseguide");
        setSyncMovement(false);
    }
}
