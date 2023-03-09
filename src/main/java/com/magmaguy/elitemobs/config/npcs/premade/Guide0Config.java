package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;
import java.util.Collections;

public class Guide0Config extends NPCsConfigFields {
    public Guide0Config() {
        super("guide_0.yml",
                true,
                "Dux",
                "<Guide>",
                Villager.Profession.NITWIT,
                "null",
                Arrays.asList(
                        "Heard about the AG?",
                        "Been to the AG?",
                        "Heard of the Adventurer's Guild?",
                        "Know about the Adventurer's Guild?"),
                Arrays.asList(
                        "Heard about the AG?",
                        "Been to the AG?",
                        "Heard of the Adventurer's Guild?",
                        "Know about the Adventurer's Guild?"),
                Arrays.asList(
                        "Come back anytime",
                        "I'll be here if you need me!",
                        "I'll be around!",
                        "See you later!"),
                true,
                5,
                NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER);
        super.setQuestFilenames(Collections.singletonList("ag_welcome_quest_0.yml"));
        setDisguise("custom:ag_guide_0");
        setCustomDisguiseData("player ag_guide_0 setskin {\"id\":\"ff49abb7-d842-45b0-97df-cf520b08dd1e\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY3NzE5OTM1OTgxOCwKICAicHJvZmlsZUlkIiA6ICJhYTk3N2E3ODk0NTE0ODdhOGZiOWVhYmI0NzcyYmNmMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiZXN0c3VuZyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yZDNmYzlkYWQzNGNlNDEzMjRjMzE5YmE1ZmQ2ZmYxZjk2ZjI2Y2QwNDMxNDI5MDI5NWVkYmNmNzYwMGFiZGM5IgogICAgfQogIH0KfQ==\",\"signature\":\"xMqh9jXhUFVIWBHnl2YD3dcmb+Nyq7RP7IY3iSYpkQrT1RPT7lNSVLVinRRDt+Yvrd4Tm5UKy7V7pjPTZdUamjbMzrqGATrHAdc/RXCj7Skk11hpXRlJijIrXR30pAZ/piZmm0j9O8k78PUq+funK3eo6W3HO7O8oIzPnbO+PAovpT6W9CDq6KUyNHoxWOP5NTk+VJxV5CaGKs4zrlGSztRlwBydJnlG2LTzdQ01Pqa5hm1QfMensebrhk3x8bLIIfl5q/CkWGuTfglQKisFznwXcsBihmKk8o5Dj+pnd7uy8/KMQXx7gOZN/5+k5HLXT1w8zNcTmpqCxJnKsNgmK3qHDMbSWvSkEmRDTRubNPokd4n5eMMuKmylQ3Io2aP+H1GpE+K/NW0Q80si/BZ5OY3WuF6tFcpWGw8RlWj6jgoQSjoIS6o9Y8uZaHBaz5d51gdyTfjyRGxzXhH/TQQu+ND/qimtmNwf7Zd3ricYXbJ8ZMuFnSuQSx9G5i67Idy11leShAGLk7UQinR2dQ/hBMKwZ0C8LFBgg1EejZxhJi0OlxJC093XKcspbj4m3o7S6WjMEeIRwmoKKtqQKJBCoCWIMp0MiON0+dGq1QJDOLTLyEGexVXqncn1MfMw3XL9amLIfTObmP08DSXERpEjnYwJ94+EP6daxYJSBtlLWpY=\"}],\"legacy\":false}");
    }
}
