package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class GoblinSlasherItem extends CustomItemsConfigFields {
    public GoblinSlasherItem() {
        super("goblin_slasher",
                true,
                Material.GOLDEN_SWORD,
                "&8Goblin Slasher",
                List.of("&8A treasure among goblins!"));
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMaterial(Material.NETHERITE_SWORD);
        setEnchantments(Arrays.asList("DAMAGE_ALL,1", "DAMAGE_UNDEAD,1", "DURABILITY,1", "KNOCKBACK,1", "LOOT_BONUS_MOBS,5"));
        setPotionEffects(List.of("FAST_DIGGING,0,self,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
