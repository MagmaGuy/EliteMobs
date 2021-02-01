package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;

import java.util.Arrays;

public class BarkeepConfig extends NPCsConfigFields {
    public BarkeepConfig() {
        super(
                "barkeep.yml",
                true,
                "Bartley",
                "<Barkeep>",
                "BUTCHER",
                "em_adventurers_guild,285.5,91,209.5,0,0",
                Arrays.asList(
                        "Need a drink?",
                        "Want a drink",
                        "Thirsty?",
                        "Howdy, partner."),
                Arrays.asList(
                        "Have one of our house specialties.",
                        "Special drinks won't find them\\nanywhere else.",
                        "One taste and will keep you\\ncoming back from more."),
                Arrays.asList(
                        "Come back anytime",
                        "Bottoms up!",
                        "Kampai!",
                        "Salut!",
                        "Brosd!",
                        "Salud!",
                        "À la vôtre !",
                        "Prost!",
                        "Santi!",
                        "Salute!",
                        "Saúde!",
                        "Cheers!",
                        "乾杯!"),
                false,
                true,
                3,
                true,
                "BAR"
        );
    }
}
