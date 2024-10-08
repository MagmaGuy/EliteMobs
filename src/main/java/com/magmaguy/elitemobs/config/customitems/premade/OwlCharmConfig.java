package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class OwlCharmConfig extends CustomItemsConfigFields {
    public OwlCharmConfig() {
        super("owl_charm",
                true,
                Material.ELYTRA,
                "&bOwl Charm",
                new ArrayList<>(List.of("&aBecome the ultimate nocturnal", "&apredator!")));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setPotionEffects(new ArrayList<>(List.of("GLOWING,0,target,onHit", "JUMP,2,self,continuous", "NIGHT_VISION,0,self,continuous", "WEAKNESS,0,self,continuous")));
        setDropWeight("1");
    }
}
