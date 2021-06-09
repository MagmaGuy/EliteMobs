package com.magmaguy.elitemobs.powers.majorpowers.enderdragon.bombardments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.powers.MajorPower;
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

    public Bombardment(PowersConfigFields powersConfigFields) {
        super(powersConfigFields);
        bombardments.add(powersConfigFields.getFileName());
    }

    private boolean isActive = false;
    private boolean firing = false;
    private BukkitTask task = null;

    public void activate(EliteMobEntity eliteMobEntity) {

        if (isActive) return;
        isActive = true;

        task = new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {

                counter++;

                if (stopCondition(eliteMobEntity))
                    return;

                if (firing || isInCooldown(eliteMobEntity))
                    return;

                if (ThreadLocalRandom.current().nextDouble() > 0.1) return;

                if (eliteMobEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON)) {
                    EnderDragon.Phase phase = ((EnderDragon) eliteMobEntity.getLivingEntity()).getPhase();
                    if (phase.equals(EnderDragon.Phase.DYING) ||
                            phase.equals(EnderDragon.Phase.HOVER) ||
                            phase.equals(EnderDragon.Phase.ROAR_BEFORE_ATTACK) ||
                            phase.equals(EnderDragon.Phase.FLY_TO_PORTAL) ||
                            phase.equals(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET)) {
                        return;
                    }
                }

                for (Entity entity : eliteMobEntity.getLivingEntity().getNearbyEntities(10, 100, 10)) {
                    if (entity.getType().equals(EntityType.PLAYER)) {
                        firing = true;
                        fire(eliteMobEntity);
                        return;
                    }
                }

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);
    }

    public void deactivate() {
        firing = false;
        isActive = false;
        task.cancel();
    }

    private boolean stopCondition(EliteMobEntity eliteMobEntity) {
        if (eliteMobEntity == null ||
                eliteMobEntity.getLivingEntity() == null ||
                !eliteMobEntity.getLivingEntity().isValid() ||
                !eliteMobEntity.isInCombat()) {
            deactivate();
            return true;
        }

        if (eliteMobEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON))
            return !EnderDragonPhaseSimplifier.isFlying(
                    ((EnderDragon) eliteMobEntity.getLivingEntity()).getPhase(),
                    false);

        return false;
    }

    public int firingTimer = 0;

    private void fire(EliteMobEntity eliteMobEntity) {

        doCooldown(eliteMobEntity);

        isActive = true;

        firingTimer = 0;

        new BukkitRunnable() {

            @Override
            public void run() {
                firingTimer++;
                if (stopCondition(eliteMobEntity) || firingTimer > 20 * 5) {
                    cancel();
                    firing = false;
                    return;
                }

                taskBehavior(eliteMobEntity);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    public abstract void taskBehavior(EliteMobEntity eliteMobEntity);

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
