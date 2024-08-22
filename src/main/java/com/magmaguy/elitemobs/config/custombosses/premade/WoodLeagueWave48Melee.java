package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave48Melee extends CustomBossesConfigFields {
    public WoodLeagueWave48Melee() {
        super("wood_league_wave_48_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Arena Zombie",
                "48");
        setFollowDistance(60);
        setPowers(new ArrayList<>(List.of("corpse.yml", "attack_fire.yml", "invulnerability_arrow.yml")));
        setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        setMainHand(new ItemStack(Material.DIAMOND_SWORD));
        setMovementSpeedAttribute(0.3D);
    }
}
