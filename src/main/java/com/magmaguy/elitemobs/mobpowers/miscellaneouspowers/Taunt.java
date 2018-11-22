/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.mobpowers.miscellaneouspowers;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.minorpowers.EventValidator;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
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

    @Override
    public void applyPowers(Entity entity) {
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent event) {

        if (EntityTracker.hasPower(this, event.getEntity()))
            if (event.getTarget() instanceof Player) {
                Entity targetter = event.getEntity();
                nametagProcessor(targetter, TARGET_TAUNT_LIST);
            }

    }

    @EventHandler
    public void onDamaged(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof LivingEntity) ||
                ((LivingEntity) event.getEntity()).getHealth() - event.getFinalDamage() <= 0 ||
                !event.getEntity().isValid())
            return;

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;
        if (EntityTracker.hasPower(this, event.getEntity())) {
            Entity entity = event.getEntity();

            if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE))
                nametagProcessor(entity, DAMAGED_BY_BOW_LIST);
            else
                nametagProcessor(entity, GENERIC_DAMAGED_LIST);

        }

    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        EliteMobEntity eliteMobEntity = EventValidator.getEventEliteMob(this, event);
        if (eliteMobEntity == null) return;

        nametagProcessor(eliteMobEntity.getLivingEntity(), HIT_LIST);

    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (EntityTracker.hasPower(this, event.getEntity()))
            nametagProcessor(event.getEntity(), DEATH_LIST);

    }

    private void nametagProcessor(Entity entity, List<String> list) {

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
