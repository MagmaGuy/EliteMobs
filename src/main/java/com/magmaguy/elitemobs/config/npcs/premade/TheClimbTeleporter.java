package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheClimbTeleporter extends NPCsConfigFields {
    public TheClimbTeleporter() {
        super("the_climb_teleporter",
                true,
                "Miner Regulus",
                "<[10] The Climb Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,303.5,78.18,208.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_climb_dungeon.yml");
        setDisguise("custom:elitemobs_the_climb_teleporter");
        setCustomDisguiseData("player elitemobs_the_climb_teleporter setskin {\"id\":\"ad1681c1-65cd-4dc6-9ea0-fc9143fab444\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY3Mjg4ODAxOTA2OCwKICAicHJvZmlsZUlkIiA6ICJkOGNkMTNjZGRmNGU0Y2IzODJmYWZiYWIwOGIyNzQ4OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJaYWNoeVphY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzE1N2IwNGNkMmExYTBjYWUxYjljYmFkNzk2MDY0OWIwMTI0NTMwMmE0MDlmYmQ2M2EyMTg4MWM2NWUwMGQ4YyIKICAgIH0KICB9Cn0=\",\"signature\":\"Bo9OzYaZuF8YVCO6+d5Vx5vYG5FsFQzBBbszlZj8rHL2jyDSecxojHqjC3k/T0wSsWW9cVS14xu3TkLyWcPJv9LWUE3F5pmiynQp1iAc75TIXYV3DU3uQQ8LZp/FehMquc7CoaIGaNh7ZYAHsJ/Rp2uYned6YGeAHtWCd+Um4HnekgZ8oAFxgySQTA8qP//N55VUcyRNvuRyEsEUnSQsi3dFJsDTR+QicOdrCPkPsnOgIrQaDiNu2sspr0pfuCalwVFivqf2eAt/s1MWuLNIzaBOHR/OTVlL2QAlRZPJJjfAVXGuq08Hb75YacrizwM2vVCpQ9DdiAA2EjnS19rEsK7IsP4xQsMvxZFpa/OqnEbHn1vQcKueQSWnj4BtmwmAP8HLhnL+HzOsEGenpurIQAohnWWr2NmbxsMDSSy5oAEalOVzglDdQl6E+tPNDf7YECRB7yOF4t88GIQrWNui2cFYjPuYd9tphxLkfN0ekY7rceoHwIU/Ebu3LpU3/al3X1c1kXNNLhmQrOivMTe2Zw6mILPjuPJyRR0aISP4DHDC+OB+dvDs0v9fqfM2S7U2mQzqob/BVFCthLioIoqbrc6UFk1FzOE5xUc4tbv1q00GOXLwM9i9OGMtVDfvNqZVRijlKNRC/xclTrmkxhpD9cQX2ihd/FjJCaaoe0WjaOM=\"}],\"legacy\":false}");
    }
}
