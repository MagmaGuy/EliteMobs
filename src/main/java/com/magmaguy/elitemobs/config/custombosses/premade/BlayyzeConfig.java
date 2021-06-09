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
                "dynamic");
        setTimeout(30);
        setPersistent(true);
        setHealthMultiplier(4);
        setDamageMultiplier(0.5);
        setPowers(Arrays.asList("meteor_shower.yml", "summon_embers.yml", "bullet_hell.yml", "spirit_walk.yml"));
        setSpawnMessage("&cSomething came out of the meteorite's crater...");
        setDeathMessage("&6$players completed first contact.");
        setDeathMessages(Arrays.asList(
                "&e&l---------------------------------------------",
                "&6The Blayyze has been repelled!",
                "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                "&aSlayers: $players",
                "&e&l---------------------------------------------"));
        setEscapeMessage("&6The ayyliens have been taken to area 51!");
        setLocationMessage("&6????: $distance blocks away");
        setUniqueLootList(Arrays.asList("meteor_shower_scroll.yml:1"));
        setTrails(Arrays.asList(Material.FIRE_CHARGE.toString()));
        setAnnouncementPriority(2);
    }
}
