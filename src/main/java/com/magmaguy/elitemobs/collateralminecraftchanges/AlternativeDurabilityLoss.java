package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.EliteMobsItemDetector;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.Arrays;

public class AlternativeDurabilityLoss implements Listener {

    //these values are percentual
    private static double durabilityLoss(ItemStack itemStack) {
        boolean isWeaponMaterial = ItemTierFinder.isWeaponMaterial(itemStack);
        int maxDurability = itemStack.getType().getMaxDurability() > (isWeaponMaterial ? 2000 : 1000) ? (isWeaponMaterial ? 2000 : 1000) : itemStack.getType().getMaxDurability();
        double baseModifier = isWeaponMaterial ? 2000 : 1000;
        double durabilityLoss = ((baseModifier - maxDurability) / baseModifier) * ItemSettingsConfig.getEliteDurabilityMultiplier();
        double durabilityLevel = 1 + (ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.DURABILITY.getKey()) / 4d);
        double defaultMultiplier = 0.5; //just tweaking defaults
        return durabilityLoss / durabilityLevel * defaultMultiplier;
    }

    private static boolean isOnLastDamage(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (!itemStack.hasItemMeta()) return false;
        if (!(itemStack.getItemMeta() instanceof Damageable)) return false;
        if (((Damageable) itemStack.getItemMeta()).getDamage() + 1 < itemStack.getType().getMaxDurability())
            return false;
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDurabilityLoss(PlayerItemDamageEvent event) {
        if (!EliteMobsItemDetector.isEliteMobsItem(event.getItem())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        ArrayList<ItemStack> itemsList = new ArrayList<>(Arrays.asList(event.getEntity().getInventory().getArmorContents()));
        itemsList.add(event.getEntity().getInventory().getItemInMainHand());
        itemsList.add(event.getEntity().getInventory().getItemInOffHand());

        for (ItemStack itemStack : itemsList)
            if (itemStack != null &&
                    itemStack.getType().getMaxDurability() != 0 &&
                    EliteMobsItemDetector.isEliteMobsItem(itemStack) &&
                    itemStack.getItemMeta() instanceof Damageable) {
                Damageable damageable = (Damageable) itemStack.getItemMeta();
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
                    } else
                        itemStack.setAmount(0);
            }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();
        for (ItemStack itemStack : player.getInventory().getArmorContents())
            if (isOnLastDamage(itemStack)) {
                player.getWorld().dropItem(player.getLocation(), itemStack.clone());
                itemStack.setAmount(0);
                player.sendMessage(ChatColorConverter.convert(ItemSettingsConfig.getLowArmorDurabilityItemDropMessage()));
            }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        LivingEntity livingEntity = EntityFinder.filterRangedDamagers(event.getDamager());
        if (livingEntity == null) return;
        if (!livingEntity.getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getDamager();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (isOnLastDamage(itemStack)) {
            player.getWorld().dropItem(player.getLocation(), itemStack.clone());
            itemStack.setAmount(0);
            player.sendMessage(ChatColorConverter.convert(ItemSettingsConfig.getLowWeaponDurabilityItemDropMessage()));
            event.setCancelled(true);
        }
    }
}
