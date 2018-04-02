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

package com.magmaguy.elitemobs.events.eventitems;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.elitedrops.UniqueItemConstructor;
import com.magmaguy.elitemobs.events.mobs.ZombieKing;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class ZombieKingAxe implements Listener {

    @EventHandler
    public void onIteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)))
            return;

        if (player.hasMetadata(MetadataHandler.USING_ZOMBIE_KING_AXE)) return;

        ItemStack zombieKingAxe = player.getInventory().getItemInMainHand();

        if (UniqueItemConstructor.zombieKingAxeDetector(zombieKingAxe)) {

            if (zombieKingAxe.getDurability() + 4 > zombieKingAxe.getType().getMaxDurability()) {
                return;
//                player.getInventory().removeItem(zombieKingAxe);
            } else zombieKingAxe.setDurability((short) (zombieKingAxe.getDurability() + 4));

            ZombieKing.initializeFlamethrower(player.getLocation(), player.getLocation().getDirection(), player, true);
            player.setMetadata(MetadataHandler.USING_ZOMBIE_KING_AXE, new FixedMetadataValue(MetadataHandler.PLUGIN, true));

            new BukkitRunnable() {

                @Override
                public void run() {

                    player.removeMetadata(MetadataHandler.USING_ZOMBIE_KING_AXE, MetadataHandler.PLUGIN);

                }

            }.runTaskLater(MetadataHandler.PLUGIN, 3 * 20);

        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        if (event.getPlayer().hasMetadata(MetadataHandler.USING_ZOMBIE_KING_AXE)) event.setCancelled(true);

    }

}
