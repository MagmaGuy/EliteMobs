package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;

import java.util.ArrayList;
import java.util.List;

public class StoryModeDungeonsChapter1MetaPackage extends ContentPackagesConfigFields {
    public StoryModeDungeonsChapter1MetaPackage() {
        super(
                "free_story_mode_dungeon_meta_pack.yml", // Internal name matching YAML key
                true, // isEnabled
                "&2[010-055] Story Mode Dungeons: Chapter One!", // Name with formatting
                new ArrayList<>(List.of(
                        "The perfect set of story dungeons", // Description lines
                        "for players new to EliteMobs!"
                )),
                "https://nightbreak.io/plugin/elitemobs/#story-mode-dungeons", // Download link
                new ArrayList<>(List.of(
                        // Contained package files
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
                ))
        );
        setSetupMenuDescription(List.of(
                // Setup menu description
                "&2All free story mode dungeons for EliteMobs!",
                "&2Featuring multiple custom soundtracks, models",
                "&2unique powers and elaborate maps, it''s a really",
                "&2fun way of experiencing EliteMobs content!"
        ));
        setNightbreakSlug("story-mode-dungeons");
    }
}
