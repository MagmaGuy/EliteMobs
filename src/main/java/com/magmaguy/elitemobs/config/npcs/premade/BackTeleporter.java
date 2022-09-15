package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class BackTeleporter extends NPCsConfigFields {
    public BackTeleporter() {
        super("back_teleporter",
                true,
                "Hermes",
                "<Transporter>",
                Villager.Profession.FLETCHER,
                "em_adventurers_guild,213.5,88,232,0,0",
                List.of("Welcome to the\\nAdventurer's Guild Hub!"),
                List.of("Need to go back?"),
                List.of("Safe travels, friend."),
                true,
                3,
                NPCInteractions.NPCInteractionType.TELEPORT_BACK);
        setNoPreviousLocationMessage("&8[EliteMobs] &cCouldn't send you back to your previous location - no previous location found!");
    }
}
