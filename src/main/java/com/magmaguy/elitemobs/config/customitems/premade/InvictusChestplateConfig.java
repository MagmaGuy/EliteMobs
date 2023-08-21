package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class InvictusChestplateConfig extends CustomItemsConfigFields {
    public InvictusChestplateConfig() {
        super("invictus_chestplate", true, Material.DIAMOND_CHESTPLATE, "&4Invictus Chestplate", Arrays.asList("&2Awarded to the champions of the", "&2Wood League Arena!"));
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMaterial(Material.NETHERITE_CHESTPLATE);
        setEnchantments(Arrays.asList("PROTECTION_ENVIRONMENTAL,5", "PROTECTION_EXPLOSIONS,4", "PROTECTION_PROJECTILE,4", "MENDING,1", "DURABILITY,5"));
        setPotionEffects(List.of("HEAL,0,self,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(50);
    }
}
