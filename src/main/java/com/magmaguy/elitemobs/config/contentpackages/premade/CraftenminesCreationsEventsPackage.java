package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;

import java.util.List;

public class CraftenminesCreationsEventsPackage extends ContentPackagesConfigFields {
    public CraftenminesCreationsEventsPackage() {
        super("craftenmines_events", true);
        setCustomEventFilenames(List.of(
                "shrunken_heads_event.yml",
                "telepathic_nightwalker_event.yml",
                "forgotten_experiment_event.yml",
                "failed_pigxperiment_event.yml",
                "craftenmines_bane_event.yml",
                "conjoined_subjects_event.yml",
                "craftenmines_beast_event.yml",
                "altered_enderman_event.yml",
                "rapid_mutator_event.yml",
                "mutated_cod_event.yml",
                "cryocorpse_event.yml"
        ));
        setName("&2Craftenmines Creations Events");
        setCustomInfo(List.of("Horrible creations will haunt your server!!"));
        setDownloadLink("https://nightbreak.io/plugin/elitemobs/#craftenmines-creations");
        setNightbreakSlug("craftenmines-creations");
    }
}