package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;

import java.util.List;

public class DefaultEventsPackage extends ContentPackagesConfigFields {
    public DefaultEventsPackage() {
        super("default_events", true);
        setCustomEventFilenames(List.of(
                "armor_goblin.yml",
                "armor_goblin.yml",
                "balrog.yml",
                "beast_master.yml",
                "charms_goblin.yml",
                "coins_goblin.yml",
                "dead_moon.yml",
                "fae.yml",
                "full_moon.yml",
                "killer_rabbit_of_caerbannog.yml",
                "kraken.yml",
                "pillager_caravan.yml",
                "queen_bee.yml",
                "treasure_goblin.yml",
                "weapons_goblin.yml",
                "zombie_totem.yml"
        ));
        setName("&2Default EliteMobs Events");
        setCustomInfo(List.of("All default custom events from EliteMobs!"));
        setDownloadLink(DiscordLinks.premiumMinidungeons);

    }
}
