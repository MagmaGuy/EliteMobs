package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class WoodLeagueArenaMasterConfig extends NPCsConfigFields {
    public WoodLeagueArenaMasterConfig() {
        super("wood_league_arena_master",
                true,
                "Gladius",
                "<Arena Master>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,240.5,92,270,-108,0",
                List.of("Welcome to the\\nArena!"),
                List.of("Prepared to face the arena?"),
                List.of("Return with your shield, or on it!"),
                true,
                3,
                NPCInteractions.NPCInteractionType.ARENA_MASTER);
        setArenaFilename("wood_league.yml");
        setDisguise("custom:ag_woodleague_master");
        setCustomDisguiseData("player ag_woodleague_master setskin {\"id\":\"76a269fa-65db-4e1a-bd33-a51b390bfd04\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY3ODI5NDYzMDAzNSwKICAicHJvZmlsZUlkIiA6ICIyOGMxMDVkNWRmY2Y0MDYyYTBkYmNmMGJjMzMyMTFmMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJZb3VuZ1N0cml4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc5M2Y0MjllOTIxYWY3YzdlMTlhMjNjNjIxYTU2ZDBjNGZiNDZhYTlhMGQ3ZjZiOGRlYWEyNzgxMTMxZDNjZmMiCiAgICB9CiAgfQp9\",\"signature\":\"b89XDzrLpZAtqC887k0NHAyblnctYLxX9byh2p/W6pXE4/W0rP1qP/y008wms58ec8lgqcZ62J3vKs7VVouJb7PBw3BXSIz20Zs5VrbYRTWhGcorsqOIOl4J7qf2VNXeyJw+p08hi3vnmMS4T4+18/9KiLXWcXE5dKsBxufGTrr5Kthbf7Lo6XSEz1H/PGkSFtJdz63QN/Sb+BnnamLUoaSUyKLK/XsRY2WxqQ+HL/svgGNnY5b4oGewBWznaCmqjzwLsNbOfNMRzi4RMGVdg4Nf3Uct7NWxN1WHIKQwhlT0+3sAwFFaD4Brw72xDZdC8eLDefEt3iYs3MN+kUjFu7FnB8Fz8K71aY/8amdEob2tcMJlBy+962OcYJXvf/GX9yr3wY/e3FN/Y2Act3UdUNK+CY0I4m/0ntEx/wcy/tkFP8ygxB/PTUrgQDQf/1FGo8LdpcZ/lSCmbtr0pSpZacpMSMhwDlYOCCHBWYNgmaZu5m/vWRMLsRxKG7DRRFjRe4Vz/djCVUqQEbVt/UxHFQ+69DYzGtXC7fXnkb4zMFWGhAYKPRjIdVqWzJ29oy5CVoc7vNEVVxvWiIABwtdsgEsP6TR7S2wH9h8iPOVwe3mtJR58XOH0TAc9Ey9GIv+2s6cofOCZ4dA6kER57TPGmRlhGNR1YxFOnoutj0QZrCA=\"}],\"legacy\":false}");
    }
}
