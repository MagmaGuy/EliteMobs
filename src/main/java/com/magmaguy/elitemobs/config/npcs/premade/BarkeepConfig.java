package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

public class BarkeepConfig extends NPCsConfigFields {
    public BarkeepConfig() {
        super("barkeep",
                true,
                "Bartley",
                "<Barkeep>",
                Villager.Profession.BUTCHER,
                "em_adventurers_guild,285.5,91,209.5,0,0",
                new ArrayList<>(List.of(
                        "Need a drink?",
                        "Want a drink",
                        "Thirsty?",
                        "Howdy, partner.")),
                new ArrayList<>(List.of(
                        "Have one of our house specialties.",
                        "Special drinks won't find them\\nanywhere else.",
                        "One taste and will keep you\\ncoming back from more.")),
                new ArrayList<>(List.of(
                        "Come back anytime",
                        "Bottoms up!",
                        "Kampai!",
                        "Salut!",
                        "Brosd!",
                        "Salud!",
                        "À la vôtre !",
                        "Prost!",
                        "Santi!",
                        "Salute!",
                        "Saúde!",
                        "Cheers!",
                        "乾杯!")),
                true,
                3,
                NPCInteractions.NPCInteractionType.BAR);
        setDisguise("custom:ag_barkeep");
        setCustomDisguiseData("player ag_barkeep setskin {\"id\":\"82410e8f-b4b7-4ca3-9a24-f9f55745e00a\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY1Mjg5ODE0MTE5NCwKICAicHJvZmlsZUlkIiA6ICJkYmQ4MDQ2M2EwMzY0Y2FjYjI3OGNhODBhMDBkZGIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4bG9nMjEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzQ0MTUxYWFjZTc4YzAzZDA2MzdkN2M2MGFhZGE2YTBlYjE0OWI0M2FiZTA4NjFiZmY5MDUyNDFjMjk3YjdiMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9\",\"signature\":\"KEr3OT8YkJUS62aDBAleWzKnjVRlxMyMJGB6wV3NxuCZOK0p/PBb6Hhxu81GU3avwBuQQtyPI0HlSyxag8/ONpvCih1yxejClYpwY2OAMXENHPz+Id8Ps6gEjVOnin9DcVtIUoWyU1pnVVFuEOuXQQTd7E0JGET6DBTpWvc8JvL3rRvUw+wCwvXN7eOC028alN3+cBoLNYO7rWv2zHBZbnKHQWrMqqbh0eVh1EMZZRhzOCTnH8CDyNj/+t2R5XPNUTPWx5x2lS9RVyJm07IZlNk8qczyyf8zBUHICExqFP4sZoJqhrkJZezxUtGv0svTMgiAfue3snCEc5NreU1m7bGnYB1X+3ZrbRLntrU2o1yDlqDrEWAPAm1xKqJvzth/FIkzLdVh2kBuA2D/dDssF81bdfTXHs1GEnbFO6A1r/Cse/EhafEv8V/3zrJrElhcMxowYYR/vxgFblT2jugX6sHDDxRBghyPAD/W5D8DBqSTbBtwFc86/uYFJ11i3ne01FhcYe2nBMrk5/IQxy68v5WHbvyhRfSkQwB7dYgYkEU2btIjDBsJjQAWQcqlOctOFMQqAneseNKf5/WOQymbT3ERE/9VuCb0LYZbFaVyL51Kp5glVvFCED3qXuUgQhago81yPKSksSdQD82w/iXQV2wBoQt+irbtak0D7aDtUQ4=\"}],\"legacy\":false}");
    }
}
