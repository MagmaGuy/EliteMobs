package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;

import java.util.List;

public class DefaultEventsPackage extends ContentPackagesConfigFields {
    public DefaultEventsPackage() {
        super("default_events", false);
        // Default events removed - package disabled
        setName("&2Default EliteMobs Events");
        setCustomInfo(List.of("Default events have been removed in EliteMobs 10.0.0"));
        setDownloadLink(DiscordLinks.premiumMinidungeons);
    }
}
