package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;

import java.util.ArrayList;
import java.util.List;

public class PrimisMetaPack extends ContentPackagesConfigFields {
    public PrimisMetaPack() {
        super("primis_meta_pack",
                true,
                "&2[000-020] The Primis Adventure!",
                new ArrayList<>(List.of("The tutorial adventure for players", "new to EliteMobs!")),
                DiscordLinks.premiumMinidungeons,
                new ArrayList<>(List.of(
                        "primis_adventure.yml",
                        "primis_blood_temple_sanctum.yml",
                        "primis_gladius_invasion_dungeon.yml"
                )));
        setSetupMenuDescription(List.of(
                "&2A soft tutorial adventure for players between levels 0-20!",
                "&2Adventures are massive maps with quests,",
                "&2many bosses and npcs, among other things!",
                "&2Also has custom models!"
        ));
        setNightbreakSlug("primis-adventure");
    }
}