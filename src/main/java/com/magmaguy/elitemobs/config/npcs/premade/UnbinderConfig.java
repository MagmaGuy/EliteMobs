package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.enchantments.premade.UnbindConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;

import java.util.Arrays;

public class UnbinderConfig extends NPCsConfigFields {
    public UnbinderConfig(){
        super("unbinder.yml",
                true,
                "",
                "<Unbinder>",
                "WEAPONSMITH",
                "em_adventurers_guild,292.5,81,251.5,90,0",
                Arrays.asList(
                        "Greetings.",
                        "Well met.",
                        "Hail, friend.",
                        "Yes?",
                        "May I help you?"),
                Arrays.asList(
                        "I will unbind your items\\nfor an extremely rare unbind scroll.",
                        "I am the only one qualified\\nto unbind your goods.",
                        "Have an unbind scroll?"),
                Arrays.asList(
                        "Remember: Use unbind\\nscrolls wisely.",
                        "Safe travels, friend."),
                false,
                true,
                3,
                false,
                "UNBINDER");
    }
}
