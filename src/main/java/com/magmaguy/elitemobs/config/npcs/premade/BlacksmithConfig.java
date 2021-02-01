package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;

import java.util.Arrays;

public class BlacksmithConfig extends NPCsConfigFields {
    public BlacksmithConfig() {
        super(
                "blacksmith.yml",
                true,
                "Greg",
                "<Blacksmith>",
                "TOOLSMITH",
                "em_adventurers_guild,285.5,93,261.5,179,0",
                Arrays.asList(
                        "Welcome to our shop!",
                        "Sell your goods here!",
                        "Fresh goods, just for you!",
                        "Got something to sell?",
                        "Want to buy something good?",
                        "Fresh goods every time!"),
                Arrays.asList(
                        "Higher level mobs drop\\nhigher value items!",
                        "Items with lots of \\nenchantments are worth more!",
                        "Higher level mobs drop\\nbetter items!",
                        "Higher level mobs have\\na higher chance of dropping loot!",
                        "Elite mobs are attracted to\\ngood armor, the better the\\narmor the higher their level!",
                        "Some items have special\\npotion effects!",
                        "Some items have unique\\neffects!",
                        "The hunter enchantment\\nattracts elite mobs to your\\nlocation!",
                        "Special weapons and armor\\ndropped by elite mobs can\\nbe sold here!",
                        "Higher guild ranks will\\nincrease the quality of the\\nloot from elite mobs!"),
                Arrays.asList(
                        "Thank you for your business!",
                        "Come back soon!",
                        "Come back any time!",
                        "Recommend this shop to your\\nfriends!"),
                false,
                true,
                3,
                false,
                "PROCEDURALLY_GENERATED_SHOP"
        );
    }
}
