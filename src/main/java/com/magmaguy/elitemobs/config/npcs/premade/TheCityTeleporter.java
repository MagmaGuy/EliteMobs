package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheCityTeleporter extends NPCsConfigFields {
    public TheCityTeleporter(){
        super("the_city_teleporter",
                true,
                "Dwarf Olav",
                "<[30] The City Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,302.5,78.18,201.5,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_city_dungeon.yml");
        setDisguise("custom:elitemobs_the_city_teleporter");
        setCustomDisguiseData("player elitemobs_the_city_teleporter setskin {\"id\":\"0912ce06-1d48-4e3b-8c06-21e274027bd7\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY5MzQzMzgyNjI2NywKICAicHJvZmlsZUlkIiA6ICI1YzY1MGIwMWIyYWE0MTY2ODIyMDQyNzA3YzMyMmU5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJQdXJwTEFCIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZlNmFlNDQ4ZWM5MDMxMjFmOGY2ODVhYzk0ZGE0N2FkYTQ1ZTE3ZGRmM2I5ZmM4OTVhMWJkYzM0ZmJiNWEzYmUiCiAgICB9CiAgfQp9\",\"signature\":\"qSe2tRxf7qD7q9FY+juA9WTmI7gKX8q8ZdL9Lq1pEkuv5TFqJRZNaCx81qSiBfMP0v4ynVdelt2i79EwJo2BmqlmJXMP3mZX8IAQel6v1bLClKVHfK4v1Yn9Z+fxVVWjxR+5qvGd0+W6Sa58/YVvS0Dm5BXsJJqybPxjDAOz9BsPqXIVfc5OyDO6kdvcjWs4i7jcKoStgfHOOqOdz0qF34lLeTMocysofekfqm8qPe3svdgPHFpNUd7OsaRCQ8ZisvGnIEhQN/dlQrxZgkXE4QHzHU3N1dUE1aNbEUiDbQmcW+sItJrI02oXV2B5wYstadKzt+ZtizEuzQ62Y/zxKVUhKg9GziEcQT4XUcPYm33Ra06NUTfGZZL9rjSCfpcUigni2VheBHc6952PIpfwsVlPl0+uRpBrFGvZF2VVIXILrYcbCuwYtIvN03N+T7qxeLVaIFCpZoObyAhmNVRNxhoiq/w0uK52Xn41jt5qrCKsM10JK9SnoKQ3YJ7q9bemw6SiprRejL0r7RRHvKDoEGRE0fifh4Gmr2Yep8gaZgBqf6DFDmaRR5iq2kjlVXTvURwvFNPYsI3SHBWVyfTM8V5nQ1OSEoJzyf1Z2llMS8b7uJQylygkS+cEStFNmsg0tyPN13TVef291kQ6q8L8lLB2j6mVOzPp+/97A495yGk=\"}],\"legacy\":false}");
    }
}
