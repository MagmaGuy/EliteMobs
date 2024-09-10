package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;

import java.util.ArrayList;
import java.util.List;

public class FreeStoryModeDungeonMetaPack extends ContentPackagesConfigFields {
    public FreeStoryModeDungeonMetaPack() {
        super("free_story_mode_dungeon_meta_pack",
                true,
                "&2[000-055] All 10 Free Story Mode Dungeons and Sanctums!",
                new ArrayList<>(List.of("The perfect set of story dungeons", "for players new to EliteMobs!")),
                "https://nightbreak.io/plugin/elitemobs/#story-mode-dungeons",
                new ArrayList<>(List.of(
                        "the_climb_dungeon.yml",
                        "the_cave_sanctum.yml",
                        "the_mines_dungeon.yml",
                        "the_bridge_sanctum.yml",
                        "the_city_dungeon.yml",
                        "the_palace_sanctum.yml",
                        "the_quarry_dungeon.yml",
                        "the_deep_mines_dungeon.yml",
                        "the_nether_wastes_dungeon.yml",
                        "the_nether_bell_sanctum.yml"
                )));
        setSetupMenuDescription(List.of(
                "&2All free story mode dungeons for EliteMobs!",
                "&2Featuring multiple custom soundtracks, models",
                "&2unique powers and elaborate maps, it's a really",
                "&2fun way of experiencing EliteMobs content!"));
    }
}