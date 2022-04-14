package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;

import java.util.Arrays;

public class InvictusSwordConfig extends CustomItemsConfigFields {
    public InvictusSwordConfig() {
        super("invictus_sword", true, Material.DIAMOND_SWORD, "&4Invictus Sword", Arrays.asList("&2Awarded to the champions of the", "&2Wood League Arena!"));
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMaterial(Material.NETHERITE_SWORD);
        setEnchantments(Arrays.asList("DAMAGE_ALL,50", "KNOCKBACK,2", "MENDING,1", "LIGHTNING,3", "SWEEPING_EDGE,1", "DURABILITY,5"));
        setPotionEffects(Arrays.asList("WITHER,0,target,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
