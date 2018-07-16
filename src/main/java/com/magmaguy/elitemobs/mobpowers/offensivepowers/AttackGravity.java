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

package com.magmaguy.elitemobs.mobpowers.offensivepowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobpowers.LivingEntityFinder;
import com.magmaguy.elitemobs.mobpowers.PowerCooldown;
import com.magmaguy.elitemobs.mobpowers.minorpowers.EventValidator;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPowers;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by MagmaGuy on 05/11/2016.
 */
public class AttackGravity extends MinorPowers implements Listener {

    String powerMetadata = MetadataHandler.ATTACK_GRAVITY_MD;
    String cooldownMetadata = MetadataHandler.ATTACK_GRAVITY_COOLDOWN;

    @Override
    public void applyPowers(Entity entity) {

        MetadataHandler.registerMetadata(entity, powerMetadata, true);
        MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance();
        minorPowerPowerStance.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        return entity.hasMetadata(powerMetadata);

    }

    @EventHandler
    public void attackGravity(EntityDamageByEntityEvent event) {

        Player player = LivingEntityFinder.findPlayer(event);
        LivingEntity eliteMob = LivingEntityFinder.findEliteMob(event);

        if (!EventValidator.eventIsValid(player, eliteMob, powerMetadata, event)) return;
        if (PowerCooldown.cooldownChecker(player, eliteMob, cooldownMetadata)) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 2 * 20, 3));
        PowerCooldown.startCooldownTimer(eliteMob, cooldownMetadata, 10 * 20);

    }

}
