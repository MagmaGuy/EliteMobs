package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave18Melee extends CustomBossesConfigFields {
    public WoodLeagueWave18Melee() {
        super("wood_league_wave_18_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel &4Fencing Club Enthusiast",
                "18");
        setFollowDistance(60);
        setPowers(new ArrayList<>(List.of("corpse.yml", "attack_fire.yml")));
        setMainHand(new ItemStack(Material.DIAMOND_SWORD));
    }
}
