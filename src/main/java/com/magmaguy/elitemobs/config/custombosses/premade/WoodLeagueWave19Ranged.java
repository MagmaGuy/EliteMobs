package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoodLeagueWave19Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave19Ranged() {
        super("wood_league_wave_19_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Archery Club Enthusiast",
                "19");
        setFollowDistance(60);

        setMainHand(new ItemStack(Material.CROSSBOW));
        setPowers(new ArrayList<>(List.of("attack_vacuum.yml", "skeleton_tracking_arrow.yml")));
    }
}
