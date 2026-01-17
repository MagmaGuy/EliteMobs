package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

/**
 * Blackjack Dealer NPC for the gambling system.
 */
public class BlackjackDealerConfig extends NPCsConfigFields {
    public BlackjackDealerConfig() {
        super("blackjack_dealer",
                true,
                "Vincent",
                "<Blackjack Dealer>",
                Villager.Profession.LIBRARIAN,
                "em_adventurers_guild,282.5,81,211.5,-45,0",
                new ArrayList<>(List.of(
                        "Care to try your luck at the tables?",
                        "Blackjack! Get 21 to win big!",
                        "The house always wins... eventually.",
                        "Step right up, test your fortune!")),
                new ArrayList<>(List.of(
                        "Remember, getting closer to 21 is the goal.",
                        "Hit or stand? The choice is yours.",
                        "A blackjack pays extra!",
                        "Don't go over 21...")),
                new ArrayList<>(List.of(
                        "Good luck at the tables!",
                        "May the cards be in your favor!",
                        "Come back when you're feeling lucky!",
                        "The tables will be waiting...")),
                true,
                3,
                NPCInteractions.NPCInteractionType.GAMBLING_BLACKJACK);
    }
}
