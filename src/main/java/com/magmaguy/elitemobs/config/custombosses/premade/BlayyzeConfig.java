package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class BlayyzeConfig extends CustomBossConfigFields {
    public BlayyzeConfig() {
        super("blayyze",
                EntityType.BLAZE.toString(),
                true,
                "&6Blayyze",
                "dynamic",
                30,
                true,
                4,
                0.5,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                Arrays.asList("meteor_shower.yml", "summon_embers.yml", "bullet_hell.yml", "spirit_walk.yml"),
                "&cSomething came out of the meteorite's crater...",
                "&6$players completed first contact.",
                "&6The ayyliens have been taken to area 51!",
                "&6????: $location",
                Arrays.asList("meteor_shower_scroll.yml:1"),
                true,
                true,
                Arrays.asList(Material.FIRE_CHARGE.toString()),
                null,
                null);
    }
}
