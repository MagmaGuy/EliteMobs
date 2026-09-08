package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

public class CombatInstructorConfig extends NPCsConfigFields {
    public CombatInstructorConfig() {
        super("combat_instructor",
                true,
                "<g:#8A4A4A:#9A5A5A>Charles</g>",
                "<g:#7A3A3A:#8A4A4A><Combat Instructor></g>",
                Villager.Profession.NITWIT,
                "em_adventurers_guild,299,91,215,0,0",
                new ArrayList<>(List.of(
                        "Want to learn about combat?",
                        "Need a combat lesson?",
                        "Want to know more about combat?",
                        "Ready to fight Elite Mobs?")),
                new ArrayList<>(List.of(
                        "Damage builds threat.\\nBosses pursue whoever\\nhas the most.",
                        "Taunts force a boss\\nto face the tank\\nfor a short time.",
                        "Give your tank a moment\\nto gather the enemies\\nbefore opening fire.",
                        "Red ground is a warning.\\nMove before the attack\\nlands!",
                        "Your combat level and\\nweapon skill both matter.\\nTrain the weapons you use.",
                        "Class weapons deal\\n10% more damage.\\nCheck your class menu.",
                        "Press F twice to move.\\nF + left-click: Signature.\\nF + right-click: Utility.",
                        "Health and class energy\\nappear above your hotbar.\\nWatch both in a fight.",
                        "Mana refills over time.\\nBerserkers build Fury\\nby dealing and taking hits.",
                        "Paladins recover faster\\nnear elites. Clerics need\\nother players nearby.",
                        "Rangers regain Focus\\nfaster after five seconds\\nwithout taking damage.",
                        "Wands seek elites first.\\nWalls and other creatures\\ncan block their shots.",
                        "Staff fireballs burst\\non impact. Aim at\\nclustered enemies.",
                        "Bring food into dungeons.\\nEating takes time;\\nfind a safe opening.",
                        "Use /em class to inspect\\nyour abilities and\\nnext specialization.",
                        "Boss loot has a level.\\nTrain your skills to\\nuse stronger equipment.",
                        "The enchanter improves\\nyour equipment with\\nelite enchanted books.",
                        "Repair damaged equipment\\nbefore your next dungeon.\\nVisit the repairman.")),
                null,
                true,
                3,
                NPCInteractions.NPCInteractionType.CHAT);
        setDisguise("custom:ag_combat_instructor");
        setCustomDisguiseData("player ag_back_teleporter setskin {\"id\":\"2309bbd6-da95-4c32-9de0-08cd1072d582\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY3NzI3MTg3MzI1OCwKICAicHJvZmlsZUlkIiA6ICI3YmRhNDBlM2E1YjU0YzE0YWJmZGYzNGMyODY2NjQ0NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfRWdvcl9wbGF5XyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xYmEwM2E5MzQ2NzlmOTdlZTNmNTIyMDI2ZGQyY2FlZDNmOWUzZGUxOTIxZDdkMzUwNzRhNzA5YWVlZTY1M2Y0IgogICAgfQogIH0KfQ==\",\"signature\":\"yLnzRj53v2941K05aMvIqH/5gREtBrDJCHReHsHko3jLzsN10BidUJd9Hs40wIdrlji8G/GvVv469fmmSVda5QTInszgNhDTUk/0IpC4Ir9nGe5S9mN+gDuyaHHClux0ZRxQkf4O7A/8765y7lGG8Y3ZBNFwovyht3Q0qoxW5AP0AUVvv7GHy2MeXWv7iDtrb3o79Yzy8tm1SG3d9jnCnIEjpKD98U5cwAu9N/UL5MM1zgVtMXsB4D7ymm3bHy+SC4HYjg8oA1TLYc9JofJB1qsMcKUbQ1N3BG63p1b6yf79ADRgtZY258Q83G9x2TmznlKeAH7WgnnYL8Mf01XTr5D7hUwhkxpPVAwLL0kJ3BXmOEE+CwRoBthOWTCWvSTNfkBu7PicML8zzdT89dMFJF+OXH/ofg+FHkhlO009mmd4H7FaAkmTAXZpqr9vjfwnO3/m5pcMgNiPpDqyZushdJKEAOTIQtUR5hO6L1u6opIVUmE5pVSY4BuuZtOi7tlkkJpqnXH8+5gZoQq7ZB0yrnNr2LlhDLdfHkBCG2/nhHK4yVdzRWb+N20c2yP2FUXWbCaWvZF8+i2051zAarqkaLL1hJ24Ysmu+n1BXbLSKB/AlUJVHmrQbJZpj+LKX1Up4buqQmnFLQwem2O3hyAaEnbQ56xwVHTiszQJ+BJqqm4=\"}],\"legacy\":false}");
    }
}
