package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class WoodLeagueWave6Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave6Ranged() {
        super("wood_league_wave_6_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Crossbowman",
                "6");
        setFollowDistance(60);

        setMainHand(new ItemStack(Material.CROSSBOW));
        setDamageMultiplier(0.5D);
        setHealthMultiplier(0.75D);
    }
}
