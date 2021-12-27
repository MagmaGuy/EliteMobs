package com.magmaguy.elitemobs.powers.miscellaneouspowers;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class Taunt extends MinorPower implements Listener {

    private static final List<String> TARGET_TAUNT_LIST = PowersConfig.getPower("taunt.yml").getFileConfiguration().getStringList("onTarget");
    private static final List<String> GENERIC_DAMAGED_LIST = PowersConfig.getPower("taunt.yml").getFileConfiguration().getStringList("onDamaged");
    private static final List<String> DAMAGED_BY_BOW_LIST = PowersConfig.getPower("taunt.yml").getFileConfiguration().getStringList("onDamagedByBow");
    private static final List<String> HIT_LIST = PowersConfig.getPower("taunt.yml").getFileConfiguration().getStringList("onDamage");
    private static final List<String> DEATH_LIST = PowersConfig.getPower("taunt.yml").getFileConfiguration().getStringList("onDeath");

    public Taunt() {
        super(PowersConfig.getPower("taunt.yml"));
    }

    //Also used by the custom bosses
    public static void nameTagProcessor(EliteEntity eliteEntity, Entity entity, List<String> list) {
        int randomizedKey = ThreadLocalRandom.current().nextInt(list.size());
        String tempName = list.get(randomizedKey);
        entity.setCustomName(ChatColorConverter.convert(tempName));
        new BukkitRunnable() {

            @Override
            public void run() {
                if (!entity.isValid())
                    return;
                entity.setCustomName(eliteEntity.getName());
            }


        }.runTaskLater(MetadataHandler.PLUGIN, 4 * 20L);
    }

    /**
     * Runs when the Elite Mob targets a player
     *
     * @param event
     */
    @EventHandler
    public void onTarget(EliteMobTargetPlayerEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;
        nameTagProcessor(event.getEliteMobEntity(), event.getEliteMobEntity().getLivingEntity(), TARGET_TAUNT_LIST);
    }

    /**
     * Runs when the Elite Mob is damaged by a player
     *
     * @param event
     */
    @EventHandler
    public void onDamaged(EliteMobDamagedEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;

        if (!(event.getEntity() instanceof LivingEntity) ||
                ((LivingEntity) event.getEntity()).getHealth() - event.getEntityDamageEvent().getFinalDamage() <= 0 ||
                !event.getEliteMobEntity().isValid())
            return;

        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteEntity == null) return;
        if (eliteEntity.hasPower(this)) {
            Entity entity = event.getEntity();

            if (event.getEntityDamageEvent().getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE))
                nameTagProcessor(eliteEntity, entity, DAMAGED_BY_BOW_LIST);
            else
                nameTagProcessor(eliteEntity, entity, GENERIC_DAMAGED_LIST);
        }

    }

    /**
     * Runs when the Elite Mob damages a player
     *
     * @param event
     */
    @EventHandler
    public void onHit(PlayerDamagedByEliteMobEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;
        nameTagProcessor(event.getEliteMobEntity(), event.getEliteMobEntity().getLivingEntity(), HIT_LIST);
    }

    /**
     * Runs when the Elite Mob dies
     *
     * @param event
     */
    @EventHandler
    public void onDeath(EliteMobDeathEvent event) {
        if (!event.getEliteEntity().hasPower(this)) return;
        nameTagProcessor(event.getEliteEntity(), event.getEntity(), DEATH_LIST);
    }

}
