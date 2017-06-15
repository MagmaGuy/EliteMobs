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

package com.magmaguy.elitemobs.minorpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

/**
 * Created by MagmaGuy on 28/04/2017.
 */
public class AttackFreeze extends MinorPowers implements Listener {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    String powerMetadata = MetadataHandler.ATTACK_FREEZE_MD;

    private int processID;

    @Override
    public void applyPowers(Entity entity) {

        entity.setMetadata(powerMetadata, new FixedMetadataValue(plugin, true));
        MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance();
        minorPowerPowerStance.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        return entity.hasMetadata(powerMetadata);

    }

    @EventHandler
    public void attackFreeze(EntityDamageByEntityEvent event) {

        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();

        Block block = damagee.getLocation().getBlock();
        Material originalMaterial = block.getType();

        if (!originalMaterial.equals(Material.AIR) || !(damagee instanceof Player)) {

            return;

        }

        if (damagee.hasMetadata(MetadataHandler.FROZEN_COOLDOWN)) {

            return;

        }

        //if a block spawned by the plugin is already in place, skip effect
        if (block.hasMetadata("TemporaryBlock")) {

            return;

        }

        if (damager.hasMetadata(powerMetadata)) {

            processID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

                int counter = 0;

                @Override
                public void run() {

                    iceEffectApplier(counter, damagee, block);
                    counter++;

                }

            }, 1, 1);


        }

        if (damager instanceof Projectile) {

            if (ProjectileMetadataDetector.projectileMetadataDetector((Projectile) damager, powerMetadata)) {

                processID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

                    int counter = 0;

                    @Override
                    public void run() {

                        iceEffectApplier(counter, damagee, block);
                        counter++;

                    }

                }, 1, 1);

            }


        }

    }

    public void iceEffectApplier(int counter, Entity damagee, Block block) {

        if (counter == 0) {

            damagee.setMetadata(MetadataHandler.FROZEN, new FixedMetadataValue(plugin, true));
            damagee.setMetadata(MetadataHandler.FROZEN_COOLDOWN, new FixedMetadataValue(plugin, true));
            block.setType(Material.PACKED_ICE);
            block.setMetadata("TemporaryBlock", new FixedMetadataValue(plugin, true));

            if (damagee instanceof Player) {

                Player player = (Player) damagee;

                player.sendTitle("","Frozen!");

            }

        }

        if (counter == 40) {

            damagee.removeMetadata(MetadataHandler.FROZEN, plugin);

            block.setType(Material.AIR);
            block.removeMetadata("TemporaryBlock", plugin);

        }

        if (counter == 20 * 7) {

            damagee.removeMetadata(MetadataHandler.FROZEN_COOLDOWN, plugin);
            Bukkit.getScheduler().cancelTask(processID);

        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();

        if (player.hasMetadata(MetadataHandler.FROZEN)) {

            event.setCancelled(true);

        }

    }

}
