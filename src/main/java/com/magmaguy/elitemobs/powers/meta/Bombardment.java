package com.magmaguy.elitemobs.powers.meta;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.EnderDragonPhaseSimplifier;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Bombardment extends MajorPower implements Listener {

    public static ArrayList<String> bombardments = new ArrayList<>();
    public int firingTimer = 0;
    private boolean isActive = false;
    private boolean firing = false;
    private BukkitTask task = null;

    public Bombardment(PowersConfigFields powersConfigFields) {
        super(powersConfigFields);
        bombardments.add(powersConfigFields.getFilename());
    }

    public void activate(EliteEntity eliteEntity) {

        if (isActive) return;
        isActive = true;

        task = new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {

                counter++;

                if (stopCondition(eliteEntity))
                    return;

                if (firing || isInCooldown(eliteEntity))
                    return;

                if (ThreadLocalRandom.current().nextDouble() > 0.1) return;

                if (eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON)) {
                    EnderDragon.Phase phase = ((EnderDragon) eliteEntity.getLivingEntity()).getPhase();
                    if (phase.equals(EnderDragon.Phase.DYING) ||
                            phase.equals(EnderDragon.Phase.HOVER) ||
                            phase.equals(EnderDragon.Phase.ROAR_BEFORE_ATTACK) ||
                            phase.equals(EnderDragon.Phase.FLY_TO_PORTAL) ||
                            phase.equals(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET)) {
                        return;
                    }
                }

                for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(10, 100, 10)) {
                    if (entity.getType().equals(EntityType.PLAYER)) {
                        firing = true;
                        fire(eliteEntity);
                        return;
                    }
                }

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);
    }

    public void deactivate() {
        firing = false;
        isActive = false;
        if (task != null)
            task.cancel();
    }

    private boolean stopCondition(EliteEntity eliteEntity) {
        if (eliteEntity == null ||
                eliteEntity.getLivingEntity() == null ||
                !eliteEntity.isValid() ||
                !eliteEntity.isInCombat()) {
            deactivate();
            return true;
        }

        if (eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON))
            return !EnderDragonPhaseSimplifier.isFlying(
                    ((EnderDragon) eliteEntity.getLivingEntity()).getPhase(),
                    false);

        return false;
    }

    private void fire(EliteEntity eliteEntity) {

        doCooldown(eliteEntity);

        isActive = true;

        firingTimer = 0;

        new BukkitRunnable() {

            @Override
            public void run() {
                firingTimer++;
                if (stopCondition(eliteEntity) || firingTimer > 20 * 5) {
                    cancel();
                    firing = false;
                    return;
                }

                taskBehavior(eliteEntity);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    public abstract void taskBehavior(EliteEntity eliteEntity);

    public static class BombardmentEvents implements Listener {
        @EventHandler
        public void onCombatEnter(EliteMobEnterCombatEvent event) {
            for (String bombardmentPower : bombardments) {
                Bombardment bombardment = (Bombardment) event.getEliteMobEntity().getPower(bombardmentPower);
                if (bombardment == null) continue;
                bombardment.activate(event.getEliteMobEntity());
            }
        }

        @EventHandler
        public void onCombatLeave(EliteMobExitCombatEvent event) {
            for (String bombardmentPower : bombardments) {
                Bombardment bombardment = (Bombardment) event.getEliteMobEntity().getPower(bombardmentPower);
                if (bombardment == null) continue;
                bombardment.deactivate();
            }
        }
    }

}
