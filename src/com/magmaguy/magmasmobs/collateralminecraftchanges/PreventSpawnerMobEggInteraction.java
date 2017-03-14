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

package com.magmaguy.magmasmobs.collateralminecraftchanges;

import com.magmaguy.magmasmobs.MagmasMobs;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

/**
 * Created by MagmaGuy on 11/12/2016.
 */
public class PreventSpawnerMobEggInteraction implements Listener {

    private MagmasMobs plugin;

    public PreventSpawnerMobEggInteraction(Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (event.getAction().equals(RIGHT_CLICK_BLOCK)) {

            if (event.getClickedBlock().getType().equals(Material.MOB_SPAWNER) &&
                    player.getEquipment().getItemInMainHand().getType().equals(Material.MONSTER_EGG)) {

                event.setCancelled(true);
                getLogger().info("MagmasMobs - Mob spawner change cancelled.");

            }

        }

    }

}
