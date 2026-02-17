package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;

import java.util.ArrayList;
import java.util.List;

public class OasisMetaPack  extends ContentPackagesConfigFields {
    public OasisMetaPack() {
        super("oasis_meta_pack",
                true,
                "&2[025-055] The Oasis Adventure!",
                new ArrayList<>(List.of("The tutorial adventure for players", "new to EliteMobs!")),
                DiscordLinks.premiumMinidungeons,
                new ArrayList<>(List.of(
                        "oasis_adventure.yml",
                        "oasis_pyramid_sanctum.yml"
                )));
        setSetupMenuDescription(List.of(
                "&2An adventure for players from level 20-55!",
                "&2Adventures are massive maps with quests,",
                "&2many bosses and npcs, among other things!"
        ));
        setNightbreakSlug("the-oasis-adventure");
    }
}