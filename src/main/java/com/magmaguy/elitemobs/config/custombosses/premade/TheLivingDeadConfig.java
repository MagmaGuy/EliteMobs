package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class TheLivingDeadConfig extends CustomBossesConfigFields {
    public TheLivingDeadConfig() {
        super("the_living_dead",
                EntityType.HUSK,
                true,
                "$reinforcementLevel <g:#808080:#D3D3D3>The Living Dead</g>",
                "dynamic");
        setHelmet(ItemStackGenerator.generateItemStack(Material.CHAINMAIL_HELMET));
        setChestplate(ItemStackGenerator.generateItemStack(Material.CHAINMAIL_CHESTPLATE));
        setLeggings(ItemStackGenerator.generateItemStack(Material.CHAINMAIL_LEGGINGS));
        setBoots(ItemStackGenerator.generateItemStack(Material.CHAINMAIL_BOOTS));
        setPowers(new ArrayList<>(List.of("attack_fire.yml", "corpse.yml", "ground_pound.yml")));
        setFollowDistance(100);
    }
}
