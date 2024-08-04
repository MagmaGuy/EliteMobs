package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WoodLeagueWave16Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave16Ranged() {
        super("wood_league_wave_16_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Mr. Oinkers Fan",
                "16");
        setFollowDistance(60);

        setMainHand(new ItemStack(Material.CROSSBOW));
        setPowers(List.of("attack_vacuum.yml"));
        setDamageMultiplier(0.5D);
        setHealthMultiplier(0.75D);
    }
}
