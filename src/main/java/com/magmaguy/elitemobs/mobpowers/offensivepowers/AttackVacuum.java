package com.magmaguy.elitemobs.mobpowers.offensivepowers;

import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.PowerCooldown;
import com.magmaguy.elitemobs.mobpowers.minorpowers.EventValidator;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashSet;

public class AttackVacuum extends MinorPower implements Listener {

    private static HashSet<EliteMobEntity> cooldownList = new HashSet<>();

    @Override
    public void applyPowers(Entity entity) {
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        EliteMobEntity eliteMobEntity = EventValidator.getEventEliteMob(this, event);
        if (eliteMobEntity == null) return;
        Player player = EntityFinder.findPlayer(event);
        if (PowerCooldown.isInCooldown(eliteMobEntity, cooldownList)) return;

        player.setVelocity(eliteMobEntity.getLivingEntity().getLocation().clone().subtract(player.getLocation()).toVector().normalize().multiply(3));

        PowerCooldown.startCooldownTimer(eliteMobEntity, cooldownList, 5 * 20);

    }

}
