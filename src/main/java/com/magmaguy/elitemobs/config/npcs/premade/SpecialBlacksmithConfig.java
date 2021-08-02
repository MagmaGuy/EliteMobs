package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class SpecialBlacksmithConfig extends NPCsConfigFields {
    public SpecialBlacksmithConfig() {
        super("special_blacksmith",
                true,
                "Grog",
                "<Special Blacksmith>",
                Villager.Profession.WEAPONSMITH,
                "em_adventurers_guild,282.5,93,258.5,-90,0",
                Arrays.asList(
                        "Need something?",
                        "Got anything good?",
                        "We have nothing but the best",
                        "Got anything worth selling?",
                        "We only buy elite mob gear."),
                Arrays.asList(
                        "We buy low and sell high.",
                        "No refunds.",
                        "Absolutely no refunds.",
                        "There are no refunds.",
                        "No taksies backsies.",
                        "Shop purchases are final."),
                Arrays.asList(
                        "Come back when you have\\nmore to spend",
                        "Don't get yourself killed,\\nwe want you to bring us\\nmore gear.",
                        "Next time buy something \\nmore expensive.",
                        "Don't forget, no refunds."),
                true,
                3,
                NPCInteractions.NPCInteractionType.CUSTOM_SHOP);
    }
}
