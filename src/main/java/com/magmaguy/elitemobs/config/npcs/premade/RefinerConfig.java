package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class RefinerConfig extends NPCsConfigFields {
    public RefinerConfig() {
        super("refiner_config",
                true,
                "Ralph",
                "<Refiner>",
                Villager.Profession.WEAPONSMITH,
                "em_adventurers_guild,292.5,81,269.5,90,0",
                Arrays.asList(
                        "Want to uprade your scrap?",
                        "Got scrap?",
                        "Looking for low level scrap!",
                        "Greetings, got scrap?",
                        "I'll recycle your scrap!"),
                Arrays.asList(
                        "Need your scrap upgraded?",
                        "Upgrade your scrap here!",
                        "Upgrade 10 scrap to a higher level!",
                        "Make your scrap more useful!",
                        "Got low level scrap?"),
                Arrays.asList(
                        "Come back when you have\\nmore to low level scrap",
                        "Don't forget to make that\\nscrap useful!",
                        "Go get into a scrap!"),
                true,
                3,
                NPCInteractions.NPCInteractionType.REFINER);
    }
}
