package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
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
        boolean isWeaponMaterial = EliteItemManager.isWeapon(itemStack);
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
        if (!ItemTagger.isEliteItem(itemStack)) return false;
        if (!(itemStack.getItemMeta() instanceof Damageable)) return false;
        if (itemStack.getType().getMaxDurability() == 0) return false;
        return ((Damageable) itemStack.getItemMeta()).getDamage() + 1 >= itemStack.getType().getMaxDurability();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDurabilityLoss(PlayerItemDamageEvent event) {
        if (!EliteItemManager.isEliteMobsItem(event.getItem())) return;
        event.setCancelled(true);
    }

    public static void doDurabilityLoss(Player player) {
        if (!ItemSettingsConfig.isEliteDurability()) return;
        ArrayList<ItemStack> itemsList = new ArrayList<>(Arrays.asList(player.getInventory().getArmorContents()));
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
                    } else
                        itemStack.setAmount(0);
            }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        doDurabilityLoss(event.getEntity());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        //citizens spams this really hard for some reason
        if (event.getEntity().hasMetadata("NPC")) return;
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
        Player player = (Player) livingEntity;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (isOnLastDamage(itemStack)) {
            player.getWorld().dropItem(player.getLocation(), itemStack.clone());
            itemStack.setAmount(0);
            player.sendMessage(ChatColorConverter.convert(ItemSettingsConfig.getLowWeaponDurabilityItemDropMessage()));
            event.setCancelled(true);
        }
    }
}
