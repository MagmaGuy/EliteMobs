package com.magmaguy.elitemobs.powers.offensivepowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class AttackArrow extends MinorPower implements Listener {

    public AttackArrow() {
        super(PowersConfig.getPower("attack_arrow.yml"));
    }

    @EventHandler
    public void targetEvent(EliteMobTargetPlayerEvent event) {
        if (!(event.getEliteMobEntity().getLivingEntity() instanceof Monster)) return;
        AttackArrow attackArrow = (AttackArrow) event.getEliteMobEntity().getPower(this);
        if (attackArrow == null) return;
        if (attackArrow.getIsFiring()) return;

        attackArrow.setIsFiring(true);
        repeatingArrowTask(attackArrow, event.getEliteMobEntity());
    }

    private void repeatingArrowTask(AttackArrow attackArrow, EliteMobEntity eliteMobEntity) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!eliteMobEntity.getLivingEntity().isValid() || ((Monster) eliteMobEntity.getLivingEntity()).getTarget() == null) {
                    attackArrow.setIsFiring(false);
                    cancel();
                    return;
                }

                for (Entity nearbyEntity : eliteMobEntity.getLivingEntity().getNearbyEntities(20, 20, 20))
                    if (nearbyEntity instanceof Player)
                        if (((Player) nearbyEntity).getGameMode().equals(GameMode.ADVENTURE) ||
                                ((Player) nearbyEntity).getGameMode().equals(GameMode.SURVIVAL))
                            shootArrow(eliteMobEntity.getLivingEntity(), (Player) nearbyEntity);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 8);

    }

    public static Arrow shootArrow(Entity entity, Player player) {
        return (Arrow) EliteProjectile.create(EntityType.ARROW, entity, player, true);
    }

}
