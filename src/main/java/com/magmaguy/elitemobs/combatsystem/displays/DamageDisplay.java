package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.customenchantments.CriticalStrikesEnchantment;
import com.magmaguy.elitemobs.utils.DialogArmorStand;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class DamageDisplay implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        if (!MobCombatSettingsConfig.displayDamageOnHit) return;

        double randomCoordX = (ThreadLocalRandom.current().nextDouble() * 2) - 1;
        double randomCoordZ = (ThreadLocalRandom.current().nextDouble() * 2) - 1;

        Vector offset = new Vector(randomCoordX, 1.5, randomCoordZ);

        if (event.isCriticalStrike()) {
            DialogArmorStand.createDialogArmorStand(
                    event.getEntity(),
                    ChatColorConverter.convert(EnchantmentsConfig.getEnchantment("critical_strikes.yml").getFileConfiguration()
                            .getString("criticalHitColor") + "" + ChatColor.BOLD + "" + (int) event.getDamage() + ""), offset);
            CriticalStrikesEnchantment.criticalStrikePopupMessage(event.getEntity(), new Vector(0, 0.2, 0));
            return;
        }

        DialogArmorStand.createDialogArmorStand(event.getEntity(), ChatColor.RED + "" + ChatColor.BOLD + "" + (int) event.getDamage() + "", offset);
    }

}
