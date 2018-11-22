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
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.PowerCooldown;
import com.magmaguy.elitemobs.mobpowers.minorpowers.EventValidator;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

/**
 * Created by MagmaGuy on 28/04/2017.
 */
public class AttackWeb extends MinorPower implements Listener {

    private static HashSet<EliteMobEntity> cooldownList = new HashSet<>();

    @Override
    public void applyPowers(Entity entity) {
    }

    @EventHandler
    public void attackWeb(EntityDamageByEntityEvent event) {

        EliteMobEntity eliteMobEntity = EventValidator.getEventEliteMob(this, event);
        if (eliteMobEntity == null) return;
        Player player = EntityFinder.findPlayer(event);
        if (PowerCooldown.isInCooldown(eliteMobEntity, cooldownList)) return;

        Block block = player.getLocation().getBlock();
        Material originalMaterial = block.getType();

        if (!originalMaterial.equals(Material.AIR))
            return;

        block.setType(Material.WEB);

        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(originalMaterial);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3);

        PowerCooldown.startCooldownTimer(eliteMobEntity, cooldownList, 3 * 20);

    }

}
