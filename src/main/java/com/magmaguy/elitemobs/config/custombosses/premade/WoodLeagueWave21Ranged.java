package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave21Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave21Ranged() {
        super("wood_league_wave_21_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Crossbowman",
                "21");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.LEATHER_HELMET));
        setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        setBoots(new ItemStack(Material.LEATHER_BOOTS));
        setMainHand(new ItemStack(Material.CROSSBOW));
        setPowers(Arrays.asList("attack_vacuum.yml", "skeleton_tracking_arrow.yml"));
    }
}
