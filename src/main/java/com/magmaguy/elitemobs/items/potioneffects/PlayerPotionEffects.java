package com.magmaguy.elitemobs.items.potioneffects;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.potioneffects.custom.Harm;
import com.magmaguy.elitemobs.items.potioneffects.custom.Heal;
import com.magmaguy.elitemobs.items.potioneffects.custom.Saturation;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by MagmaGuy on 14/03/2017.
 */
public class PlayerPotionEffects implements Listener {

    private static final int STANDARD_REFRESH_THRESHOLD_TICKS = 20;
    private static final int NIGHT_VISION_REFRESH_THRESHOLD_TICKS = 12 * 20;
    private final Map<UUID, Map<PotionEffectType, AppliedContinuousEffect>> appliedContinuousEffects = new HashMap<>();

    public PlayerPotionEffects() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ElitePlayerInventory inventory = ElitePlayerInventory.playerInventories.get(player.getUniqueId());
                    if (player.isDead() || inventory == null || PlayerData.getPlayerData(player.getUniqueId()) == null) {
                        reconcileContinuousPotionEffects(player, Map.of());
                        continue;
                    }

                    applyContinuousPotionEffects(inventory.getContinuousPotionEffects(true), player);
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
    }

    public static void addOnHitCooldown(Set<UUID> cooldownList, Player player, long delay) {
        UUID playerUUID = player.getUniqueId();
        cooldownList.add(playerUUID);
        new BukkitRunnable() {
            @Override
            public void run() {
                cooldownList.remove(playerUUID);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, delay);
    }

    private void applyContinuousPotionEffects(List<ElitePotionEffect> elitePotionEffects, Player player) {
        Map<PotionEffectType, ElitePotionEffect> desiredPotionEffects = new HashMap<>();

        for (ElitePotionEffect elitePotionEffect : elitePotionEffects) {
            PotionEffect potionEffect = elitePotionEffect.getPotionEffect();
            if (potionEffect == null) continue;

            PotionEffectType potionEffectType = potionEffect.getType();
            // These attributes cannot be maintained correctly through Bukkit potion effects.
            if (potionEffectType.equals(PotionEffectType.ABSORPTION) ||
                    potionEffectType.equals(PotionEffectType.HEALTH_BOOST))
                continue;

            if (potionEffectType.equals(PotionEffectType.INSTANT_HEALTH)) {
                Heal.doHeal(player, elitePotionEffect);
                continue;
            }
            if (potionEffectType.equals(PotionEffectType.SATURATION)) {
                Saturation.doSaturation(player, elitePotionEffect);
                continue;
            }
            if (potionEffectType.equals(PotionEffectType.INSTANT_DAMAGE)) {
                Harm.doHarm(player, elitePotionEffect);
                continue;
            }

            desiredPotionEffects.merge(potionEffectType, elitePotionEffect,
                    PlayerPotionEffects::strongerPotionEffect);
        }

        reconcileContinuousPotionEffects(player, desiredPotionEffects);
    }

    private void reconcileContinuousPotionEffects(Player player,
                                                  Map<PotionEffectType, ElitePotionEffect> desiredPotionEffects) {
        UUID playerUUID = player.getUniqueId();
        Map<PotionEffectType, AppliedContinuousEffect> appliedEffects =
                appliedContinuousEffects.get(playerUUID);

        if (appliedEffects == null && desiredPotionEffects.isEmpty()) return;
        if (appliedEffects == null) {
            appliedEffects = new HashMap<>();
            appliedContinuousEffects.put(playerUUID, appliedEffects);
        }

        Iterator<Map.Entry<PotionEffectType, AppliedContinuousEffect>> iterator =
                appliedEffects.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<PotionEffectType, AppliedContinuousEffect> entry = iterator.next();
            PotionEffectType potionEffectType = entry.getKey();
            AppliedContinuousEffect appliedEffect = entry.getValue();
            ElitePotionEffect desiredEffect = desiredPotionEffects.get(potionEffectType);
            PotionEffect currentEffect = player.getPotionEffect(potionEffectType);

            if (desiredEffect == null) {
                if (appliedEffect.matchesCurrentLease(currentEffect))
                    player.removePotionEffect(potionEffectType);
                iterator.remove();
                continue;
            }

            // Another source replaced the effect. Relinquish ownership so EliteMobs never removes it.
            if (!appliedEffect.matchesCurrentLease(currentEffect)) {
                iterator.remove();
                continue;
            }

            // The strongest equipped source changed. Remove only the lease EliteMobs owns, then
            // let the desired-effect pass below install the replacement.
            if (!appliedEffect.hasSameDefinition(desiredEffect.getPotionEffect())) {
                player.removePotionEffect(potionEffectType);
                iterator.remove();
            }
        }

        for (Map.Entry<PotionEffectType, ElitePotionEffect> entry : desiredPotionEffects.entrySet()) {
            PotionEffectType potionEffectType = entry.getKey();
            PotionEffect desiredEffect = entry.getValue().getPotionEffect();
            AppliedContinuousEffect appliedEffect = appliedEffects.get(potionEffectType);
            PotionEffect currentEffect = player.getPotionEffect(potionEffectType);

            if (appliedEffect != null && appliedEffect.matchesCurrentLease(currentEffect)) {
                if (currentEffect.getDuration() > refreshThreshold(potionEffectType, desiredEffect))
                    continue;
                player.removePotionEffect(potionEffectType);
                currentEffect = null;
                appliedEffects.remove(potionEffectType);
            }

            // Preserve potion effects from vanilla or other plugins. Bukkit does not expose an
            // effect source, so only an effect previously leased by this instance is replaceable.
            if (currentEffect != null) continue;

            if (player.addPotionEffect(desiredEffect))
                appliedEffects.put(potionEffectType, new AppliedContinuousEffect(desiredEffect));
        }

        if (appliedEffects.isEmpty())
            appliedContinuousEffects.remove(playerUUID);
    }

    private static ElitePotionEffect strongerPotionEffect(ElitePotionEffect first, ElitePotionEffect second) {
        PotionEffect firstEffect = first.getPotionEffect();
        PotionEffect secondEffect = second.getPotionEffect();
        if (secondEffect.getAmplifier() != firstEffect.getAmplifier())
            return secondEffect.getAmplifier() > firstEffect.getAmplifier() ? second : first;
        return secondEffect.getDuration() > firstEffect.getDuration() ? second : first;
    }

    private static int refreshThreshold(PotionEffectType potionEffectType, PotionEffect desiredEffect) {
        if (potionEffectType.equals(PotionEffectType.NIGHT_VISION))
            return NIGHT_VISION_REFRESH_THRESHOLD_TICKS;
        return Math.min(STANDARD_REFRESH_THRESHOLD_TICKS, Math.max(1, desiredEffect.getDuration() / 2));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeOwnedContinuousEffects(event.getPlayer());
    }

    private void removeOwnedContinuousEffects(Player player) {
        Map<PotionEffectType, AppliedContinuousEffect> appliedEffects =
                appliedContinuousEffects.remove(player.getUniqueId());
        if (appliedEffects == null) return;

        for (Map.Entry<PotionEffectType, AppliedContinuousEffect> entry : appliedEffects.entrySet())
            if (entry.getValue().matchesCurrentLease(player.getPotionEffect(entry.getKey())))
                player.removePotionEffect(entry.getKey());
    }

    private record AppliedContinuousEffect(PotionEffect appliedEffect) {

        private boolean hasSameDefinition(PotionEffect potionEffect) {
            return potionEffect != null &&
                    appliedEffect.getType().equals(potionEffect.getType()) &&
                    appliedEffect.getAmplifier() == potionEffect.getAmplifier() &&
                    appliedEffect.getDuration() == potionEffect.getDuration() &&
                    appliedEffect.isAmbient() == potionEffect.isAmbient() &&
                    appliedEffect.hasParticles() == potionEffect.hasParticles() &&
                    appliedEffect.hasIcon() == potionEffect.hasIcon();
        }

        private boolean matchesCurrentLease(PotionEffect potionEffect) {
            return potionEffect != null &&
                    appliedEffect.getType().equals(potionEffect.getType()) &&
                    appliedEffect.getAmplifier() == potionEffect.getAmplifier() &&
                    potionEffect.getDuration() > 0 &&
                    potionEffect.getDuration() <= appliedEffect.getDuration() &&
                    appliedEffect.isAmbient() == potionEffect.isAmbient() &&
                    appliedEffect.hasParticles() == potionEffect.hasParticles() &&
                    appliedEffect.hasIcon() == potionEffect.hasIcon();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerHitWithPotionEffect(EntityDamageByEntityEvent event) {
        LivingEntity damager = EntityFinder.getRealDamager(event);
        if (damager == null || !damager.getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) damager;

        //citizens
        if (player.hasMetadata("NPC"))
            return;

        if (ElitePlayerInventory.playerInventories.get(player.getUniqueId()) == null) return;

        LivingEntity damagee;
        if (event.getEntity() instanceof LivingEntity)
            damagee = (LivingEntity) event.getEntity();
        else
            return;

        for (ElitePotionEffect elitePotionEffect : ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getOnHitPotionEffects(true))
            doOnHitPotionEffect(elitePotionEffect, player, damagee);
    }

    private void doOnHitPotionEffect(ElitePotionEffect elitePotionEffect, Player player, LivingEntity damagee) {
        //This one doesn't work
        if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.ABSORPTION)) return;
        if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.HEALTH_BOOST)) return;
        switch (elitePotionEffect.getTarget()) {
            case SELF:
                if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.INSTANT_HEALTH)) {
                    Heal.doHeal(player, elitePotionEffect);
                    break;
                }

                if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.SATURATION)) {
                    Saturation.doSaturation(player, elitePotionEffect);
                    break;
                }

                if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.INSTANT_DAMAGE)) {
                    Harm.doHarm(player, elitePotionEffect);
                    return;
                }
                player.addPotionEffect(elitePotionEffect.getPotionEffect());
                break;
            case TARGET:
                if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.LEVITATION) ||
                        elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.SLOWNESS) ||
                        elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.BLINDNESS)) {
                    EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(damagee);
                    if (eliteEntity != null && eliteEntity.getHealthMultiplier() > 1)
                        return;
                }
                damagee.addPotionEffect(elitePotionEffect.getPotionEffect());
                break;
        }

    }

}
