package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

public class ScrollApplier  extends NPCsConfigFields {
    public ScrollApplier() {
        super("scroll_applier_config",
                true,
                "<g:#4A7A7A:#5A8A8A>Scotty</g>",
                "<g:#3A6A6A:#4A7A7A><Scroll Applier></g>",
                Villager.Profession.WEAPONSMITH,
                "em_adventurers_guild,281.5,74,244.5,0,0",
                new ArrayList<>(List.of(
                        "Got elite scrolls?",
                        "Is it elite scroll time?",
                        "Good vanilla item you want converted?")),
                new ArrayList<>(List.of(
                        "Need a vanilla item converted?",
                        "Want to hit elite mobs harder?",
                        "Good vanilla item you want converted?")),
                new ArrayList<>(List.of(
                        "Come back when you have elite scrolls!")),
                true,
                3,
                NPCInteractions.NPCInteractionType.SCROLL_APPLIER);
        setDisguise("custom:ag_back_teleporter");
        setCustomDisguiseData("player ag_back_teleporter setskin {\"id\":\"884501c8-4d93-46bc-85fb-2028723de920\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTYxNDQ3NTk0OTk1NiwKICAicHJvZmlsZUlkIiA6ICJkZTU3MWExMDJjYjg0ODgwOGZlN2M5ZjQ0OTZlY2RhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfTWluZXNraW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzE1YjRjNzk0NzJiMTVlZmRkOTRmZDQ2MmMxZGYzYTM2MjhhNjY2MDVkYjk5NjQ3ZTNhYzA5MzJiYTZlNzk0ZSIKICAgIH0KICB9Cn0=\",\"signature\":\"hAAiBL8a0UBXNdyYehx9AjWsz7EzhbnyldVEZPx2pd8N76TCgscu2ad8ypKj961D8iBl7u3GMmAtoAQtDSMIfaA8GteGu7nhBE4f9/+QXtzkFcudbk7C+Hq8LVLvWsbmVywrsNiqQ+6KFdT7/YnhPXO1xO1EjsnRbuynN/FhlJWdWwBLjuA9e/QA2LH8OgHHdAA73/gJfxH+5isc4sy0dVo+KwUjKaPGDem+eB2JlwSAfJECj9PiFNbdDKcjFNMVBQJOUZmrWH1eW4fYgk4I3mQ6uURmwp2Tp0zLu9Sgt0+wDNbYFb33x2XN4veDdEqTLoE55XqpSRmQAi9dAtyjiOc+3ATDV9pewcAjT+yk6IN5gR0qU36UM4E2uoZ+2tvgOlm9ZOoxx8nv0fWLUNEBnR16n8LCceisDh9KG8+MBtxvPLZnkghBNJmy6S6Fm1aPq4dZmCV/PlxMznN4ys/eG/QrmYguO8Wf9jNQiO47x745fBaD53W5A27GH7tChNgDFJ6pF3U/cfa0HdH5ddkGCYlfevWkOhEKdTxC//+R7+ryyaiBLsr1vTQvgTWYdvZKWIW8QLTTVktkhdZIApoTJMD8HxdvdxhulpyCIFBxkW+iXBkJLfBA93LrXvkNRmUoXl2OIyAxekhoL1Hj753l9PDkz4sqLx1nA18cb4TyQ0U=\"}],\"legacy\":false}");
    }
}
