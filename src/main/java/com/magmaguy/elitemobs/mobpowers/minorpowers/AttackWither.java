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

package com.magmaguy.elitemobs.mobpowers.minorpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobpowers.LivingEntityFinder;
import com.magmaguy.elitemobs.mobpowers.PowerCooldown;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by MagmaGuy on 12/12/2016.
 */
public class AttackWither extends MinorPowers implements Listener {

    String powerMetadata = MetadataHandler.ATTACK_WITHER_MD;
    String cooldownMetadata = MetadataHandler.ATTACK_WITHER_COOLDOWN;

    @Override
    public void applyPowers(Entity entity) {

        entity.setMetadata(powerMetadata, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance();
        minorPowerPowerStance.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        return entity.hasMetadata(powerMetadata);

    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        Player player = LivingEntityFinder.findPlayer(event);
        LivingEntity eliteMob = LivingEntityFinder.findEliteMob(event);

        if (!EventValidator.eventIsValid(player, eliteMob, powerMetadata, event)) return;
        if (PowerCooldown.cooldownActive(player, eliteMob, cooldownMetadata)) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 50, 1));

        PowerCooldown.cooldownTimer(eliteMob, cooldownMetadata, 10 * 20);

    }

}
