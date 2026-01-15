package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

/**
 * Coin Flipper NPC for the gambling system.
 */
public class CoinFlipperConfig extends NPCsConfigFields {
    public CoinFlipperConfig() {
        super("coin_flipper",
                true,
                "Lucky Pete",
                "<Coin Flipper>",
                Villager.Profession.CARTOGRAPHER,
                "em_adventurers_guild,293,81,211,45,0",
                new ArrayList<>(List.of(
                        "Heads or tails? Simple as that!",
                        "Flip a coin, double your money!",
                        "Feeling lucky, adventurer?",
                        "A simple coin flip can change your fortune!")),
                new ArrayList<>(List.of(
                        "50/50 chance, pure luck!",
                        "Pick your side wisely...",
                        "The coin decides all.",
                        "Fortune favors the bold!")),
                new ArrayList<>(List.of(
                        "May fortune smile upon you!",
                        "Better luck next time!",
                        "The coin awaits your return!",
                        "Heads... or tails... which will it be?")),
                true,
                3,
                NPCInteractions.NPCInteractionType.GAMBLING_COINFLIP);
    }
}
