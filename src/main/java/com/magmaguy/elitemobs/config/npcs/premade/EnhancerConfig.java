package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class EnhancerConfig extends NPCsConfigFields {
    public EnhancerConfig(){
        super("enhancer_config",
                true,
                "Tim",
                "<Enhancer>",
                Villager.Profession.WEAPONSMITH,
                "em_adventurers_guild,282.5,77,242.5,0,0",
                Arrays.asList(
                        "Want to uprade yer items?",
                        "Got upgrade orbs?",
                        "Get yer items upgraded here!",
                        "Ahoy!",
                        "Ye got scrap son?"),
                Arrays.asList(
                        "Yer items are lousy,\\n get an upgrade here!",
                        "Upgrade yer items here!",
                        "Ye got an upgrade orb?",
                        "That sword looks awful,\\nget it upgraded here!",
                        "Need an upgrade?"),
                Arrays.asList(
                        "Don't embarrass yourself out there!",
                        "Come back when you have more orbs!",
                        "Go get more orbs!"),
                true,
                3,
                NPCInteractions.NPCInteractionType.ENHANCER);
    }
}
