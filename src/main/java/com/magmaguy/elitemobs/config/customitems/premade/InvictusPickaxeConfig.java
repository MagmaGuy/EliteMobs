package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class InvictusPickaxeConfig extends CustomItemsConfigFields {
    public InvictusPickaxeConfig() {
        super("invictus_pickaxe", true, Material.DIAMOND_PICKAXE, "&4Invictus Pickaxe", Arrays.asList("&2Awarded to the champions of the", "&2Wood League Arena!"));
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMaterial(Material.NETHERITE_PICKAXE);
        setEnchantments(Arrays.asList("DIG_SPEED,6", "DRILLING,3", "UNBREAKING,5"));
        setPotionEffects(List.of("FAST_DIGGING,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
