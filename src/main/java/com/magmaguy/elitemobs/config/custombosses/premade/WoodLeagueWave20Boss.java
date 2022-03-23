package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave20Boss extends CustomBossesConfigFields {
    public WoodLeagueWave20Boss(){
        super("wood_league_wave_20_boss",
                EntityType.ZOMBIE,
                true,
                "$bossLevel &2Zoltan",
                "20");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
        setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
        setBoots(new ItemStack(Material.GOLDEN_BOOTS));
        setMainHand(new ItemStack(Material.GOLDEN_AXE));
        setOffHand(new ItemStack(Material.BOW));
        setPowers(Arrays.asList("skeleton_tracking_arrow.yml", "zombie_bloat.yml", "shield_wall.yml", "bullet_hell.yml"));
        setMovementSpeedAttribute(0.6D);
        setHealthMultiplier(5D);
        setDamageMultiplier(2D);
    }
}
