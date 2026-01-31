package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

public class BlacksmithConfig extends NPCsConfigFields {
    public BlacksmithConfig() {
        super("blacksmith",
                true,
                "Greg",
                "<Blacksmith>",
                Villager.Profession.TOOLSMITH,
                "em_adventurers_guild,293.5,78,276.5,-180,0",
                new ArrayList<>(List.of(
                        "Welcome to our shop!",
                        "Sell your goods here!",
                        "Fresh goods, just for you!",
                        "Got something to sell?",
                        "Want to buy something good?",
                        "Fresh goods every time!")),
                new ArrayList<>(List.of(
                        "Higher level mobs drop\\nhigher value items!",
                        "Items with lots of \\nenchantments are worth more!",
                        "Higher level mobs drop\\nbetter items!",
                        "Higher level mobs have\\na higher chance of dropping loot!",
                        "Elite mobs are attracted to\\ngood armor, the better the\\narmor the higher their level!",
                        "Some items have special\\npotion effects!",
                        "Some items have unique\\neffects!",
                        "The hunter enchantment\\nattracts elite mobs to your\\nlocation!",
                        "Special weapons and armor\\ndropped by elite mobs can\\nbe sold here!",
                        "Higher guild ranks will\\nincrease the quality of the\\nloot from elite mobs!")),
                new ArrayList<>(List.of(
                        "Thank you for your business!",
                        "Come back soon!",
                        "Come back any time!",
                        "Recommend this shop to your\\nfriends!")),
                true,
                3,
                NPCInteractions.NPCInteractionType.PROCEDURALLY_GENERATED_SHOP);
        setDisguise("custom:ag_blacksmith");
        setCustomDisguiseData("player ag_blacksmith setskin {\"id\":\"5fbb022e-b62e-47ef-aeb5-a98fbc7ff936\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY3NjEyMzkyMTU3MCwKICAicHJvZmlsZUlkIiA6ICJhYTA3ZjM2Mjk0NTM0YzYwODQzMjI4NzAzZTBlMjE3OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJfU2FrdXlhX0l6YXlvaV8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWM0YTdhYTQyMzVhYjZjOTI2OTU4MTg5YjhiZDUwNTJkODExNmUyZDAzNGJhZGU2OWIyMzBiZTgxN2M3ZjI4MiIKICAgIH0KICB9Cn0=\",\"signature\":\"uZS2bAkz4f07TcXlZtSDK0ZPuUk/+H3PS5dr80WuDyQEN5FKPSEP2rSrVg8LHpVm5Gs2fwtkzUVG+MUloCR/7F/98RyLwzP5xyzt8+DELONi5iPfxLsYLG0VsIQCUH67sZGm4A9/iOyBTAkbr9aKUdwjJvyVnaT+Bj2dslzJqXaw9cO3gRQxlweeq9VL1weMFbldPEi12K4tS6KMYfYjfVXvCxHXpKby/AbXI/huNyK1T4BBIHrzq4TbpRD7xnIUMTLYf4ARnowsvoF9cNzbyTFrkCpH87wdGFbPyyH49h6mKLyTLUPESOHAxtwwhudsyi34zlVgMuzkvY6AqcjCZHnmELBm7/2QVEEZlk6lMFO8dI0pcF3fdQKrlLQBg/gNjUNdzU3rvgNs56P8a8RCRIvx/ExMMFEIUtIDPVZBIToSn7lwkLxwZJKS9Lk2fmVD5Vgs2v4Vn74zhxQYHYziwcpMThu1uDynEA2FNlkS9O9nd0dSSe1e55uMPWYMkgQNTLusRrIU53DpFTRMjMySubteielH9Xbk5PjmTGlBpqeDuA80XNOgGSO84uXDrFrdgnHkiAO9nKyCzOY+95RrPdIoUQkUps3mj5gIrmBF5mGIzF+QWgkpMeSx78WCGEGxPHBHl9ZzpM5iuU+iRGCBWSnJA1Og1JnLooIT6eAEFm4=\"}],\"legacy\":false}");
    }
}
