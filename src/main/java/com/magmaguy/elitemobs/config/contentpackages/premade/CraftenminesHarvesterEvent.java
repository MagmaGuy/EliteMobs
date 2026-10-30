package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;

import java.util.List;

public class CraftenminesHarvesterEvent extends ContentPackagesConfigFields {
    public CraftenminesHarvesterEvent() {
        super("em_craftemines_harvester_event", true);
        setName("&6Craftenmine's Harvester Event");
        setCustomInfo(List.of(
                "&7A bio-mechanical scarecrow harvester stalks the world,",
                "&7reaping grass, trees and mobs to grow ever stronger.",
                "&6Credits: MagmaGuy, Dali, Frostcone, CraftenMine"));
        setCustomEventFilenames(List.of("em_craftemines_harvester_event.yml"));
        setSetupMenuDescription(List.of(
                "&6Face Craftenmine's Harvester mkIII!",
                "&cDestructive: it breaks the terrain it harvests."));
        setDefaultDungeon(true);
        setNightbreakSlug("craftenmines-harvester-event");
    }
}
