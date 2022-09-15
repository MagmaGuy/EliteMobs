package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WoodLeagueWave7Melee extends CustomBossesConfigFields {
    public WoodLeagueWave7Melee() {
        super("wood_league_wave_7_melee",
                EntityType.ZOMBIE,
                true,
                "$normalLevel Arena Zombie",
                "7");
        setFollowDistance(60);
        setPowers(List.of("corpse.yml"));
        setHelmet(new ItemStack(Material.STICK));
    }
}
