package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

/**
 * Slot Machine Operator NPC for the gambling system.
 */
public class SlotMachineOperatorConfig extends NPCsConfigFields {
    public SlotMachineOperatorConfig() {
        super("slot_machine_operator",
                true,
                "Spinner Sam",
                "<Slot Machine>",
                Villager.Profession.CLERIC,
                "em_adventurers_guild,293,81,227,135,0",
                new ArrayList<>(List.of(
                        "Spin the reels! Match three to win!",
                        "JACKPOT waiting for a winner!",
                        "Lucky 7s pay the most...",
                        "Give those reels a spin!")),
                new ArrayList<>(List.of(
                        "Three of a kind wins big!",
                        "Even two matches get you something.",
                        "The sevens are the key to riches!",
                        "Cherries, lemons, bells, bars, or sevens...")),
                new ArrayList<>(List.of(
                        "The reels are always spinning!",
                        "Come back for another spin!",
                        "Maybe the jackpot is next!",
                        "Luck be with you!")),
                true,
                3,
                NPCInteractions.NPCInteractionType.GAMBLING_SLOTS);
    }
}
