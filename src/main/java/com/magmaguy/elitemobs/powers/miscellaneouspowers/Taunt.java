package com.magmaguy.elitemobs.powers.miscellaneouspowers;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamageEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.ChatColorConverter.convert;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class Taunt extends MinorPower implements Listener {

    private final static List<String> TARGET_TAUNT_LIST = ConfigValues.translationConfig.getStringList("Taunts.Target");
    private final static List<String> GENERIC_DAMAGED_LIST = ConfigValues.translationConfig.getStringList("Taunts.Damaged");
    private final static List<String> DAMAGED_BY_BOW_LIST = ConfigValues.translationConfig.getStringList("Taunts.BowDamaged");
    private final static List<String> HIT_LIST = ConfigValues.translationConfig.getStringList("Taunts.Hit");
    private final static List<String> DEATH_LIST = ConfigValues.translationConfig.getStringList("Taunts.Death");

    public Taunt() {
        super(PowersConfig.getPower("taunt.yml"));
    }

    /**
     * Runs when the Elite Mob targets a player
     *
     * @param event
     */
    @EventHandler
    public void onTarget(EliteMobTargetPlayerEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;
        nametagProcessor(event.getEliteMobEntity().getLivingEntity(), TARGET_TAUNT_LIST);
    }

    /**
     * Runs when the Elite Mob is damaged by a player
     *
     * @param event
     */
    @EventHandler
    public void onDamaged(EliteMobDamageEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;

        if (!(event.getEntity() instanceof LivingEntity) ||
                ((LivingEntity) event.getEntity()).getHealth() - event.getEntityDamageEvent().getFinalDamage() <= 0 ||
                !event.getEntity().isValid())
            return;

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;
        if (eliteMobEntity.hasPower(this)) {
            Entity entity = event.getEntity();

            if (event.getEntityDamageEvent().getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE))
                nametagProcessor(entity, DAMAGED_BY_BOW_LIST);
            else
                nametagProcessor(entity, GENERIC_DAMAGED_LIST);
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
        nametagProcessor(event.getEliteMobEntity().getLivingEntity(), HIT_LIST);
    }

    /**
     * Runs when the Elite Mob dies
     *
     * @param event
     */
    @EventHandler
    public void onDeath(EliteMobDeathEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;
        nametagProcessor(event.getEntity(), DEATH_LIST);
    }

    //Also used by the custom bosses
    public static void nametagProcessor(Entity entity, List<String> list) {
        int randomizedKey = ThreadLocalRandom.current().nextInt(list.size());
        String tempName = list.get(randomizedKey);
        entity.setCustomName(convert(tempName));
        new BukkitRunnable() {

            @Override
            public void run() {
                if (!entity.isValid())
                    return;
                entity.setCustomName(EntityTracker.getEliteMobEntity(entity).getName());
            }


        }.runTaskLater(MetadataHandler.PLUGIN, 4 * 20);
    }

}
