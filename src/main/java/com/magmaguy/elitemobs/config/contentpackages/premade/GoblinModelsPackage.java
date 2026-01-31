package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;

import java.util.List;

public class GoblinModelsPackage extends ContentPackagesConfigFields {
    public GoblinModelsPackage() {
        super("goblin_models", true);
        setModelNames(List.of(
                "em_goblin_coins",
                "em_goblin_armor",
                "em_goblin_treasure",
                "em_goblin_charms",
                "em_goblin_weapon"
        ));
        setName("&2Goblin Models");
        setCustomInfo(List.of("Custom models for the goblins events!"));
        setSetupMenuDescription(List.of(
                "&2Makes goblins from EliteMobs events look real fancy!"));
        setDownloadLink("https://nightbreak.io/plugin/elitemobs/#goblin-models");
        setNightbreakSlug("goblin-models");
    }
}