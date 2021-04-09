package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;

import java.util.Arrays;

public class SmelterConfig extends NPCsConfigFields {
    public SmelterConfig(){
        super("smelter.yml",
                true,
                "Sam",
                "<Smelter>",
                "WEAPONSMITH",
                "em_adventurers_guild,276.5,77,243.5,0,0",
                Arrays.asList(
                        "Got scrap?",
                        "Got extra scrap?",
                        "I smell scrap on you!?",
                        "Please I need scrap!",
                        "Feed me scrap!"),
                Arrays.asList(
                        "I'll make your scrap useful!",
                        "Give me scrap and I'll\\ngive you upgrade orbs!",
                        "Scrap is useless, give it to me!"),
                Arrays.asList(
                        "Come back when you have\\nmore scrap",
                        "Off to the upgrader you go!"),
                false,
                true,
                3,
                false,
                "SMELTER"
        );
    }
}
