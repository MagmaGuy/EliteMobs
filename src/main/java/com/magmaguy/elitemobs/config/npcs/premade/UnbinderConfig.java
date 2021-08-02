package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class UnbinderConfig extends NPCsConfigFields {
    public UnbinderConfig() {
        super("unbinder",
                true,
                "Ulfric",
                "<Unbinder>",
                Villager.Profession.WEAPONSMITH,
                "em_adventurers_guild,296.5,81,253.5,90,0",
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
                true,
                3,
                NPCInteractions.NPCInteractionType.UNBINDER);
    }
}
