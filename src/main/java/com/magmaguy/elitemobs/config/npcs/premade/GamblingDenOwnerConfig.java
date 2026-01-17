package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

/**
 * Gambling Den Owner NPC that displays house earnings and encourages gambling.
 */
public class GamblingDenOwnerConfig extends NPCsConfigFields {
    public GamblingDenOwnerConfig() {
        super("gambling_den_owner",
                true,
                "&6The Baron",
                "<Gambling Den Owner>",
                Villager.Profession.WEAPONSMITH,
                "em_adventurers_guild,286.5,81,231.5,-90,0",
                new ArrayList<>(List.of(
                        "Welcome to my humble establishment!",
                        "The house always wins, they say...",
                        "Care to test your luck today?",
                        "Fortune favors the bold!")),
                new ArrayList<>(List.of(
                        "My dealers are the best in the realm!",
                        "Blackjack, slots, cards... take your pick!",
                        "Don't worry about debt, we're... flexible.",
                        "The odds are fair, I promise!")),
                new ArrayList<>(List.of(
                        "Come back soon, I enjoy your... patronage.",
                        "May luck be on your side! Or not...",
                        "The tables will be waiting!",
                        "Remember: you can't win if you don't play!")),
                true,
                3,
                NPCInteractions.NPCInteractionType.CHAT);
    }
}
