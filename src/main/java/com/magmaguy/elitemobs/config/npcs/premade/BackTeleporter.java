package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class BackTeleporter extends NPCsConfigFields {
    public BackTeleporter() {
        super("back_teleporter",
                true,
                "Hermes",
                "<Transporter>",
                Villager.Profession.FLETCHER,
                "em_adventurers_guild,213.5,88,232,0,0",
                Arrays.asList("Welcome to the\\nAdventurer's Guild Hub!"),
                Arrays.asList("Need to go back?"),
                Arrays.asList("Safe travels, friend."),
                true,
                3,
                NPCInteractions.NPCInteractionType.TELEPORT_BACK);
        setNoPreviousLocationMessage("&8[EliteMobs] &cCouldn't send you back to your previous location - no previous location found!");
    }
}
