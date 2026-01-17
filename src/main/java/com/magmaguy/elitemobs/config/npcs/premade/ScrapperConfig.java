package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

public class ScrapperConfig extends NPCsConfigFields {
    public ScrapperConfig() {
        super("scrapper_config",
                true,
                "Kelly",
                "<Scrapper>",
                Villager.Profession.WEAPONSMITH,
                "em_adventurers_guild,297.5,81,263.5,90,0",
                new ArrayList<>(List.of(
                        "Want to get rid of items?",
                        "Need to recycle items?",
                        "Looking to scrap?",
                        "It's scrap 'o clock!",
                        "Scrap time!")),
                new ArrayList<>(List.of(
                        "Scrappy deals!",
                        "Get your scrap here!",
                        "75% chance to work!",
                        "There are no refunds.",
                        "Recycle, reuse, reduce!")),
                new ArrayList<>(List.of(
                        "Come back when you have\\nmore to scrap",
                        "Don't forget to make that\\nscrap useful!",
                        "Happy scrapping!")),
                true,
                3,
                NPCInteractions.NPCInteractionType.SCRAPPER);
        setDisguise("custom:ag_back_teleporter");
        setCustomDisguiseData("player ag_back_teleporter setskin {\"id\":\"884501c8-4d93-46bc-85fb-2028723de920\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTYxNDQ3NTk0OTk1NiwKICAicHJvZmlsZUlkIiA6ICJkZTU3MWExMDJjYjg0ODgwOGZlN2M5ZjQ0OTZlY2RhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfTWluZXNraW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzE1YjRjNzk0NzJiMTVlZmRkOTRmZDQ2MmMxZGYzYTM2MjhhNjY2MDVkYjk5NjQ3ZTNhYzA5MzJiYTZlNzk0ZSIKICAgIH0KICB9Cn0=\",\"signature\":\"hAAiBL8a0UBXNdyYehx9AjWsz7EzhbnyldVEZPx2pd8N76TCgscu2ad8ypKj961D8iBl7u3GMmAtoAQtDSMIfaA8GteGu7nhBE4f9/+QXtzkFcudbk7C+Hq8LVLvWsbmVywrsNiqQ+6KFdT7/YnhPXO1xO1EjsnRbuynN/FhlJWdWwBLjuA9e/QA2LH8OgHHdAA73/gJfxH+5isc4sy0dVo+KwUjKaPGDem+eB2JlwSAfJECj9PiFNbdDKcjFNMVBQJOUZmrWH1eW4fYgk4I3mQ6uURmwp2Tp0zLu9Sgt0+wDNbYFb33x2XN4veDdEqTLoE55XqpSRmQAi9dAtyjiOc+3ATDV9pewcAjT+yk6IN5gR0qU36UM4E2uoZ+2tvgOlm9ZOoxx8nv0fWLUNEBnR16n8LCceisDh9KG8+MBtxvPLZnkghBNJmy6S6Fm1aPq4dZmCV/PlxMznN4ys/eG/QrmYguO8Wf9jNQiO47x745fBaD53W5A27GH7tChNgDFJ6pF3U/cfa0HdH5ddkGCYlfevWkOhEKdTxC//+R7+ryyaiBLsr1vTQvgTWYdvZKWIW8QLTTVktkhdZIApoTJMD8HxdvdxhulpyCIFBxkW+iXBkJLfBA93LrXvkNRmUoXl2OIyAxekhoL1Hj753l9PDkz4sqLx1nA18cb4TyQ0U=\"}],\"legacy\":false}");
    }
}
