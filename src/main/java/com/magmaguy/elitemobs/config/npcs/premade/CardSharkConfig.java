package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

/**
 * Card Shark NPC for the Higher/Lower gambling game.
 */
public class CardSharkConfig extends NPCsConfigFields {
    public CardSharkConfig() {
        super("card_shark",
                true,
                "<g:#6A3A3A:#7A4A4A>Ace Morgan</g>",
                "<g:#5A2A2A:#6A3A3A><Card Shark></g>",
                Villager.Profession.NITWIT,
                "em_adventurers_guild,282.5,81,227.5,-135,0",
                new ArrayList<>(List.of(
                        "Higher or lower? How far can you go?",
                        "Risk it all or cash out smart...",
                        "The cards don't lie. Do you trust them?",
                        "A simple game with big rewards!")),
                new ArrayList<>(List.of(
                        "Each correct guess multiplies your winnings!",
                        "Cash out anytime, or push your luck...",
                        "One wrong guess and it's all gone!",
                        "How long can your streak last?")),
                new ArrayList<>(List.of(
                        "The cards will be waiting!",
                        "Trust your instincts!",
                        "May the odds ever be in your favor!",
                        "Higher... or lower... only the cards know.")),
                true,
                3,
                NPCInteractions.NPCInteractionType.GAMBLING_HIGHERLOWER);
    }
}
