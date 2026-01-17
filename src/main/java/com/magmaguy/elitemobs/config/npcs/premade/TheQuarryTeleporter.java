package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheQuarryTeleporter extends NPCsConfigFields {
    public TheQuarryTeleporter() {
        super("the_quarry_teleporter",
                true,
                "Dwarf Berge",
                "<[40] The Quarry Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,303.5,78.18,201.5,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_quarry_dungeon.yml");
        setDisguise("custom:elitemobs_the_quarry_teleporter");
        setCustomDisguiseData("player elitemobs_the_quarry_teleporter setskin {\"id\":\"da3c951f-2e67-4df4-8a86-dc6109ab9084\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTcxMDI0NDgwNzU1MSwKICAicHJvZmlsZUlkIiA6ICIwMDM4Y2RkYTcyNjU0MjE1YjdkNWZlNmNmYWZhZmM1ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBR0E3T04iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmIyMDYwMGMwOTM1Zjc2MTFlM2E2MTYyNGYyYWZmNjFlNjBkZDExMWFhMGIzYjA1YjAwN2UzZjljZWQ2MjUyOSIKICAgIH0KICB9Cn0=\",\"signature\":\"qJkykPsoNRZwCELGWQWi71i/Gq0tvCJY9cRz7LCdHN3cGBVnKkH8VhbR0Nl1HxyIJJn0H6Od+akbLP6i92+nhxHkGKbO862qDyR+2PnpGJ4FydAQ5+bZ+FkML/JtR2BxY8/5n9uz3SxmTwQOIT697900jHbAEB39BRR77An7Fz5wdNsAoW3rqgMHApaKTFzcH8PK6B9Vg7ksCOfMh7ckbWPkaB2fq9TVwFZsCOkBbAhSPXwYzI2faS9BBUoEUzWtMxcDLQGu3H3pY8lJaf1ErMXxuyAbN6wsADWqiJ1mlgHUvt5FFfCxXIOjJs0uWRSZsL7H9/6gXrZWQqOyDKQDS533iSM/eHNOdYgh9gTuDiAZtZ/624vJySsVo4PoJlhH631OEe7iaBR11AYiGeHBWDXvdOGm+lx6O6MhMRPQHIy9RjLE3fEDCSk2N1cGH+LAAf67I1i9u92m1IIrqfETzEy4ripy7wC2anC2s7vRkqWxl8pdvoipzMxle5OwP2shbMfwgP8R1uWn6iTLsv3RElqifhu+IMcB1fAgghWWXr8AamxrAff5DRY+5jqEIfRMisv9ML5ikU3wN1eNFkIk8lzLL3FHdW6zGXuD36LpUt+l7w12Iq8iA+sjyFs7+hIJv3fkpJ5I5DIkJQEJPqgtpJ8EOIbXGNixNiBERFTw7GY=\"}],\"legacy\":false}");
    }
}
