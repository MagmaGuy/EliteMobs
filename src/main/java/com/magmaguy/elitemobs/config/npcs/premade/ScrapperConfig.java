package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;

import java.util.Arrays;

public class ScrapperConfig extends NPCsConfigFields {
    public ScrapperConfig() {
        super("scapper_config.yml",
                true,
                "Kelly",
                "<Scrapper>",
                "WEAPONSMITH",
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
                        "50% chance to work!",
                        "There are no refunds.",
                        "Recycle, reuse, reduce!"),
                Arrays.asList(
                        "Come back when you have\\nmore to scrap",
                        "Don't forget to make that\\nscrap useful!",
                        "Happy scrapping!"),
                false,
                true,
                3,
                false,
                "SCRAPPER"
        );
    }
}
