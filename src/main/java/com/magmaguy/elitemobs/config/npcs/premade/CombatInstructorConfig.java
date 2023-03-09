package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class CombatInstructorConfig extends NPCsConfigFields {
    public CombatInstructorConfig() {
        super("combat_instructor",
                true,
                "Charles",
                "<Combat Instructor>",
                Villager.Profession.NITWIT,
                "em_adventurers_guild,277.41,90.0,286.23,-214.85,-1.95",
                Arrays.asList(
                        "Want to learn about combat?",
                        "Need a combat lesson?",
                        "Want to know more about combat?",
                        "Ready to fight Elite Mobs?"),
                Arrays.asList(
                        "The items around Elite Mobs\\nshow what powers they have.",
                        "The higher the level of\\nthe Elite Mob, the\\nmore powers they can have",
                        "The higher the level of\\nthe Elite Mob, the\\nbetter the loot they can drop",
                        "Higher level Elite Mobs\\nhave higher chances\\nof dropping special items",
                        "Elite Mobs with floating\\narrows fire arrows at\\nnearby players",
                        "Elite Mobs with floating\\nender eyes blind you\\nwhen they hit you",
                        "Elite Mobs with floating\\nspell particles confuse you\\nwhen they hit you",
                        "Elite Mobs with floating\\nlava buckets set you on fire\\nwhen they hit you",
                        "Elite Mobs with floating\\nfireballs fire fireballs\nat nearby players",
                        "Elite Mobs with floating\\npacked ice freeze you\\nwhen they hit you",
                        "Elite Mobs with floating\\nelytras make you float\\nwhen they hit you",
                        "Elite Mobs with floating\\nemeralds poison you\\nwhen they hit you",
                        "Elite Mobs with floating\\npistons push you\\nwhen they hit you",
                        "Elite Mobs with floating\\ntotems weaken you\\nwhen they hit you",
                        "Elite Mobs with floating\\nwebs web you\\nwhen they hit you",
                        "Elite Mobs with floating\\nskulls wither you\\nwhen they hit you",
                        "Elite Mobs with floating\\nchests drop bonus loot\\nwhen you kill them",
                        "Elite Mobs with floating\\ngold boots are faster",
                        "Elite Mobs with floating\\njukeboxes taunt you\\nduring combat",
                        "Elite Mobs with floating\\nglass panes are invisible",
                        "Elite Mobs with floating\\nspectral arrows are immune\\nto arrows",
                        "Elite Mobs with floating\\nfeathers are immune\\nto fall damage",
                        "Elite Mobs with floating\\nflames are immune\\nto fire",
                        "Elite Mobs with floating\\nanvils are heavier\\nthan usual",
                        "Elite Mobs with floating\\nbone blocks leave their\\ncorpses behind when killed",
                        "Elite Mobs with floating\\nslime blocks jump higher\\nthan usual",
                        "Elite Mobs with floating\\nslime balls create a vaccum\\nupon death",
                        "Elite Mobs with floating\\nleads pull you\\nwhen they hit you",
                        "Elite Mobs with floating\\nbones create bone pillars\\nduring combat",
                        "Elite Mobs with floating\\ntipped arrows fire tracking\\narrows during combat",
                        "Elite Mobs with floating\\nmushrooms bloat up to become giants\\nwhen hit",
                        "Elite Mobs with floating\\nskulls summon reinforcements\\nwhen hit",
                        "Elite Mobs with floating\\nbooks summon reinforcements\\nuntil you kill the summoner",
                        "Elite Mobs with floating\\neggs summon reinforcements\\nwhen hit",
                        "Elite Mobs with floating\\nfireworks summon Team Rocket\\nwhen hit"),
                null,
                true,
                3,
                NPCInteractions.NPCInteractionType.CHAT);
        setDisguise("custom:ag_combat_instructor");
        setCustomDisguiseData("player ag_back_teleporter setskin {\"id\":\"2309bbd6-da95-4c32-9de0-08cd1072d582\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY3NzI3MTg3MzI1OCwKICAicHJvZmlsZUlkIiA6ICI3YmRhNDBlM2E1YjU0YzE0YWJmZGYzNGMyODY2NjQ0NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfRWdvcl9wbGF5XyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xYmEwM2E5MzQ2NzlmOTdlZTNmNTIyMDI2ZGQyY2FlZDNmOWUzZGUxOTIxZDdkMzUwNzRhNzA5YWVlZTY1M2Y0IgogICAgfQogIH0KfQ==\",\"signature\":\"yLnzRj53v2941K05aMvIqH/5gREtBrDJCHReHsHko3jLzsN10BidUJd9Hs40wIdrlji8G/GvVv469fmmSVda5QTInszgNhDTUk/0IpC4Ir9nGe5S9mN+gDuyaHHClux0ZRxQkf4O7A/8765y7lGG8Y3ZBNFwovyht3Q0qoxW5AP0AUVvv7GHy2MeXWv7iDtrb3o79Yzy8tm1SG3d9jnCnIEjpKD98U5cwAu9N/UL5MM1zgVtMXsB4D7ymm3bHy+SC4HYjg8oA1TLYc9JofJB1qsMcKUbQ1N3BG63p1b6yf79ADRgtZY258Q83G9x2TmznlKeAH7WgnnYL8Mf01XTr5D7hUwhkxpPVAwLL0kJ3BXmOEE+CwRoBthOWTCWvSTNfkBu7PicML8zzdT89dMFJF+OXH/ofg+FHkhlO009mmd4H7FaAkmTAXZpqr9vjfwnO3/m5pcMgNiPpDqyZushdJKEAOTIQtUR5hO6L1u6opIVUmE5pVSY4BuuZtOi7tlkkJpqnXH8+5gZoQq7ZB0yrnNr2LlhDLdfHkBCG2/nhHK4yVdzRWb+N20c2yP2FUXWbCaWvZF8+i2051zAarqkaLL1hJ24Ysmu+n1BXbLSKB/AlUJVHmrQbJZpj+LKX1Up4buqQmnFLQwem2O3hyAaEnbQ56xwVHTiszQJ+BJqqm4=\"}],\"legacy\":false}");
    }
}
