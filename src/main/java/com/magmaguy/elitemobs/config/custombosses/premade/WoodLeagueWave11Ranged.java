package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WoodLeagueWave11Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave11Ranged() {
        super("wood_league_wave_11_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Crossbowman",
                "11");
        setFollowDistance(60);

        setMainHand(new ItemStack(Material.CROSSBOW));
        setPowers(List.of("attack_vacuum.yml"));
    }
}
