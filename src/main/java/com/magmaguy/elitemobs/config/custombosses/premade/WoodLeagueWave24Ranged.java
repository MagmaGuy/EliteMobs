package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WoodLeagueWave24Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave24Ranged() {
        super("wood_league_wave_24_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Crossbowman",
                "24");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.LEATHER_HELMET));
        setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        setBoots(new ItemStack(Material.LEATHER_BOOTS));
        setMainHand(new ItemStack(Material.CROSSBOW));
        setPowers(new ArrayList<>(List.of("attack_vacuum.yml", "skeleton_tracking_arrow.yml")));
    }
}
