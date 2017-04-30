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

import com.magmaguy.elitemobs.EliteMobs;
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

    private EliteMobs plugin;
    private int processID;

    MetadataHandler metadataHandler = new MetadataHandler(plugin);

    public AttackFreeze(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    @Override
    public void applyPowers(Entity entity) {

        entity.setMetadata(metadataHandler.attackFreezeMD, new FixedMetadataValue(plugin, true));
        MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance(plugin);
        minorPowerPowerStance.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        return entity.hasMetadata(metadataHandler.attackFreezeMD);

    }

    @EventHandler
    public void attackFreeze(EntityDamageByEntityEvent event) {

        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();

        Block block = damagee.getLocation().getBlock();
        Material originalMaterial = block.getType();

        if (damagee.hasMetadata(metadataHandler.frozenCooldown)) {

            return;

        }

        //if a block spawned by the plugin is already in place, skip effect
        if (block.hasMetadata("TemporaryBlock")) {

            return;

        }

        if (damager.hasMetadata(metadataHandler.attackFreezeMD)) {

            processID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

                int counter = 0;

                @Override
                public void run() {

                    iceEffectApplier(counter, damagee, originalMaterial, block);
                    counter++;

                }

            }, 1, 1);


        }

        if (damager instanceof Projectile) {

            if (ProjectileMetadataDetector.projectileMetadataDetector((Projectile) damager, metadataHandler.attackFreezeMD)) {

                processID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

                    int counter = 0;

                    @Override
                    public void run() {

                        iceEffectApplier(counter, damagee, originalMaterial, block);
                        counter++;

                    }

                }, 1, 1);

            }


        }

    }

    public void iceEffectApplier(int counter, Entity damagee, Material originalMaterial, Block block) {

        if (counter == 0) {

            damagee.setMetadata(metadataHandler.frozen, new FixedMetadataValue(plugin, true));
            damagee.setMetadata(metadataHandler.frozenCooldown, new FixedMetadataValue(plugin, true));
            block.setType(Material.PACKED_ICE);
            block.setMetadata("TemporaryBlock", new FixedMetadataValue(plugin, true));

            if (damagee instanceof Player) {

                Player player = (Player) damagee;

                player.sendTitle("","Frozen!");

            }

        }

        if (counter == 40) {

            damagee.removeMetadata(metadataHandler.frozen, plugin);

            block.setType(originalMaterial);
            block.removeMetadata("TemporaryBlock", plugin);

        }

        if (counter == 20 * 7) {

            damagee.removeMetadata(metadataHandler.frozenCooldown, plugin);
            Bukkit.getScheduler().cancelTask(processID);

        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();

        if (player.hasMetadata(metadataHandler.frozen)) {

            event.setCancelled(true);

        }

    }

}
