package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Collections;

public class SummonableWolfConfig extends CustomBossesConfigFields {
    public SummonableWolfConfig() {
        super("summonable_wolf",
                EntityType.WOLF,
                true,
                "$normalLevel Wolfy",
                "dynamic");
        setTimeout(3);
        setHealthMultiplier(3);
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
        setTrails(Collections.singletonList(Material.BONE.toString()));
        setOnDamagedMessages(Collections.singletonList("Woof!"));
        setOnDamageMessages(Collections.singletonList("Woof!"));
    }
}
