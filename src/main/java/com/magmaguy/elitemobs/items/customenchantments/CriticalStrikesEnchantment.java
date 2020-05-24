package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.utils.DialogArmorStand;
import org.bukkit.Location;
import org.bukkit.util.Vector;


public class CriticalStrikesEnchantment extends CustomEnchantment {

    public static String key = "critical_strikes";

    public CriticalStrikesEnchantment() {
        super(key);
    }

    public static void criticalStrikePopupMessage(Location location) {
        DialogArmorStand.createDialogArmorStand(location.clone().subtract(new Vector(0, 0, 0)),
                ChatColorConverter.convert(EnchantmentsConfig.getEnchantment("critical_strikes.yml")
                        .getFileConfiguration().getString("criticalHitPopup")));
    }

}
