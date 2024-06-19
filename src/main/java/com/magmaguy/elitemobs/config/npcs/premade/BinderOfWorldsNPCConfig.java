package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BinderOfWorldsNPCConfig extends NPCsConfigFields {
    public BinderOfWorldsNPCConfig() {
        super("binder_of_worlds_teleporter",
                true,
                "Buði the Doomsayer",
                "<[200] Binder of Worlds Teleport>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,294.5,91,223.5,180,0",
                List.of(
                        "You are not prepared."),
                Arrays.asList(
                        "The things I've seen...\\nYou wouldn't believe it.",
                        "The End is near...\\nYou are not prepared.",
                        "I give the greatest challenge of them all.\\nExpect death."),
                new ArrayList<>(),
                true,
                3,
                NPCInteractions.NPCInteractionType.COMMAND);
        setDisguise("custom:elitemobs_world_binder_teleporter");
        setCustomDisguiseData("player elitemobs_world_binder_teleporter setskin {\"id\":\"fda90f28-71fe-439b-911c-a815a787ab85\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY3ODI4NjMyNjY2OCwKICAicHJvZmlsZUlkIiA6ICI3MDQ0ZDlkY2I5OGI0YzgzYWFjNjIzNjFlYTY5YmNmOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJfV2F0cnlzaGthXyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81OWE2NzMxNmY0OGI4ZjcyMzlhNzJjYWE1ODM5OWIxYjFmMjVlODlhYjAwNTIwZmI5MWI0YTEzMmE4NTBmMmJhIgogICAgfQogIH0KfQ==\",\"signature\":\"QujA31pUzzN+Nnnrq9TrYJJN+TGgqVwf7i84QSL0R84oVDiFC8uBd23ZV5jWql1wuHAlCGHnozqa7MkXmJ+b2P8Aqa/DVLEeWO5o9xkmYsTR28nR1gesHe9/1NNRj8hSsqP3trwYsIdGrTkSc5B56px/jDKM3uvpEpFk9CyzKRGccizunavWahUS3HRDmgj8EsjyeIPuZhbkA/0SSGtA0eIyg3xkmcT7NWOcebW3yGfknc9AYmrKRpS+ud6bRNXb0681uHrshz1NEg9X8pg4+W1xP1xgzxWOQfYNP+T9t1IFTUPql9uTp6cJ27z/uTSneH6FpFbUpve0H9X0w8Ow/OmNPEJzdfLrDmgCQHSwZERgvorKBn9WDfPgHX/wzTNIGxuzU88X4KYa3E0HO/HRJ4shMDSc+cYoB/KGuq1xpHYbAbmbzzUeTS9WhQ9jqB82Fr0Izq0eb81/gapFTzl5Au+96LTBnMVqCaAFb2mY3pD4ZGChCuxYWgD45ORvqR2QPVAjs1acbzqLLPuda52GKp7NOO4IZ6UIG6bQfj++5RFgn7lDU7qLtt0ADq8Ushha73+1cJLNZbD4tq0b5u7XWzemZba36Fup6fXv6XIN08Lij0CZyMseEFlLZWkgIQCeYdXIhFItWZ0rD05gw/udm8lMHvaHvWFff7I2HVsLQnA=\"}],\"legacy\":false}");
        setCommand("em dungeontp binder_of_worlds_sanctum.yml");
    }
}
