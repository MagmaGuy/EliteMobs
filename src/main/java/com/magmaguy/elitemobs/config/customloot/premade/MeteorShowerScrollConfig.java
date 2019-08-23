package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class MeteorShowerScrollConfig extends CustomLootConfigFields {
    public MeteorShowerScrollConfig() {
        super("meteor_shower_scroll",
                true,
                Material.PAPER.toString(),
                "&7Meteor Shower Scroll",
                Arrays.asList("&4Call forth destruction.", "&4Single-use."),
                Arrays.asList("METEOR_SHOWER,1"),
                Arrays.asList(),
                "dynamic",
                "fixed",
                "unique");
    }
}
