package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WoodLeagueWave12Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave12Ranged() {
        super("wood_league_wave_12_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Crossbowman",
                "12");
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
        setMainHand(new ItemStack(Material.CROSSBOW));
        setPowers(List.of("attack_vacuum.yml"));
    }
}
