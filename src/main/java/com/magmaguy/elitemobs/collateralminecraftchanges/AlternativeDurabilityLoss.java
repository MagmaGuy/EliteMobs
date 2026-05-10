package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlternativeDurabilityLoss implements Listener {

    //these values are percentual
    private static double durabilityLoss(ItemStack itemStack) {
        boolean isWeaponMaterial = EliteItemManager.isWeapon(itemStack);
        int maxDurability = itemStack.getType().getMaxDurability() > (isWeaponMaterial ? 2000 : 1000) ? (isWeaponMaterial ? 2000 : 1000) : itemStack.getType().getMaxDurability();
        double baseModifier = isWeaponMaterial ? 2000 : 1000;
        double durabilityLoss = ((baseModifier - maxDurability) / baseModifier) * ItemSettingsConfig.getEliteDurabilityMultiplier();
        double durabilityLevel = 1 + (ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.UNBREAKING.getKey()) / 4d);
        double defaultMultiplier = 0.5; //just tweaking defaults
        return durabilityLoss / durabilityLevel * defaultMultiplier;
    }

    public static void doDurabilityLoss(Player player) {
        if (!ItemSettingsConfig.isEliteDurability()) return;
        List<ItemStack> itemsList = new ArrayList<>(Arrays.stream(player.getInventory().getArmorContents()).toList());
        itemsList.add(player.getInventory().getItemInMainHand());
        itemsList.add(player.getInventory().getItemInOffHand());

        for (ItemStack itemStack : itemsList)
            if (itemStack != null &&
                    itemStack.getType().getMaxDurability() != 0 &&
                    EliteItemManager.isEliteMobsItem(itemStack) &&
                    itemStack.getItemMeta() instanceof Damageable damageable) {
                int maxDurability = itemStack.getType().getMaxDurability();
                int durabilityLoss = (int) (maxDurability * durabilityLoss(itemStack));
                int currentDurability = damageable.getDamage();
                int newDurability = currentDurability + durabilityLoss;
                damageable.setDamage(newDurability);
                itemStack.setItemMeta(damageable);
                if (newDurability >= maxDurability)
                    if (ItemSettingsConfig.isPreventEliteItemsFromBreaking()) {
                        damageable.setDamage(maxDurability - 1);
                        itemStack.setItemMeta(damageable);
                    } else {
                        itemStack.setAmount(0);
                    }
            }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDurabilityLoss(PlayerItemDamageEvent event) {
        if (!EliteItemManager.isEliteMobsItem(event.getItem())) return;
        if (!EliteItemManager.isArmor(event.getItem()) && !EliteItemManager.isWeapon(event.getItem())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        doDurabilityLoss(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerShoot(EntityShootBowEvent event) {
        if (!EliteItemManager.isEliteMobsItem(event.getBow())) return;
        if (EliteItemManager.isOnLastDamage(event.getBow())) {
            event.setCancelled(true);
            if (event.getEntity() instanceof Player player) sendBrokenWeaponWarning(player);
        }
    }

    /**
     * Mirror of the bow handler for melee. Cancels EntityDamageByEntityEvent when the
     * attacking player's mainhand is an elite weapon at last durability, so a "broken"
     * elite spear/sword/axe/scythe truly stops dealing damage instead of only losing
     * its cached enchantment effects via {@link com.magmaguy.elitemobs.playerdata.PlayerItem}.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMeleeAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!EliteItemManager.isOnLastDamage(mainHand)) return;
        event.setCancelled(true);
        sendBrokenWeaponWarning(player);
    }

    private static void sendBrokenWeaponWarning(Player player) {
        String warning = ItemSettingsConfig.getBrokenItemSubtitleWarning();
        if (warning == null || warning.isEmpty()) return;
        player.sendTitle("", ChatColorConverter.convert(warning), 0, 20, 10);
    }

}
