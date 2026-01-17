package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheDeepMinesTeleporter extends NPCsConfigFields {
    public TheDeepMinesTeleporter() {
        super("the_deep_mines",
                true,
                "Nether Lurk",
                "<[45] The Deep Mines Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,301.5,78.18,201.5,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_deep_mines_dungeon.yml");
        setDisguise("custom:elitemobs_the_deep_mines_teleporter");
        setCustomDisguiseData("player elitemobs_the_deep_mines_teleporter setskin {\"id\":\"ecf00747-d91c-4acb-82e9-320a70d924c0\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTcwOTc1MzAxMDgzMiwKICAicHJvZmlsZUlkIiA6ICI0OTY5YTVlZTYxMTY0MDBkYTM4YzhmZjRiMWJhZTZiZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWFjdFpJUCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84ZWM1NGI2YTYzNmViNzRlYTMyY2NhMTA5ZjUzOTY5OWZjZDM3ZjVjMjdhYzA5ZjM4YjFiNmQ4ODU4YjAzOGM2IgogICAgfQogIH0KfQ==\",\"signature\":\"USRl7qn5bARzqeZa8E+w2b/08LETpa1WvRe8nDcQntAMl34BR/AQxeoxCbU9nIov6tQLKgxwdlnlDx5m2JIs4hMnVPrJZQXKNuu+Il8K4r4hf6H59BlAHn1bkcLuMGhZRRjyyDf78itKli+5cw3pKm3K+3FilZZcj+NRT/+FrZa1ifXe8OKj/yTSigzI6v5OIwCn589KOi/66htbWIonfx5vWPp/+Lsi3SdNqgBS02N7cEUzdv/SxT5eq+x5hU2fNXKNkgmP09yysg7vDR+yMCKOTlaxhLbueoBjC/h5/aCbGH6kD1Ido8gyZbsS8oLK7yrGLxZSO+O1cko00FDHieOf8yhfpGSQTta93R/xYMTGPkW5Z0tXbf+gK7gWRz2Y/hWUHes8s2u32SQ6cTamO66sumFEn3dn+W1EFtKVMvjj5KQpnQ0hQ7zX0gqOnxbdYEr29FHIulgFQdiloogfrrl/PmizmmBVMylmu+m7nwKYlYQZWSKHIAzxU2F3RN2nX7Td5N6uJr05da9OLFGQJcwacXo2VQmxfO9L8kuYjMCtCHsW4gAP2pGX/u0D8O0tBHtu3jaqx62Jyc/5PgQXt8f3wKe1IQY3x9B8MwBi4mPA68CRSJzf7hPDPjHx3rLXfJzUFVWHn79gmMbZnthI+4q6lO+czDUHjyEYjOvuW0Y=\"}],\"legacy\":false}");
    }
}