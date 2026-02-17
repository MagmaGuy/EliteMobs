package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;

import java.util.ArrayList;
import java.util.List;

public class PremiumEnchantmentChallengesMetaPackage extends ContentPackagesConfigFields {
    public PremiumEnchantmentChallengesMetaPackage() {
        super("premium_enchantment_challenges",
                true,
                "&2Premium Enchantment Challenges",
                new ArrayList<>(List.of("All premium enchantment challenges!")),
                "https://nightbreak.io/plugin/elitemobs/#10-premium-enchantment-sanctums",
                new ArrayList<>(List.of(
                        "enchantment_challenge_11_sanctum.yml",
                        "enchantment_challenge_12_sanctum.yml",
                        "enchantment_challenge_13_sanctum.yml",
                        "enchantment_challenge_14_sanctum.yml",
                        "enchantment_challenge_15_sanctum.yml",
                        "enchantment_challenge_16_sanctum.yml",
                        "enchantment_challenge_17_sanctum.yml",
                        "enchantment_challenge_18_sanctum.yml",
                        "enchantment_challenge_19_sanctum.yml",
                        "enchantment_challenge_20_sanctum.yml"
                )));
        setSetupMenuDescription(List.of(
                "&2Free challenges that players can randomly",
                "&2get teleported to when enchanting elite items!",
                "&2Has custom models!"));
        setNightbreakSlug("premium-enchantment-sanctums");
    }
}
