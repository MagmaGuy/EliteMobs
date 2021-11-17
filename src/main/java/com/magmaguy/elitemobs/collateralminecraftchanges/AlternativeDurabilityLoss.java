package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobsItemDetector;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class AlternativeDurabilityLoss implements Listener {
    private static final HashSet<Player> cancelledPlayers = new HashSet<>();

    private static double durabilityLoss(ItemStack itemStack) {
        boolean isWeaponMaterial = ItemTierFinder.isWeaponMaterial(itemStack);
        int maxDurability = itemStack.getType().getMaxDurability() > (isWeaponMaterial ? 2000 : 1000) ? (isWeaponMaterial ? 2000 : 1000) : itemStack.getType().getMaxDurability();
        double baseModifier = isWeaponMaterial ? 2000 : 1000;
        double durabilityLoss = ((baseModifier - maxDurability) / baseModifier) * ItemSettingsConfig.getEliteDurabilityMultiplier();
        double durabilityLevel = 1 + (ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.DURABILITY.getKey()) / 4d);
        return durabilityLoss / durabilityLevel;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDurabilityLoss(PlayerItemDamageEvent event) {
        if (!cancelledPlayers.contains(event.getPlayer())) return;
        if (!EliteMobsItemDetector.isEliteMobsItem(event.getItem())) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEliteDamagedByPlayerEvent(EliteMobDamagedByPlayerEvent event) {
        cancelledPlayers.add(event.getPlayer());
        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> cancelledPlayers.remove(event.getPlayer()), 1);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEliteDamagedByPlayerEvent(PlayerDamagedByEliteMobEvent event) {
        cancelledPlayers.add(event.getPlayer());
        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> cancelledPlayers.remove(event.getPlayer()), 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        ArrayList<ItemStack> itemsList = new ArrayList<>(Arrays.asList(event.getEntity().getInventory().getArmorContents()));
        itemsList.add(event.getEntity().getInventory().getItemInMainHand());
        itemsList.add(event.getEntity().getInventory().getItemInOffHand());

        for (ItemStack itemStack : itemsList)
            if (itemStack != null)
                if (EliteMobsItemDetector.isEliteMobsItem(itemStack))
                    if (itemStack.getItemMeta() instanceof Damageable) {
                        Damageable damageable = (Damageable) itemStack.getItemMeta();
                        int maxDurability = itemStack.getType().getMaxDurability();
                        int durabilityLoss = (int) (maxDurability * durabilityLoss(itemStack));
                        int currentDurability = damageable.getDamage();
                        int newDurability = currentDurability + durabilityLoss;
                        damageable.setDamage(newDurability);
                        itemStack.setItemMeta((ItemMeta) damageable);
                        if (newDurability >= maxDurability)
                            itemStack.setAmount(0);
                    }
    }
}
