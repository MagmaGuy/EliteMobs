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
                "$eventBossLevel &6Blayyze",
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
                null,
                Arrays.asList("meteor_shower.yml", "summon_embers.yml", "bullet_hell.yml", "spirit_walk.yml"),
                "&cSomething came out of the meteorite's crater...",
                "&6$players completed first contact.",
                Arrays.asList(
                        "&e&l---------------------------------------------",
                        "&6The Blayyze has been repelled!",
                        "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                        "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                        "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                        "&aSlayers: $players",
                        "&e&l---------------------------------------------"),
                "&6The ayyliens have been taken to area 51!",
                "&6????: $distance blocks away",
                Arrays.asList("meteor_shower_scroll.yml:1"),
                true,
                true,
                Arrays.asList(Material.FIRE_CHARGE.toString()),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                2);
    }
}
