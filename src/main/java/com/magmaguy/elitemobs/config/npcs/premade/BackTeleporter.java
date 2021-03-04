package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;

import java.util.Arrays;

public class BackTeleporter extends NPCsConfigFields {

    public BackTeleporter() {
        super("back_teleporter.yml",
                true,
                "Hermes",
                "<Transporter>",
                "FLETCHER",
                "em_adventurers_guild,213.5,88,232,0,0",
                Arrays.asList(
                        "Welcome to the\\nAdventurer's Guild Hub!"),
                Arrays.asList(
                        "Need to go back?"),
                Arrays.asList(
                        "Safe travels, friend."),
                false,
                true,
                3,
                false,
                "TELEPORT_BACK");
        super.getAdditionalConfigOptions().put("noPreviousLocationMessage", "&8[EliteMobs] &cCouldn't send you back to your previous location - no previous location found!");
    }

}
