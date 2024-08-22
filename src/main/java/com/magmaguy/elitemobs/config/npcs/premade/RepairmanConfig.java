package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

public class RepairmanConfig extends NPCsConfigFields {
    public RepairmanConfig() {
        super("repairman_config",
                true,
                "Reggie",
                "<Repairman>",
                Villager.Profession.WEAPONSMITH,
                "em_adventurers_guild,278.5,81,263.5,-90,0",
                new ArrayList<>(List.of(
                        "Get your items repaired!",
                        "Repairing items for scrap!",
                        "Turn that scrap into durability!",
                        "Need a repair?",
                        "Got damaged items?")),
                new ArrayList<>(List.of(
                        "Best repairs in town!",
                        "I got your fix!",
                        "Need a fix?",
                        "I'll repair your items!",
                        "Need elite items repaired?")),
                new ArrayList<>(List.of(
                        "Don't forget to do maintenance!",
                        "I'll be here if you need me!",
                        "Call me beep me if you wanna reach me,\\nif you wanna page me that's ok")),
                true,
                3,
                NPCInteractions.NPCInteractionType.REPAIRMAN);
        setDisguise("custom:ag_repairman");
        setCustomDisguiseData("player ag_repairman setskin {\"id\":\"09824045-9335-4ef9-9e6d-26d152afe4ee\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY3ODI5MzYyNDE3NywKICAicHJvZmlsZUlkIiA6ICIwYTc1N2ZlZjYzODA0Njk3OGExMDExNjc4MDZhM2U0NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJWYXBvcmtpdHRlbiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84NmM1ZThhNWUwYjAwYTUyNTcwZTA4MjVjNDAxN2Q1ODUwMjM4ZWM5ZDQ3MjU3OGQ4MzRjMjcwY2FjN2E0ZTVjIgogICAgfQogIH0KfQ==\",\"signature\":\"BASup7t5u1zwqWTzfOqVAy4rMA8ZvRdv5BISpEIUl/FEKGNt3fppJkB/EUvaU2TZuUTaYRTNih3VjtYIkajkp2TKAR+IRAJLurVEK/SidHmXXI39tnb3/bivBlGGCJRU8QU4OYyiNFK7HUyVCFFtJytRENdK0Cv/r83IAy/ac/rVzrI/HBSsdZ5iX304tBpWJlP1iwTC5ezJUpZRfxHdr/vD8ftdPftLJrG/41X+YL55gSOWjxD2o8IzNrTUcHitnTzf56pdxE+DSZkOe3RLnoc9+z+cm1r7+fzIbrV6FkmJ7GxVkFZyRpeFMKt7/4BZb2YPDmLfKyyVq+bPtoAaTr85EqjGY3iHLvxpaBV/wmM9hzqU+A0pN411VMPRJPTeqcp3PCYtqZY+LHMT7kkJqtC4cFyS3aPgeLblLO74oC4RhRLEICybvA68ypmIykHSEgQdGWgcuesE/8Mpibn4hEdvjbqHv6ZiXMjp6WWBIRvNtSBinxwGqofwC+6/KI4uJ1A25KCWyJR4Wb3dAnjy/eDNTkM0LnwpA0Ul9worI20H87NnEV/rmUNN2yBWDf718Tg6Wm7ZEQANp/dEy2S8fk+OkbVr9et1EilVK+Y9lb0rkAfiEFTxXCBykZv8KHGIH/nb09Er7/xguNpoDk4xHOVjO6pPM4RiU4Sluep1Mg4=\"}],\"legacy\":false}");
    }
}
