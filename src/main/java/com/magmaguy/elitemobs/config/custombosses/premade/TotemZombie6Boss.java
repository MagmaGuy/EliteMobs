package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TotemZombie6Boss extends CustomBossesConfigFields {
    public TotemZombie6Boss() {
        super("totem_zombie_6", EntityType.ZOMBIE, true, "$eventBossLevel Totem Zombie", "dynamic");
        setBaby(true);
        setHelmet(new ItemStack(Material.LEATHER_HELMET));
        setPowers(new ArrayList<>(List.of(("attack_wither.yml"), "bonus_loot.yml")));
        setCullReinforcements(false);
        setFollowDistance(100);
        setDamageMultiplier(1.5);
        setHealthMultiplier(1.5);
    }
}
