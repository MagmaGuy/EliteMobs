package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheBridgeTeleporter extends NPCsConfigFields {
    public TheBridgeTeleporter() {
        super("the_bridge_teleporter",
                true,
                "Old Dwarf Jotun",
                "<[25] The Bridge Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,308.5,78.18,202.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_bridge_sanctum.yml");
        setDisguise("custom:elitemobs_the_bridge_teleporter");
        setCustomDisguiseData("player elitemobs_the_bridge_teleporter setskin {\"id\":\"4dc553ab-1814-4362-909f-d529ede78768\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY5ODQzOTE5ODIwNiwKICAicHJvZmlsZUlkIiA6ICJkMjdhNDM3YmI2OWQ0N2E0OTY4NjU1OGFhYzA4NzA1OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJEYWx5bmthYSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xYzhiNWUzYzIxYjUxZWMwNWQwYmNmNmZlZWQ5Nzk0NTczMjkxN2I0Mjg3ZDY1NjJhYWFjOWI0NTY0OTBkOGExIgogICAgfQogIH0KfQ==\",\"signature\":\"KVTpE02yIZ99o6eFzO819AMrYvoASlq6SsZqqPXjrJD5unrKI+SW22fu8vAHlFdUbC//10dygqVzBo61yrtrbfxxXyFLMYdTrxqHpGvnCL2k64/sgVNEl9DZu0kZnLybXYnRTAQEU7bi8Piru4UrSp6GBD4lU0UMzXxcBk6ucj0yI3i2pmzKl0Sn/wzjoD/2xhfymYlBgZks8uxaWlfzZobZMhYSeCGgnANR3kkT9VbHvpyNOPYuWwWHvYjTUnyCZajz2RWgZzwGiKg2X5JIMW3KLfzljUlH+wPVApT6pfpx0nbw3Vt1Tf+0OSJmjscHbl5OvmnWmn8b2i+XgWlxylLKhZ52mCxy4ONqFDh3lwy5+ZEtZLsdhnsgxYWoenxdSTa0uSWas7GhRdvmZIeNDjn/TAbp5w0D8n4ycp7hatF+WlsE2CYB9Ycs9k99lanC3MA49bo1lz7/e3BAiJDIOsZJ3vOEIQgRt8TCl2reA2/j1rRisjexNLqM/nLzlZaGa0hxdddwuH43NBi13dkBIVn3g9dGGzOP6dSeGth1BXlSsTvINqlGMlMWKTlRwzf2Wolpopq0ndvPQ925XWOaykO45JxgrYIDksiPJek2+ZI3MSduAslXqykJEQ7CHAPqR9Nvllv1JM/osp4pTElBEAEpta2eQJuLYllf/jqIJBk=\"}],\"legacy\":false}");
    }
}
