package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;

import java.util.ArrayList;
import java.util.List;

public class FreeEnchantmentChallengesMetaPackage extends ContentPackagesConfigFields {
    public FreeEnchantmentChallengesMetaPackage() {
        super("free_enchantment_challenges",
                true,
                "&2Free Enchantment Challenges",
                new ArrayList<>(List.of("All free enchantment challenges!")),
                "https://nightbreak.io/plugin/elitemobs/#10-free-enchantment-challenge-sanctums",
                new ArrayList<>(List.of(
                        "enchantment_challenge_1_sanctum.yml",
                        "enchantment_challenge_2_sanctum.yml",
                        "enchantment_challenge_3_sanctum.yml",
                        "enchantment_challenge_4_sanctum.yml",
                        "enchantment_challenge_5_sanctum.yml",
                        "enchantment_challenge_6_sanctum.yml",
                        "enchantment_challenge_7_sanctum.yml",
                        "enchantment_challenge_8_sanctum.yml",
                        "enchantment_challenge_9_sanctum.yml",
                        "enchantment_challenge_10_sanctum.yml"
                        )));
        setSetupMenuDescription(List.of(
                "&2Free challenges that players can randomly",
                "&2get teleported to when enchanting elite items!"));
        setNightbreakSlug("free-enchantment-challenge-sanctums");
    }
}
