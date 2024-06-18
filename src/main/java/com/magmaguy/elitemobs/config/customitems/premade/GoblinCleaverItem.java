package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class GoblinCleaverItem extends CustomItemsConfigFields {
    public GoblinCleaverItem() {
        super("goblin_cleaver",
                true,
                Material.GOLDEN_AXE,
                "&8Goblin Cleaver",
                List.of("&8A treasure among goblins!"));
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMaterial(Material.NETHERITE_AXE);
        setEnchantments(Arrays.asList("DAMAGE_ALL,1", "DAMAGE_UNDEAD,1", "UNBREAKING,1", "KNOCKBACK,1", "LOOT_BONUS_MOBS,5"));
        setPotionEffects(List.of("POISON,0,target,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
