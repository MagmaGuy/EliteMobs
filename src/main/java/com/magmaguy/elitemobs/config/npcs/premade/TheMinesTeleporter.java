package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheMinesTeleporter extends NPCsConfigFields {
    public TheMinesTeleporter() {
        super(
                "the_mines_teleporter",
                true,
                "Prospector Voultar",
                "<[20] The Mines Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,308.5,78.18,204.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_mines_dungeon.yml");
        setDisguise("custom:elitemobs_the_mines_teleporter");
        setCustomDisguiseData("player elitemobs_the_mines_teleporter setskin {\"id\":\"bc71543f-ec67-4d65-9b45-c1eab4cc85f4\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY4MDYzOTE3NTU5MiwKICAicHJvZmlsZUlkIiA6ICI5MWYwNGZlOTBmMzY0M2I1OGYyMGUzMzc1Zjg2ZDM5ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9ybVN0b3JteSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82ZTJkMjhkNjZmMGY3ZmMxMzY5YWZiN2QyNjkzYjkyZGRlMTJjNjI4OTg4YWZiOTQ2Njk2ZjllZmQ2OTgyNjU2IgogICAgfQogIH0KfQ==\",\"signature\":\"UFasr3WLe1zDAH23jgTBC7Tr8tO2tSuzcxoGGAlNQz4fLvuUSNirPR3K6K7SSqHAk1yImchvW4Q/wakCjVmSJPwHLY253a+KkjmPcxQkabNusjyVAPew6gJpDRQJg2tLIxNQd+0Pam1xLDtJ0pg1JpBvcAlQ7OGOkBMIplxHlwFsJc3BFRsIjVAbKlzUT7YLFw85HEDT2nQuWxMZscwcopu2FEVs5cU8e/F8B86HsO42cAXKdui0TESdZy0AH8KbcGcYBf9qCu8gGFz+KtiM9zIb4ghSLlasK/ioTmSD6/qzraIuwaLin90NZXnotP2/OZ/1wEbn1rIyR5LDlk0fYxbsAZxUrISFC2xreD/uFepSAtrBO6li+Ie4StDuDdMKZKsr1tPAJGyc4+I9EjpFfXmXzMMkpafsW88bD9WrmdGoaFttsO6L2D3Pex8E9Q7HSPO4IxQ5lpnBPdevIh7zcY6MggLAvPTIDFjE6R/ka56/rReyAof8ocR4gUiioOjB/OB/t2WH3egO7sSBiIzof7oAtjL4vQ1MZVeWTLjTJQ0fk5rd6qkijTEv0bHfFR0ZMmkWN8mC0lBi2M34guHcxerYxBwjtu72gP9Hfmex28uZeNG2jG/MlDwhTHHGfY9Yv2lJLIWd8W9W5Wpk7IDgKaQ98q8hjyR6PjmPoCD8MOw=\"}],\"legacy\":false}");
    }
}
