package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class ThePalaceTeleporter extends NPCsConfigFields {
    public ThePalaceTeleporter(){
        super("the_city_teleporter",
                true,
                "Royal Guard Sven",
                "<[35] The Palace Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,300.5,78.18,201.5,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_palace_sanctum.yml");
        setDisguise("custom:elitemobs_the_palace_teleporter");
        setCustomDisguiseData("player elitemobs_the_palace_teleporter setskin {\"id\":\"602af930-1f59-4ea4-8e9e-0416333e24d5\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY0NzcxNzM2NzUwMiwKICAicHJvZmlsZUlkIiA6ICJkYmQ4MDQ2M2EwMzY0Y2FjYjI3OGNhODBhMDBkZGIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4bG9nMjEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJhM2ExMTM1NTQ0MzkxZjFiZjZjYTJjYmI5YjA0YWNiNzJjZTFmNmIxZWU5ZWU4NWU4Y2QzY2ZjODU1ZDJiNyIKICAgIH0KICB9Cn0=\",\"signature\":\"W8hpBA1jku8YoGLiw5FYL3HXwCRi6ZAUV9LjHMZMWjjnBxqohyPYWd26J6CyuzR1LSo3nMqctc40qFme9GtTsHtjCeYjl7ep9MRB73637hzLKtkNHudnyoX09sSCRBhFABWaROxw4ZF9/WtRxkrrDwJpGYJl47MDcy8PRlZ6vPYKJcUxc1xWxm+JTNxgeAYoTXuya56gUgULduFPS7/5sVGtNviLKbYuxrg8O9RSiB3hSEGtcf2HtzSH0a1Nhdv7uwEhWjTmRrAABJeF16V8hdoiaYylHP8nSGCSbbJLhm/zc5AjkdK4riZG8wZVnheRtBWHsBl/dzbXqg60Fy5B+pwyRNgdDvq3fbcCkWbWE1f0GNWVwzSy/p/oUZUmggfPKGfFluvwT84NzXKstH6GsxXpDjz6Tg2mYGxihKvaV5alljlRqIOeQZ2YVmyn92C7T0a6omF1jbLtaew94PKBqSOZA4qa9nTfHZRbnpeBse9q/DxexJU2EfPMR7jE3S2sEXAlXu2RrvTOGTC5McivEaYSs+00ANHhATRMNYCJyAXYgR4lUA5YXU+Hi4u6ypSGnHH+IS49QjATyWhAdyJpftB5r0hlK1oONd/4Zhu0g/bsrNKRV9GRhW17Ny9Qmk0JidgF+JpHd4/jU3uwZmf61fLwxz3Ziq4prcUDBIskFt0=\"}],\"legacy\":false}");
    }
}
