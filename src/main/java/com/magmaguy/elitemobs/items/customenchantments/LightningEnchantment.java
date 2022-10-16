package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.config.enchantments.premade.LightningConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class LightningEnchantment extends CustomEnchantment {
    public static final String key = "lightning";

    public LightningEnchantment() {
        super(key, false);
    }

    public static void playerLightning(Player player, Location location) {
        LightningSpawnBypass.bypass();
        Objects.requireNonNull(location.getWorld()).strikeLightningEffect(location);
        location.getWorld().getNearbyEntities(location, 2.5, 2.5, 2.5).forEach((entity -> {
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(entity);
            if (eliteEntity == null) return;
            double damage = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getWeaponLevel(true) * 2.5;
            eliteEntity.damage(damage);
        }));
    }

    public static class LightningEnchantmentEvents implements Listener {
        private static final Set<Player> playersInCooldown = new HashSet<>();

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onEntityDamagedByPlayer(EliteMobDamagedByPlayerEvent event) {
            if (event.getPlayer().hasMetadata("NPC") ||
                    ElitePlayerInventory.playerInventories.containsKey(event.getPlayer().getUniqueId())) return;
            if (playersInCooldown.contains(event.getPlayer())) return;
            double lightningChance = ElitePlayerInventory.playerInventories.get(event.getPlayer().getUniqueId()).getLightningChance(true);
            if (lightningChance <= 0) return;
            if (lightningChance >= ThreadLocalRandom.current().nextDouble()) return;
            playersInCooldown.add(event.getPlayer());
            Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> playersInCooldown.remove(event.getPlayer()), LightningConfig.minimumCooldown * 20L);
            playerLightning(event.getPlayer(), event.getEliteMobEntity().getLivingEntity().getLocation());
        }
    }
}
