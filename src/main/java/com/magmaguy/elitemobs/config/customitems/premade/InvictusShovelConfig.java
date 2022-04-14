package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;

import java.util.Arrays;

public class InvictusShovelConfig extends CustomItemsConfigFields {
    public InvictusShovelConfig(){
        super("invictus_shovel", true, Material.DIAMOND_SHOVEL, "&4Invictus Shovel", Arrays.asList("&2Awarded to the champions of the", "&2Wood League Arena!"));
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMaterial(Material.NETHERITE_SHOVEL);
        setEnchantments(Arrays.asList("DIG_SPEED,6", "DRILLING,3", "DURABILITY,5"));
        setPotionEffects(Arrays.asList("FAST_DIGGING,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
