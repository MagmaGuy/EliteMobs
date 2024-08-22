package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class RabbitCharmConfig extends CustomItemsConfigFields {
    public RabbitCharmConfig() {
        super("rabbit_charm",
                true,
                Material.RABBIT_FOOT,
                "&bRabbit Charm",
                new ArrayList<>(List.of("&aWith this charm, just about", "&aany destination is only a hop,", "&askip and a jump away!")));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setPotionEffects(List.of("JUMP,2,self,continuous"));
        setDropWeight("1");
    }
}
