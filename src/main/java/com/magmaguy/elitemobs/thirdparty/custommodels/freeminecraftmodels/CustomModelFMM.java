package com.magmaguy.elitemobs.thirdparty.custommodels.freeminecraftmodels;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.admin.RemoveCommand;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModelInterface;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityHitByProjectileEvent;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityLeftClickEvent;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityManager;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityRightClickEvent;
import com.magmaguy.freeminecraftmodels.customentity.DynamicEntity;
import com.magmaguy.freeminecraftmodels.customentity.ModeledEntity;
import com.magmaguy.freeminecraftmodels.customentity.ModeledEntityLeftClickCallback;
import com.magmaguy.freeminecraftmodels.customentity.ModeledEntityRightClickCallback;
import com.magmaguy.freeminecraftmodels.customentity.core.Bone;
import com.magmaguy.freeminecraftmodels.customentity.core.OBBHitDetection;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomModelFMM implements CustomModelInterface {
    @Getter
    private final DynamicEntity dynamicEntity;

    public CustomModelFMM(LivingEntity livingEntity, String modelName, String nametagName) {
        dynamicEntity = DynamicEntity.create(modelName, livingEntity);
        if (dynamicEntity == null) return;
        dynamicEntity.setDisplayName(nametagName);
    }

    public CustomModelFMM(LivingEntity livingEntity, String modelName, String nametagName,
                          ModeledEntityLeftClickCallback leftClickCallback,
                          ModeledEntityRightClickCallback rightClickCallback) {
        dynamicEntity = DynamicEntity.create(modelName, livingEntity);
        if (dynamicEntity == null) return;
        dynamicEntity.setDisplayName(nametagName);
        if (leftClickCallback != null) dynamicEntity.setLeftClickCallback(leftClickCallback);
        if (rightClickCallback != null) dynamicEntity.setRightClickCallback(rightClickCallback);
    }

    public static void reloadModels() {
        ModeledEntityManager.reload();
    }

    public static boolean modelExists(String modelName) {
        return ModeledEntityManager.modelExists(modelName);
    }

    @Override
    public void shoot() {
        if (dynamicEntity == null) return;
        if (dynamicEntity.hasAnimation("attack_ranged")) dynamicEntity.playAnimation("attack_ranged", false, false);
        else if (dynamicEntity.hasAnimation("attack")) dynamicEntity.playAnimation("attack", false, false);
    }

    @Override
    public void melee() {
        if (dynamicEntity == null) return;
        if (dynamicEntity.hasAnimation("attack_melee")) dynamicEntity.playAnimation("attack_melee", false, false);
        else if (dynamicEntity.hasAnimation("attack")) dynamicEntity.playAnimation("attack", false, false);
    }

    @Override
    public void playAnimationByName(String animationName) {
        if (dynamicEntity == null) return;
        if (!dynamicEntity.hasAnimation(animationName)) return;
        dynamicEntity.playAnimation(animationName, false, false);
    }

    @Override
    public void setName(String nametagName, boolean visible) {
        if (dynamicEntity == null) return;
        dynamicEntity.setDisplayName(nametagName);
    }

    @Override
    public void setNameVisible(boolean visible) {
        if (dynamicEntity == null) return;
        dynamicEntity.setDisplayNameVisible(visible);
    }

    @Override
    public void addPassenger(CustomBossEntity passenger) {
        //currently unimplemented
    }

    @Override
    public void switchPhase() {
        if (dynamicEntity == null) return;
        dynamicEntity.remove();
    }

    @Override
    public Location getNametagBoneLocation() {
        if (dynamicEntity == null) return null;
        List<Bone> nametagBones = dynamicEntity.getNametagBones();
        if (nametagBones == null || nametagBones.isEmpty()) return null;
        return nametagBones.get(0).getBoneLocation();
    }

    @Override
    public void setSyncMovement(boolean syncMovement) {
        if (dynamicEntity == null) return;
        dynamicEntity.setSyncMovement(syncMovement);
    }

    /**
     * Listens for {@link com.magmaguy.freeminecraftmodels.api.FmmReloadedEvent}
     * and re-attaches the FMM display to every tracked NPC. Required because
     * FMM's reload tears down every {@code DynamicEntity}, leaving every NPC's
     * cached {@code customModel} pointing at a destroyed display entity — the
     * villager survives but is no longer visually wrapped, so it shows as an
     * invisible mob to viewers.
     * <p>
     * Registered in EventsRegistrer only when this class loads (i.e. FMM is on
     * the classpath); without FMM the class is unreferenced and no listener
     * registers.
     */
    public static class FmmReloadListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
        public void onFmmReloaded(com.magmaguy.freeminecraftmodels.api.FmmReloadedEvent event) {
            for (com.magmaguy.elitemobs.npcs.NPCEntity npc :
                    new java.util.ArrayList<>(com.magmaguy.elitemobs.entitytracker.EntityTracker.getNpcEntities().values())) {
                try {
                    npc.refreshCustomModel();
                } catch (Throwable t) {
                    com.magmaguy.magmacore.util.Logger.warn(
                            "Failed to refresh FMM model after reload for NPC " + npc.getUuid() + ": " + t.getMessage());
                }
            }
        }
    }

    public static class FmmRemoveBridge implements Listener {
        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
        public void onFmmLeftClick(ModeledEntityLeftClickEvent event) {
            removeModeledTarget(event.getPlayer(), event.getEntity(), event);
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
        public void onFmmRightClick(ModeledEntityRightClickEvent event) {
            removeModeledTarget(event.getPlayer(), event.getEntity(), event);
        }

        private void removeModeledTarget(Player player, ModeledEntity modeledEntity, Cancellable event) {
            if (!RemoveCommand.isRemoving(player)) return;
            Entity underlyingEntity = modeledEntity.getUnderlyingEntity();
            if (underlyingEntity == null) return;
            if (!RemoveCommand.removeTrackedEntity(player, underlyingEntity)) return;
            event.setCancelled(true);
        }
    }

    /**
     * Routes FMM OBB projectile hits on EliteMobs-backed models through the normal
     * Bukkit projectile damage event. FMM's default modeled-hit callback applies a
     * small raw damage value directly; that can tint the model, but it does not
     * reliably enter the EliteMobs player-vs-elite pipeline, so damage popups and
     * health-bar updates never fire. By owning the FMM event first and cancelling it,
     * EliteMobs gets exactly the same downstream path as a vanilla arrow hit on the
     * underlying entity.
     */
    public static class FmmProjectileDamageBridge implements Listener {
        private static final Set<UUID> BRIDGED_PROJECTILES = ConcurrentHashMap.newKeySet();

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onFmmProjectileHit(ModeledEntityHitByProjectileEvent event) {
            Entity underlying = event.getEntity().getUnderlyingEntity();
            if (!(underlying instanceof LivingEntity)) return;

            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(underlying);
            if (eliteEntity == null || !eliteEntity.isValid()) return;

            Projectile projectile = event.getProjectile();
            if (!(projectile.getShooter() instanceof Player player)) return;

            event.setCancelled(true);
            UUID projectileId = projectile.getUniqueId();
            if (!BRIDGED_PROJECTILES.add(projectileId)) return;
            new BukkitRunnable() {
                @Override
                public void run() {
                    BRIDGED_PROJECTILES.remove(projectileId);
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20L);

            double damage = 1.0;
            if (projectile instanceof AbstractArrow arrow) {
                damage = Math.max(1.0, Math.ceil(arrow.getDamage() * projectile.getVelocity().length()));
            }

            boolean previousApplyDamage = OBBHitDetection.applyDamage;
            boolean previousBypassProjectileRedirect = OBBHitDetection.bypassProjectileRedirect;
            OBBHitDetection.applyDamage = true;
            OBBHitDetection.bypassProjectileRedirect = true;
            try {
                EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter
                        .applyModeledProjectileHit(player, eliteEntity, projectile, damage);
            } finally {
                OBBHitDetection.applyDamage = previousApplyDamage;
                OBBHitDetection.bypassProjectileRedirect = previousBypassProjectileRedirect;
            }
            event.getEntity().getSkeleton().tint();
        }
    }
}
