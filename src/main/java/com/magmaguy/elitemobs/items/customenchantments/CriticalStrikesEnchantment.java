package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.config.enchantments.premade.CriticalStrikesConfig;
import com.magmaguy.elitemobs.utils.DialogArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;


public class CriticalStrikesEnchantment extends CustomEnchantment {

    public static String key = "critical_strikes";

    public CriticalStrikesEnchantment() {
        super(key, true);
    }

    public static void criticalStrikePopupMessage(Entity entity, Vector offset) {
        DialogArmorStand.createDialogArmorStand(entity, CriticalStrikesConfig.getCriticalHitPopup(), offset);
    }

}
