package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class WoodLeagueWave40Boss extends CustomBossesConfigFields {
    public WoodLeagueWave40Boss() {
        super("wood_league_wave_40_boss",
                EntityType.SKELETON,
                true,
                "$bossLevel &cCrucio the Bearer of Curses",
                "40");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        setMainHand(new ItemStack(Material.DIAMOND_SWORD));
        setOffHand(new ItemStack(Material.SHIELD));
        setPowers(Arrays.asList("attack_wither.yml", "attack_poison.yml", "shield_wall.yml", "bullet_hell.yml", "skeleton_tracking_arrow.yml", "flame_pyre.yml"));
        setMovementSpeedAttribute(0.6D);
        setHealthMultiplier(5D);
        setDamageMultiplier(2D);
    }
}
