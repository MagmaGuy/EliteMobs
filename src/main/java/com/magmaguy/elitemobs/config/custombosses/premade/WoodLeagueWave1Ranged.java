package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class WoodLeagueWave1Ranged extends CustomBossesConfigFields {
    public WoodLeagueWave1Ranged() {
        super("wood_league_wave_1_ranged",
                EntityType.PILLAGER,
                true,
                "$normalLevel Arena Crossbowman",
                "1");
        setMainHand(new ItemStack(Material.BOW));
        setFollowDistance(60);
        setHelmet(new ItemStack(Material.STICK));
    }
}
