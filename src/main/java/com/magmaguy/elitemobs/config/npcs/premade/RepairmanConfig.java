package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class RepairmanConfig extends NPCsConfigFields {
    public RepairmanConfig() {
        super("repairman_config",
                true,
                "Reggie",
                "<Repairman>",
                Villager.Profession.WEAPONSMITH,
                "em_adventurers_guild,278.5,81,263.5,-90,0",
                Arrays.asList(
                        "Get your items repaired!",
                        "Repairing items for scrap!",
                        "Turn that scrap into durability!",
                        "Need a repair?",
                        "Got damaged items?"),
                Arrays.asList(
                        "Best repairs in town!",
                        "I got your fix!",
                        "Need a fix?",
                        "I'll repair your items!",
                        "Need elite items repaired?"),
                Arrays.asList(
                        "Don't forget to do maintenance!",
                        "I'll be here if you need me!",
                        "Call me beep me if you wanna reach me,\\nif you wanna page me that's ok"),
                true,
                3,
                NPCInteractions.NPCInteractionType.REPAIRMAN);
    }
}
