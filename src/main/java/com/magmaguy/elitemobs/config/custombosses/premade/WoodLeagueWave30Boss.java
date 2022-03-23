package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave30Boss extends CustomBossesConfigFields {
    public WoodLeagueWave30Boss() {
        super("wood_league_wave_30_boss",
                EntityType.SKELETON,
                true,
                "$bossLevel &2The Southern Viper",
                "30");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.LEATHER_HELMET));
        setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        setBoots(new ItemStack(Material.LEATHER_BOOTS));
        setMainHand(new ItemStack(Material.BOW));
        setOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        setPowers(Arrays.asList("attack_fire.yml", "attack_poison.yml", "shield_wall.yml", "attack_wither.yml", "skeleton_tracking_arrow.yml"));
        setMovementSpeedAttribute(0.6D);
        setHealthMultiplier(5D);
        setDamageMultiplier(2D);
    }
}
