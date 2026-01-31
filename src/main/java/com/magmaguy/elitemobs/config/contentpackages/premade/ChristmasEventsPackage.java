package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;

import java.util.List;

public class ChristmasEventsPackage extends ContentPackagesConfigFields {
    public ChristmasEventsPackage() {
        super("christmas_events", true);
        setCustomEventFilenames(List.of(
                "xmas_jolly_old_saint_nick.yml",
                "xmas_the_blizzard.yml",
                "xmas_rudolf_the_red_nosed_reindeer.yml",
                "xmas_workshop_elf_shift_runner.yml",
                "xmas_santas_reindeer.yml",
                "xmas_ghosts_of_christmas.yml",
                "xmas_retired_santa.yml",
                "xmas_santas_little_helper.yml",
                "xmas_mrs_claus.yml",
                "xmas_grinch.yml"
        ));
        setName("&2Christmas Events");
        setCustomInfo(List.of("Christmas-themed events!"));
        setDownloadLink("https://nightbreak.io/plugin/elitemobs/#christmas-events");
        setNightbreakSlug("christmas-events");
    }
}