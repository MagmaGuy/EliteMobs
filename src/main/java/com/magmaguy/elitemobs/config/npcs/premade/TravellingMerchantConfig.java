package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;

import java.util.Arrays;

public class TravellingMerchantConfig extends NPCsConfigFields {
    public TravellingMerchantConfig() {
        super(
                "travelling_merchant.yml",
                true,
                "Jeeves",
                "<Travelling Merchant>",
                "LIBRARIAN",
                null,
                Arrays.asList(
                        "Got something to sell?",
                        "You called?",
                        "Huh?"),
                Arrays.asList(
                        "I'll buy it on the cheap!",
                        "Got something for me?",
                        "Time is money!"),
                Arrays.asList(
                        "Call me again anytime!",
                        "Was that all?",
                        "Safe travels!",
                        "Don't get yourself killed!",
                        "Good hunting!"),
                false,
                true,
                3,
                false,
                "SELL"
        );
        super.getAdditionalConfigOptions().put("timeout", 2);
    }
}
