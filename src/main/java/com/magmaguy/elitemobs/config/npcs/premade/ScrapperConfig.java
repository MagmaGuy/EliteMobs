package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class ScrapperConfig extends NPCsConfigFields {
    public ScrapperConfig() {
        super("scrapper_config",
                true,
                "Kelly",
                "<Scrapper>",
                Villager.Profession.WEAPONSMITH,
                "em_adventurers_guild,292.5,81,263.5,90,0",
                Arrays.asList(
                        "Want to get rid of items?",
                        "Need to recycle items?",
                        "Looking to scrap?",
                        "It's scrap 'o clock!",
                        "Scrap time!"),
                Arrays.asList(
                        "Scrappy deals!",
                        "Get your scrap here!",
                        "75% chance to work!",
                        "There are no refunds.",
                        "Recycle, reuse, reduce!"),
                Arrays.asList(
                        "Come back when you have\\nmore to scrap",
                        "Don't forget to make that\\nscrap useful!",
                        "Happy scrapping!"),
                true,
                3,
                NPCInteractions.NPCInteractionType.SCRAPPER);
    }
}
