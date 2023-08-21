package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class InvictusLeggingsConfig extends CustomItemsConfigFields {
    public InvictusLeggingsConfig() {
        super("invictus_leggings", true, Material.DIAMOND_LEGGINGS, "&4Invictus Leggings", Arrays.asList("&2Awarded to the champions of the", "&2Wood League Arena!"));
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMaterial(Material.NETHERITE_LEGGINGS);
        setEnchantments(Arrays.asList("PROTECTION_ENVIRONMENTAL,5", "PROTECTION_EXPLOSIONS,4", "PROTECTION_PROJECTILE,4", "MENDING,1", "DURABILITY,5"));
        setPotionEffects(List.of("JUMP,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(50);
    }
}
