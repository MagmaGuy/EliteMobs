package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheNetherWastesTeleporter extends NPCsConfigFields {
    public TheNetherWastesTeleporter() {
        super("the_nether_wastes_teleporter",
                true,
                "Nether Shroom",
                "<[50] The Nether Wastes Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,295.5,78.18,198.5,180,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_nether_wastes_dungeon.yml");
        setDisguise("custom:elitemobs_the_nether_wastes_teleporter");
        setCustomDisguiseData("player elitemobs_the_nether_wastes_teleporter setskin {\"id\":\"ebc6f487-4973-4941-9f41-30f957de3a00\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY4OTI2NjE1MjExMSwKICAicHJvZmlsZUlkIiA6ICJlOWRlMDE0NjExZDI0NGY5OTVjNmNiMjhkMDk5MWExNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbGl6YWJldGhUcnVzcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80MzZhYzQzNzhjOTlhMGIxYWQ1NjcyNjE4ODIxMGZhNGU2ZDI4YTRhYjk1MWQzYmFlZTM1OTEyNjcwNTI3MGM1IgogICAgfQogIH0KfQ==\",\"signature\":\"Z057s/MMryjQI0zTUrZDMCSS9+U1pnfY4Gfy1hdZPYBbuZlhOIAxIIRUoeP5fvRBOA6OlOuqsWvH1ihDNhX/KSAmJzIaaCyuCG4yb6ps63v/3mzdO4oJLR2pzB8iOMOBIsBRaEhf6CDhko2C3Moz5SC6Evn9AClt0E8NCXiINOciGU8i8tMM7JYk3JTswW2KWoBC05AO4c6I/FptYIoASpFZuy9Ie3CefRmTNsFfrSXhocWMNiYici9CB6iyz2Fm491U0imJpqx2i20iCuk8J9VbrGpu94i7sbzkZwCl7983RSa+u5k36q3TNmEg03xUlqoB4icOWN+kQc50MJs81IYnvnLqGwLYukiTExPlJ+CmoUE6bLIOQV8g/P8O1OecLw4IXQx9pSKdoUwb/eNNUsQ8Uk7yHtkgCBKN8CCLbN+s5hZ84+zLa1T5ITnrDfm3BBpjJ5yOFq6pK3WF7YuyOfwxrzs69vi/AMTQ68t8UVWzjHOG2nzQKIA3mlSbZ1RIpy5RgpGM3axwzJeTo1rLP4YGewf6XHStPeQkehotbUKcIzuq4hbdMHAZfpcCq0he7oXw6T5ulA4by9cDCLOgQZpZfDZdI4rvgZUkN12jH0WmrfHGqde2qRcoGS4u3tlw3YCXXQCa3UuhGEtkdCDBYQSbhs2i5DTcm7zaTGp38Bg=\"}],\"legacy\":false}");
    }
}
