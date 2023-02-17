package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class EnchanterConfig extends NPCsConfigFields {
    public EnchanterConfig() {
        super("enchanted",
                true,
                "Eden",
                "<Enchanter>",
                Villager.Profession.TOOLSMITH,
                "em_adventurers_guild,278.5,77,243.5,0,0",
                Arrays.asList(
                        "Need something enchanted?",
                        "Feeling lucky?",
                        "Got an item to improve?",
                        "Got enchanted books?"),
                Arrays.asList(
                        "Enchantment results are\\nnot guaranteed!",
                        "Don't complain if it fails!",
                        "Got an enchanted book?",
                        "Higher quality items are\\nriskier to enchant!"),
                Arrays.asList(
                        "Got what you wanted!",
                        "How's your karma?",
                        "If at first you fail,\\ntry and try again!"),
                true,
                3,
                NPCInteractions.NPCInteractionType.ENCHANTER);
    }
}

